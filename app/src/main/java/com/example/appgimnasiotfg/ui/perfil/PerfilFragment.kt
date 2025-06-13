package com.example.appgimnasiotfg.ui.perfil

import android.content.Intent
import android.graphics.Color
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.appgimnasiotfg.databinding.FragmentPerfilBinding
import com.example.appgimnasiotfg.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.appgimnasiotfg.ui.model.Usuario
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

class PerfilFragment : Fragment() {

    companion object {
        fun newInstance() = PerfilFragment()
    }

    private val viewModel: PerfilViewModel by viewModels()
    private lateinit var binding: FragmentPerfilBinding
    private val auth = FirebaseAuth.getInstance()
    private val userUID = auth.currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPerfilBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ASIGNO EL METODO logOut() AL BOTON MEDIANTE setOnClickListener, DESDE EL LAYOUT CRASHEA AL INTENTAR BUSCAR EL METODO EN EL ACTIVITY
        binding.logOutBt.setOnClickListener {
            logOut()
        }

        obtenerDatosUsuario()
        binding.correoTV.setText(auth.currentUser?.email.toString())
    }

    fun logOut() {
        auth.signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun obtenerDatosUsuario(){
        if (userUID != null) {
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userUID)
                .get()
                .addOnSuccessListener { doc ->
                    doc.toObject(Usuario::class.java)?.let { usuario ->
                        binding.nombreUsuarioObtenidoTV.text = usuario.nombre
                        binding.alturaUsuarioObtenidaTV.text = formatearAltura(usuario.altura)
                        binding.pesoUsuarioObtenidoTV.text = String.format(Locale.getDefault(), "%.1f kg", usuario.peso)

                        val imc = calcularIMC(usuario.altura, usuario.peso)
                        binding.imcUsuarioObtenidoTV.text = String.format(Locale.getDefault(), "%.1f", imc)
                        colorearIMC(binding.imcUsuarioObtenidoTV, imc)

                        binding.editarUsuarioBT.setOnClickListener {
                            val intent = Intent(requireContext(), EditarPerfilActivity::class.java)
                            intent.putExtra("usuario",usuario)
                            startActivity(intent)
                        }
                    }
                }
        }
    }

    fun calcularIMC(alturaCm: Int, pesoKg: Float): Float {
        val alturaM = alturaCm / 100f
        return pesoKg / (alturaM * alturaM)
    }


    fun colorearIMC(view: TextView, imc: Float) {
        val color = when {
            imc < 18.5 -> Color.parseColor("#2196F3") // Azul - bajo peso
            imc < 25 -> Color.parseColor("#4CAF50") // Verde - normal
            imc < 30 -> Color.parseColor("#FFC107") // Amarillo - sobrepeso
            else -> Color.parseColor("#F44336") // Rojo - obesidad
        }
        view.setBackgroundColor(color)
    }

    fun formatearAltura(alturaCm: Int): String {
        return if (alturaCm >= 100) {
            val alturaMetros = alturaCm / 100f
            String.format(Locale.getDefault(), "%.2f m", alturaMetros)
        } else {
            "$alturaCm cm"
        }
    }

}