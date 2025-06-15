package com.example.appgimnasiotfg.ui.ejercicios

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.appgimnasiotfg.R
import com.example.appgimnasiotfg.databinding.ActivityEjercicioBinding
import com.example.appgimnasiotfg.ui.model.Ejercicio
import com.example.appgimnasiotfg.ui.model.Maquina
import com.example.appgimnasiotfg.ui.model.Musculo
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EjercicioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEjercicioBinding

    // Adaptadores para los RecyclerViews
    private lateinit var musculoAdapter: MusculoAdapter
    private lateinit var maquinaAdapter: MaquinaAdapter

    // Declaración explícita de las listas que alimentan los adapters
    private var listaMusculos = mutableListOf<Musculo>()
    private var listaMaquinas = mutableListOf<Maquina>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEjercicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Adapters de musculos y maquinas
        musculoAdapter = MusculoAdapter(listaMusculos)
        maquinaAdapter = MaquinaAdapter(listaMaquinas)

        // Asigno los adapters a los RecyclerViews y ajusto que el Layout de estos sea horizontal
        binding.musculosRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.musculosRV.adapter = musculoAdapter

        binding.maquinasRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.maquinasRV.adapter = maquinaAdapter

        // Cargar datos del ejercicio
        val ejercicio = intent.getSerializableExtra("ejercicio") as? Ejercicio //Con "?", en caso de lanzar una excepcion, lo evita y devuelve un Ejercicio null
        if (ejercicio != null) {
            binding.nombreEjercicioTV.text = ejercicio.nombre
            binding.descripcionTV.text = ejercicio.descripcion
            Glide.with(this).load(ejercicio.imagen).into(binding.ejercicioImg)

            cargarMusculosPorIds(ejercicio.musculoIds)
            cargarMaquinasPorIds(ejercicio.maquinaIds)
        }

        binding.volverALaRutinaBT.setOnClickListener{
            finish()
        }
    }

    private fun cargarMusculosPorIds(musculoIds: List<String>) {
        // Filtrar que no haya IDs vacios que causen crasheo de la App
        val validIds = musculoIds.filter { it.isNotBlank() }
        if (validIds.isEmpty()) {
            // No hay IDs válidos, evitamos hacer la consulta y añadimos un "Placeholder"
            listaMusculos.clear()
            listaMusculos.add(
                Musculo(
                    id = "placeholder",
                    nombre = "Ningun músculo",
                    imagen = ""
                )
            )
            musculoAdapter.notifyDataSetChanged()
            return
        }

        // Hay IDs validos, se hace la consulta en la coleccion "musculos" en base a la lista de IDs obtenida de los "ejercicios"
        val db = Firebase.firestore
        db.collection("musculos")
            .whereIn(FieldPath.documentId(), validIds)
            .get()
            .addOnSuccessListener { result ->
                listaMusculos.clear()
                for (doc in result) {
                    val musculo = doc.toObject(Musculo::class.java)
                    musculo.id = doc.id
                    listaMusculos.add(musculo)
                }
                musculoAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al cargar músculos", e)
                Toast.makeText(this, "Error al cargar músculos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun cargarMaquinasPorIds(maquinaIds: List<String>) {
        // Filtrar que no haya ID vacio que causen crasheo de la App
        val validIds = maquinaIds.filter { it.isNotBlank() }
        if (validIds.isEmpty()) {
            listaMaquinas.clear()
            listaMaquinas.add(
                Maquina(
                    id = "placeholder",
                    nombre = "Ninguna máquina",
                    descripcion = "",
                    imagen = ""
                )
            )
            maquinaAdapter.notifyDataSetChanged()
            return
        }

        val db = Firebase.firestore
        db.collection("maquinas")
            .whereIn(FieldPath.documentId(), validIds)
            .get()
            .addOnSuccessListener { result ->
                listaMaquinas.clear()
                for (doc in result) {
                    val maquina = doc.toObject(Maquina::class.java)
                    maquina.id = doc.id
                    listaMaquinas.add(maquina)
                }
                maquinaAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al cargar máquinas", e)
                Toast.makeText(this, "Error al cargar máquinas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
