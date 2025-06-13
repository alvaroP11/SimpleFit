package com.example.appgimnasiotfg.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appgimnasiotfg.databinding.ActivityRutinaBinding
import com.example.appgimnasiotfg.ui.ejercicios.EjercicioActivity
import com.example.appgimnasiotfg.ui.model.Ejercicio
import com.example.appgimnasiotfg.ui.model.EjercicioRutina
import com.example.appgimnasiotfg.ui.model.Rutina
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RutinaActivity : AppCompatActivity(), RutinaFragmentDialog.OnNombreConfirmadoListener {
    private lateinit var binding: ActivityRutinaBinding
    private val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    private var diaSeleccionado: Int = 0
    private lateinit var rutina: Rutina
    private lateinit var adapter: EjercicioRutinaAdapter

    private lateinit var addEjerciciosLauncher: ActivityResultLauncher<Intent>
    private lateinit var editarEjercicioLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRutinaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Recibir rutina del intent
        rutina = intent.getSerializableExtra("rutina") as? Rutina
            ?: throw IllegalStateException("No se recibió la rutina")

        // Setup RecyclerView y Adapter
        adapter = EjercicioRutinaAdapter(
            mutableListOf(),
            onItemClick = { ejercicioRutina ->
                val intent = Intent(this, EjercicioActivity::class.java)
                intent.putExtra("ejercicio", ejercicioRutina.ejercicio)
                startActivity(intent)
            },
            onEditarClick = { ejercicioRutina ->
                val intent = Intent(this, EditarEjercicioRutinaActivity::class.java).apply {
                    putExtra("rutinaId", rutina.id)
                    putExtra("diaSeleccionado", diaSeleccionado)
                    putExtra("ejercicioRutina", ejercicioRutina)
                }
                editarEjercicioLauncher.launch(intent)
            },
            onEliminarClick = { ejercicioRutina ->
                eliminarEjercicioDeFirebase(ejercicioRutina)
            }
        )

        binding.editarNombreBT.setOnClickListener {
            val dialog = RutinaFragmentDialog.newInstance(
                titulo = "Editar nombre de rutina",
                textoBoton = "Guardar",
                nombreInicial = rutina.nombreRutina
            )
            dialog.show(supportFragmentManager, "EditarNombreRutinaDialog")
        }

        binding.ejerciciosRutinaRV.layoutManager = LinearLayoutManager(this)
        binding.ejerciciosRutinaRV.adapter = adapter

        // Launchers para Add y Edit
        addEjerciciosLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) cargarEjerciciosDelDia(diaSeleccionado)
        }

        editarEjercicioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) cargarEjerciciosDelDia(diaSeleccionado)
        }

        // Spinner de días
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, diasSemana)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.diasSP.adapter = adapterSpinner

        binding.diasSP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                diaSeleccionado = position
                cargarEjerciciosDelDia(diaSeleccionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Botón añadir ejercicio
        binding.addEjerciciosBT.setOnClickListener {
            val intent = Intent(this, AddEjerciciosActivity::class.java).apply {
                putExtra("diaSeleccionado", diaSeleccionado)
                putExtra("rutinaId", rutina.id)
            }
            addEjerciciosLauncher.launch(intent)
        }

        // Nombre de la rutina
        binding.nombreRutinaTV.text = rutina.nombreRutina

        // Ajustar padding para sistema (barra de estado y navegación)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun cargarEjerciciosDelDia(dia: Int) {
        val db = Firebase.firestore
        val referenciaDia = db.collection("rutinas")
            .document(rutina.id)
            .collection("diasRutina")
            .document(dia.toString())

        referenciaDia.get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    adapter.updateData(emptyList())
                    return@addOnSuccessListener
                }

                val ejerciciosRutinaList = doc.get("ejercicios") as? List<Map<String, Any>> ?: emptyList()
                val listaFinal = mutableListOf<EjercicioRutina>()
                val tareas = mutableListOf<Task<DocumentSnapshot>>()

                for (map in ejerciciosRutinaList) {
                    val ejercicioId = map["ejercicioId"] as? String ?: continue
                    val series = (map["series"] as? Long)?.toInt() ?: 0
                    val repeticiones = (map["repeticiones"] as? Long)?.toInt() ?: 0
                    val peso = (map["peso"] as? Double) ?: 0.0
                    val tiempo = (map["tiempo"] as? Long)?.toInt() ?: 0

                    val ejercicioRutina = EjercicioRutina(
                        ejercicioId = ejercicioId,
                        series = series,
                        repeticiones = repeticiones,
                        peso = peso,
                        tiempo = tiempo
                    )

                    val tarea = db.collection("ejercicios")
                        .document(ejercicioId)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                val ejercicio = snapshot.toObject(Ejercicio::class.java)
                                ejercicio?.id = snapshot.id
                                ejercicioRutina.ejercicio = ejercicio
                                listaFinal.add(ejercicioRutina)
                            }
                        }

                    tareas.add(tarea)
                }

                Tasks.whenAllSuccess<Any>(tareas)
                    .addOnSuccessListener {
                        listaFinal.sortBy { it.ejercicio?.nombre ?: "" }
                        adapter.updateData(listaFinal)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al cargar ejercicios", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener ejercicios del día", Toast.LENGTH_SHORT).show()
            }
    }
    private fun eliminarEjercicioDeFirebase(ejercicioRutina: EjercicioRutina) {
        val db = Firebase.firestore
        val refDia = db.collection("rutinas")
            .document(rutina.id)
            .collection("diasRutina")
            .document(diaSeleccionado.toString())

        refDia.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val ejerciciosList = doc.get("ejercicios") as? MutableList<Map<String, Any>> ?: mutableListOf()

                    val nuevaLista = ejerciciosList.filter {
                        it["ejercicioId"] != ejercicioRutina.ejercicioId
                    }

                    refDia.update("ejercicios", nuevaLista)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Ejercicio eliminado", Toast.LENGTH_SHORT).show()
                            cargarEjerciciosDelDia(diaSeleccionado)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al acceder a la rutina", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onNombreConfirmado(nombre: String) {
        val db = Firebase.firestore
        val rutinaRef = db.collection("rutinas").document(rutina.id)

        rutinaRef.update("nombreRutina", nombre)
            .addOnSuccessListener {
                rutina.nombreRutina = nombre
                binding.nombreRutinaTV.text = nombre
                Toast.makeText(this, "Nombre actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
