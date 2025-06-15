package com.example.appgimnasiotfg.ui.home

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appgimnasiotfg.databinding.ActivityAddEjerciciosBinding
import com.example.appgimnasiotfg.ui.model.Ejercicio
import com.example.appgimnasiotfg.ui.model.EjercicioRutina
import com.example.appgimnasiotfg.ui.model.Musculo
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddEjerciciosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEjerciciosBinding
    private var diaSeleccionado: Int = 0
    private var rutinaId: String = ""

    private val diasSemana = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")

    private lateinit var adapter: AddEjercicioAdapter
    private val listaEjercicios = mutableListOf<Ejercicio>()

    private lateinit var spinner: Spinner
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private val musculos = mutableListOf<Musculo>()
    private val musculoIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEjerciciosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Datos del Intent
        diaSeleccionado = intent.getIntExtra("diaSeleccionado", 0)
        rutinaId = intent.getStringExtra("rutinaId") ?: return finishWithError("ID de rutina inválido.")

        binding.addRutinaTV.text = "Añadir ejercicios al ${diasSemana[diaSeleccionado]}"

        // RecyclerView y adapter
        adapter = AddEjercicioAdapter(listaEjercicios)
        binding.recyclerViewEjercicios.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewEjercicios.adapter = adapter

        // Spinner
        spinner = binding.addEjerciciosSP
        cargarMusculos()
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMusculoId = musculoIds.getOrNull(position) ?: ""
                adapter.filtrarPorMusculo(selectedMusculoId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                adapter.filtrarPorMusculo("")
            }
        }

        // Cargar ejercicios
        cargarEjerciciosDesdeFirebase()

        // Botón guardar ejercicios
        binding.addEjerciciosRutinaBT.setOnClickListener {
            val seleccionados = adapter.obtenerSeleccionados()
            if (seleccionados.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos un ejercicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevosEjercicios = seleccionados.map {
                EjercicioRutina(
                    ejercicioId = it.id,
                    series = 0,
                    repeticiones = 0,
                    peso = 0.0
                )
            }

            subirEjerciciosAFirebase(nuevosEjercicios)
        }

        // Botón volver
        binding.volverALaRutina3BT.setOnClickListener {
            finish()
        }

        // Insets sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun cargarEjerciciosDesdeFirebase() {
        Firebase.firestore.collection("ejercicios")
            .get()
            .addOnSuccessListener { result ->
                val ejercicios = result.map { doc ->
                    doc.toObject(Ejercicio::class.java).apply { id = doc.id }
                }

                listaEjercicios.clear()
                listaEjercicios.addAll(ejercicios)

                adapter = AddEjercicioAdapter(listaEjercicios)
                binding.recyclerViewEjercicios.adapter = adapter

                // Mostrar todos al principio
                adapter.filtrarPorMusculo("")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar ejercicios", Toast.LENGTH_SHORT).show()
            }
    }

    private fun subirEjerciciosAFirebase(ejerciciosNuevos: List<EjercicioRutina>) {
        val db = Firebase.firestore
        val docRef = db.collection("rutinas").document(rutinaId)
            .collection("diasRutina").document(diaSeleccionado.toString())

        docRef.get()
            .addOnSuccessListener { doc ->
                val actuales = (doc.get("ejercicios") as? List<Map<String, Any>> ?: emptyList())
                    .mapNotNull { map ->
                        val id = map["ejercicioId"] as? String ?: return@mapNotNull null
                        val series = (map["series"] as? Long)?.toInt() ?: 0
                        val repeticiones = (map["repeticiones"] as? Long)?.toInt() ?: 0
                        val peso = (map["peso"] as? Double) ?: 0.0
                        EjercicioRutina(id, series, repeticiones, peso)
                    }
                    .toMutableList()

                for (nuevo in ejerciciosNuevos) {
                    if (actuales.none { it.ejercicioId == nuevo.ejercicioId }) {
                        actuales.add(nuevo)
                    }
                }

                val mapFinal = actuales.map {
                    mapOf(
                        "ejercicioId" to it.ejercicioId,
                        "series" to it.series,
                        "repeticiones" to it.repeticiones,
                        "peso" to it.peso
                    )
                }

                docRef.set(mapOf("ejercicios" to mapFinal))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Ejercicios añadidos correctamente", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener ejercicios actuales: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun finishWithError(mensaje: String): Nothing {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        finish()
        throw RuntimeException(mensaje)
    }

    private fun cargarMusculos() {
        Firebase.firestore.collection("musculos")
            .get()
            .addOnSuccessListener { result ->
                musculos.clear()
                musculoIds.clear()
                musculos.add(Musculo(id = "", nombre = "Todos", imagen = ""))
                musculoIds.add("")
                for (doc in result) {
                    val m = doc.toObject(Musculo::class.java).apply { id = doc.id }
                    musculos.add(m)
                    musculoIds.add(m.id)
                }
                val nombres = musculos.map { it.nombre }
                spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = spinnerAdapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar músculos", Toast.LENGTH_SHORT).show()
            }
    }
}
