package com.example.appgimnasiotfg.ui.perfil

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.appgimnasiotfg.databinding.FragmentPerfilBinding
import com.example.appgimnasiotfg.ui.login.LoginActivity
import com.example.appgimnasiotfg.ui.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class PerfilFragment : Fragment() {

    private lateinit var binding: FragmentPerfilBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var usuarioActual: Usuario? = null
    private val userUID = auth.currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logOutBt.setOnClickListener {
            logOut()
        }

        binding.editarUsuarioBT.setOnClickListener {
            val usuario = usuarioActual ?: Usuario()
            val intent = Intent(requireContext(), EditarPerfilActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }


        binding.imcUsuarioObtenidoTV.setOnClickListener {
            usuarioActual?.let {
                mostrarInfoIMC(calcularIMC(it.altura, it.peso))
            }
        }

        binding.indicadorIMC.setOnClickListener {
            usuarioActual?.let {
                mostrarInfoIMC(calcularIMC(it.altura, it.peso))
            }
        }

        binding.correoTV.text = auth.currentUser?.email ?: ""
        cargarDatosUsuario()
    }

    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
    }

    private fun logOut() {
        auth.signOut()
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun cargarDatosUsuario() {
        userUID?.let { uid ->
            db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    doc.toObject(Usuario::class.java)?.let { usuario ->
                        usuarioActual = usuario

                        binding.nombreUsuarioObtenidoTV.text = usuario.nombre
                        binding.alturaUsuarioObtenidaTV.text = formatearAltura(usuario.altura)

                        val pesoFormateado = String.format(Locale.getDefault(), "%.1f", usuario.peso)
                        binding.pesoUsuarioObtenidoTV.text = "$pesoFormateado kg"

                        val imc = calcularIMC(usuario.altura, usuario.peso)
                        binding.imcUsuarioObtenidoTV.text = String.format(Locale.getDefault(), "%.1f", imc)
                        colorearIMC(binding.imcUsuarioObtenidoTV, imc)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun calcularIMC(alturaCm: Int, pesoKg: Double): Double {
        val alturaM = alturaCm / 100f
        return pesoKg / (alturaM * alturaM)
    }

    private fun colorearIMC(imcView: TextView, imc: Double) {
        val color = when {
            imc < 18.5 -> Color.parseColor("#2196F3") // Azul
            imc < 25 -> Color.parseColor("#4CAF50")   // Verde
            imc < 30 -> Color.parseColor("#FFC107")   // Amarillo
            else -> Color.parseColor("#F44336")       // Rojo
        }
        binding.indicadorIMC.background.setTint(color)
    }

    // Si la altura es 1 metro o superior, se formatea en metros, sino, en centimetros
    private fun formatearAltura(alturaCm: Int): String {
        return if (alturaCm >= 100) {
            val alturaMetros = alturaCm / 100f
            String.format(Locale.getDefault(), "%.2f m", alturaMetros)
        } else {
            "$alturaCm cm"
        }
    }

    private fun mostrarInfoIMC(imc: Double) {
        val (categoria, mensaje) = obtenerCategoriaIMC(imc)

        AlertDialog.Builder(requireContext())
            .setTitle("Clasificación IMC: $categoria")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun obtenerCategoriaIMC(imc: Double): Pair<String, String> {
        return when {
            imc < 18.5 -> "Bajo peso" to "Tu IMC indica que estás por debajo del peso recomendado."
            imc < 25 -> "Peso normal" to "Tu IMC está dentro del rango saludable."
            imc < 30 -> "Sobrepeso" to "Tu IMC indica un peso superior al recomendado."
            else -> "Obesidad" to "Tu IMC indica obesidad. Considera consultar con un especialista."
        }
    }
}