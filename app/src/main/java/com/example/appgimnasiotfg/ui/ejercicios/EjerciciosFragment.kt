package com.example.appgimnasiotfg.ui.ejercicios

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appgimnasiotfg.R
import com.example.appgimnasiotfg.databinding.FragmentEjerciciosBinding
import com.example.appgimnasiotfg.ui.model.Ejercicio
import com.example.appgimnasiotfg.ui.model.Musculo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EjerciciosFragment : Fragment() {
    companion object {
        fun newInstance() = EjerciciosFragment()
    }

    private val viewModel: EjerciciosViewModel by viewModels()
    private lateinit var binding: FragmentEjerciciosBinding

    private val auth = FirebaseAuth.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EjercicioAdapter
    private val listaEjercicios = mutableListOf<Ejercicio>()
    private var listaEjerciciosFiltrados = mutableListOf<Ejercicio>()

    private val musculos = mutableListOf<Musculo>()
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private var musculoIds = mutableListOf<String>()
    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEjerciciosBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializacion del RecyclerView y Spinner sin binding
        recyclerView = view.findViewById(R.id.ejerciciosRV)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        spinner = view.findViewById(R.id.ejerciciosSP)

        // Inicializacion del adapter para el RecyclerView, el cual al hacer clic, hace un intent pasando el objeto seleccionado
        adapter = EjercicioAdapter(mutableListOf()) { ejercicio ->
            val intent = Intent(requireContext(), EjercicioActivity::class.java)
            intent.putExtra("ejercicio", ejercicio)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Desactivamos el Spinner de musculos, se activará si en su metodo, se cargan los musculos correctamente
        spinner.isEnabled = false
        cargarMusculos()

        // Filtro segun el item seleccionado en el Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMusculoId = musculoIds.getOrNull(position) ?: "" //Si la posición es null, busca un ID vacio (todos los ejercicios)
                filtrarEjerciciosPorMusculo(selectedMusculoId)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                filtrarEjerciciosPorMusculo("")
            }
        }

        // Obtencion de los ejercicios para el RecyclerView
        cargarEjerciciosDesdeFirestore()
    }

    private fun cargarEjerciciosDesdeFirestore() {
        val db = Firebase.firestore

        db.collection("ejercicios")
            .get()
            .addOnSuccessListener { result ->
                listaEjercicios.clear()
                for (doc in result) {
                    val ejercicio = doc.toObject(Ejercicio::class.java)
                    ejercicio.id = doc.id
                    listaEjercicios.add(ejercicio)
                }
                filtrarEjerciciosPorMusculo("")  // Mostrar todo al inicio
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener ejercicios", e)
                Toast.makeText(requireContext(), "Error al cargar datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    fun filtrarEjerciciosPorMusculo(musculoId: String) {
        if (musculoId.isEmpty()) {
            // Si el ID está vacio, lista completa
            listaEjerciciosFiltrados = listaEjercicios.toMutableList()
        } else {
            //Si el ID contiene algo, se recorre cada objeto, comprobando que contenga el ID del musculo que queremos y añadiendolo a la lista
            listaEjerciciosFiltrados = listaEjercicios.filter {
                it.musculoIds.contains(musculoId)
            }.toMutableList()
        }
        // Se añade la nueva lista al adapter
        adapter.setEjercicios(listaEjerciciosFiltrados)
    }

    fun cargarMusculos() {
        val db = Firebase.firestore
        // Aseguro la desactivacion del Spinner mientras se cargan los musculos
        spinner.isEnabled = false
        db.collection("musculos")
            .get()
            .addOnSuccessListener { result ->
                musculos.clear()
                musculoIds.clear()
                //Añado una primera opcion general, con ID en blanco para que no sirva para filtrar
                musculos.add(Musculo("", "Todos"))
                musculoIds.add("")

                for (doc in result) {
                    // Obtengo todos los musculos y guardo sus IDs y nombres
                    val musculo = doc.toObject(Musculo::class.java)
                    musculo.id = doc.id
                    // Dos listas, en el mismo orden para tener toda la información, pero no usar gran flujo de datos si solo quiero usar un ID y no el item entero
                    musculos.add(musculo)
                    musculoIds.add(doc.id)
                }

                // Obtengo una lista solo con los nombres para asignarla al Spinner
                val nombres = musculos.map { it.nombre }
                spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                // Establezco el adapter y habilito el spinner de nuevo
                spinner.adapter = spinnerAdapter
                spinner.isEnabled = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error cargando músculos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}