package com.example.appgimnasiotfg.ui.home

import android.app.AlertDialog
import android.content.Intent
import androidx.fragment.app.viewModels
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

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding
    private val auth = FirebaseAuth.getInstance()

    private lateinit var adapter: RutinaAdapter
    private val listaRutinas = mutableListOf<Rutina>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rutinasRV.layoutManager = LinearLayoutManager(requireContext())
        adapter = RutinaAdapter(
            listaRutinas,
            onItemClick = { rutina ->
                val intent = Intent(requireContext(), RutinaActivity::class.java)
                intent.putExtra("rutina", rutina)
                startActivity(intent)
            },
            onLongClick = { rutina ->
                mostrarDialogoEliminarRutina(rutina)
            }
        )

        binding.rutinasRV.adapter = adapter

        //cargarRutinasDelUsuario()
        escucharRutinasDelUsuario()
        binding.rutinasRV.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )


        binding.floatingActionButton.setOnClickListener {
            mostrarDialogCrearRutina()
        }
    }
    // Mostrar el dialog
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
                    Toast.makeText(requireContext(), "Rutina creada correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al crear rutina: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    //Actualiza dinamicamente la lista de rutinas si se añade una
    private fun escucharRutinasDelUsuario() {
        val uid = auth.currentUser?.uid ?: return
        val db = Firebase.firestore

        db.collection("rutinas")
            .whereEqualTo("usuarioId", uid) // ← este es el cambio importante
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Error al escuchar rutinas: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    listaRutinas.clear()
                    for (doc in snapshots) {
                        val rutina = doc.toObject(Rutina::class.java)
                        rutina.id = doc.id
                        listaRutinas.add(rutina)
                    }
                    adapter.notifyDataSetChanged()
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
                        Toast.makeText(requireContext(), "Rutina eliminada", Toast.LENGTH_SHORT).show()
                        // No necesitas llamar a cargarRutinas(), el listener actualizará automáticamente
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error al eliminar rutina: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}