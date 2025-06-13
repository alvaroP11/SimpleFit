package com.example.appgimnasiotfg.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appgimnasiotfg.databinding.ActivityEditarEjercicioRutinaBinding
import com.example.appgimnasiotfg.ui.model.EjercicioRutina
import com.example.appgimnasiotfg.ui.model.TipoEjercicio
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditarEjercicioRutinaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditarEjercicioRutinaBinding

    private lateinit var ejercicioRutina: EjercicioRutina
    private lateinit var rutinaId: String
    private var diaSeleccionado: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarEjercicioRutinaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        rutinaId = intent.getStringExtra("rutinaId") ?: return finish()
        diaSeleccionado = intent.getIntExtra("diaSeleccionado", 0)
        ejercicioRutina = intent.getSerializableExtra("ejercicioRutina") as EjercicioRutina

        val ejercicio = ejercicioRutina.ejercicio
        val tipo = ejercicio?.tipo ?: TipoEjercicio.PESO_VARIABLE

        // Mostrar datos básicos
        binding.nombreEjercicioTV.text = ejercicio?.nombre ?: "Ejercicio"
        binding.descripcionEjercicioTV.text = ejercicio?.descripcion ?: "Descripción"

        // Mostrar/ocultar campos según el tipo de ejercicio
        when (tipo) {
            TipoEjercicio.PESO_VARIABLE -> {
                binding.grupoSeries.visibility = View.VISIBLE
                binding.grupoRepeticiones.visibility = View.VISIBLE
                binding.grupoPeso.visibility = View.VISIBLE
                binding.grupoDuracion.visibility = View.GONE

                binding.seriesET.setText(ejercicioRutina.series.toString())
                binding.repeticionesET.setText(ejercicioRutina.repeticiones.toString())
                binding.pesoET.setText(ejercicioRutina.peso.toString())
            }
            TipoEjercicio.TIEMPO -> {
                binding.grupoSeries.visibility = View.GONE
                binding.grupoRepeticiones.visibility = View.GONE
                binding.grupoPeso.visibility = View.GONE
                binding.grupoDuracion.visibility = View.VISIBLE

                binding.duracionET.setText(ejercicioRutina.tiempo.toString())
            }
            TipoEjercicio.PESO_PROPIO -> {
                binding.grupoSeries.visibility = View.VISIBLE
                binding.grupoRepeticiones.visibility = View.VISIBLE
                binding.grupoPeso.visibility = View.GONE
                binding.grupoDuracion.visibility = View.GONE

                binding.seriesET.setText(ejercicioRutina.series.toString())
                binding.repeticionesET.setText(ejercicioRutina.repeticiones.toString())
            }
        }


        binding.guardarBT.setOnClickListener {
            when (tipo) {
                TipoEjercicio.PESO_VARIABLE -> {
                    val nuevasSeries = binding.seriesET.text.toString().toIntOrNull() ?: 0
                    val nuevasReps = binding.repeticionesET.text.toString().toIntOrNull() ?: 0
                    val nuevoPeso = binding.pesoET.text.toString().toDoubleOrNull() ?: 0.0
                    actualizarEjercicioEnFirebase(nuevasSeries, nuevasReps, nuevoPeso, 0)
                }
                TipoEjercicio.TIEMPO -> {
                    val nuevaDuracion = binding.duracionET.text.toString().toIntOrNull() ?: 0
                    actualizarEjercicioEnFirebase(0, 0, 0.0, nuevaDuracion)
                }
                TipoEjercicio.PESO_PROPIO -> {
                    val nuevasSeries = binding.seriesET.text.toString().toIntOrNull() ?: 0
                    val nuevasReps = binding.repeticionesET.text.toString().toIntOrNull() ?: 0
                    actualizarEjercicioEnFirebase(nuevasSeries, nuevasReps, 0.0, 0)
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun actualizarEjercicioEnFirebase(series: Int, reps: Int, peso: Double, duracion: Int) {
        val db = Firebase.firestore
        val diaRef = db.collection("rutinas").document(rutinaId)
            .collection("diasRutina").document(diaSeleccionado.toString())

        diaRef.get().addOnSuccessListener { doc ->
            val ejercicios = (doc.get("ejercicios") as? List<Map<String, Any>> ?: emptyList()).map {
                val id = it["ejercicioId"] as? String ?: return@map null
                if (id == ejercicioRutina.ejercicioId) {
                    val nuevo = mutableMapOf<String, Any>(
                        "ejercicioId" to id,
                        "series" to (if (series > 0) series else it["series"] ?: 0),
                        "repeticiones" to (if (reps > 0) reps else it["repeticiones"] ?: 0),
                        "peso" to (if (peso > 0.0) peso else it["peso"] ?: 0.0),
                        "tiempo" to (if (duracion > 0) duracion else it["tiempo"] ?: 0)
                    )
                    nuevo
                } else {
                    it
                }
            }.filterNotNull()

            diaRef.set(mapOf("ejercicios" to ejercicios))
                .addOnSuccessListener {
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }

        }.addOnFailureListener {
            Toast.makeText(this, "No se pudo obtener el día", Toast.LENGTH_SHORT).show()
        }
    }
}
