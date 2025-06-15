package com.example.appgimnasiotfg.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appgimnasiotfg.databinding.FragmentHomeBinding
import com.example.appgimnasiotfg.ui.model.Rutina
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(), RutinaFragmentDialog.OnNombreConfirmadoListener {
    private lateinit var binding: FragmentHomeBinding
    private val auth = FirebaseAuth.getInstance()

    private lateinit var rutinaAdapter: RutinaAdapter
    private lateinit var rutinaPrehechaAdapter: RutinaAdapter

    private val listaRutinas = mutableListOf<Rutina>()
    private val listaRutinasPrehechas = mutableListOf<Rutina>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rutinasRV.layoutManager = LinearLayoutManager(requireContext())
        binding.rutinasPrehechasRV.layoutManager = LinearLayoutManager(requireContext())

        // Las rutinas del usuario permiten ser borradas con una larga pulsacion, y en el intent pasan un boolean para activar o desactivar funciones
        rutinaAdapter = RutinaAdapter(
            listaRutinas,
            onItemClick = { rutina ->
                val intent = Intent(requireContext(), RutinaActivity::class.java)
                intent.putExtra("rutina", rutina)
                intent.putExtra("editable", true)
                startActivity(intent)
            },
            onLongClick = { rutina ->
                mostrarDialogoEliminarRutina(rutina)
            },
            editable = true // La rutina podrá ser editada
        )

        // En caso de rutinas prehechas por el administrador, el usuario solo tendra permisos de lectura, no podra borrar la rutina ni editar atributos de la rutina o ejercicios
        rutinaPrehechaAdapter = RutinaAdapter(
            listaRutinasPrehechas,
            onItemClick = { rutina ->
                val intent = Intent(requireContext(), RutinaActivity::class.java)
                intent.putExtra("rutina", rutina)
                intent.putExtra("editable", false)
                startActivity(intent)
            },
            onLongClick = { rutina ->
            },
            editable = false // La rutina no podrá ser editada
        )

        binding.rutinasRV.adapter = rutinaAdapter
        binding.rutinasPrehechasRV.adapter = rutinaPrehechaAdapter

        binding.rutinasRV.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        binding.rutinasPrehechasRV.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        escucharRutinasDelUsuario()
        cargarRutinasPrehechas()

        binding.floatingActionButton.setOnClickListener {
            mostrarDialogCrearRutina()
        }
    }

    // Dialog para crear una rutina vacia
    private fun mostrarDialogCrearRutina() {
        val dialog = RutinaFragmentDialog()
        dialog.show(childFragmentManager, "NuevaRutinaDialog")
    }

    override fun onNombreConfirmado(nombre: String) {
        val user = auth.currentUser

        if (user != null) {
            val db = Firebase.firestore
            val rutina = Rutina(
                nombreRutina = nombre,
                usuarioId = user.uid
            )

            db.collection("rutinas")
                .add(rutina)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Rutina creada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Error al crear rutina: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    //Actualiza dinamicamente la lista de rutinas si se crean o destruyen
    private fun escucharRutinasDelUsuario() {
        val uid = auth.currentUser?.uid ?: return
        val db = Firebase.firestore

        db.collection("rutinas")
            .whereEqualTo("usuarioId", uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar rutinas: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    listaRutinas.clear()
                    for (doc in snapshots) {
                        val rutina = doc.toObject(Rutina::class.java)
                        rutina.id = doc.id
                        listaRutinas.add(rutina)
                    }
                    rutinaAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun cargarRutinasPrehechas() {
        val db = Firebase.firestore
        db.collection("rutinas_prehechas")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar rutinas prehechas: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    listaRutinasPrehechas.clear()
                    for (doc in snapshots) {
                        val rutina = doc.toObject(Rutina::class.java)
                        rutina.id = doc.id
                        listaRutinasPrehechas.add(rutina)
                    }
                    rutinaPrehechaAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun mostrarDialogoEliminarRutina(rutina: Rutina) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar rutina")
            .setMessage("¿Seguro que quieres eliminar esta rutina?")
            .setPositiveButton("Eliminar") { _, _ ->
                Firebase.firestore.collection("rutinas").document(rutina.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Rutina eliminada", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error al eliminar rutina: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}