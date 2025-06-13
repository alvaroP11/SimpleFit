package com.example.appgimnasiotfg.ui.perfil

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appgimnasiotfg.databinding.ActivityEditarPerfilBinding
import com.example.appgimnasiotfg.ui.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

class EditarPerfilActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val userUID = auth.currentUser?.uid
    private lateinit var binding: ActivityEditarPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val usuario = intent.getSerializableExtra("usuario") as? Usuario
        usuario?.let {
            binding.nombreUsuarioEdt.setText(it.nombre)
            binding.alturaUsuarioEdt.setText(it.altura.toString())
            binding.pesoUsuarioEdt.setText(it.peso.toString())
        }


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.guardarCambiosUsuarioBT.setOnClickListener {
            guardarCambiosUsuario()
        }
    }

    private fun guardarCambiosUsuario() {
        val nombre = binding.nombreUsuarioEdt.text.toString()
        val altura = binding.alturaUsuarioEdt.text.toString().toIntOrNull() ?: 0
        val peso = parseFloatSafe(binding.pesoUsuarioEdt.text.toString())

        if (userUID != null) {
            val usuario = Usuario(nombre, altura, peso)

            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userUID)
                .set(usuario)
                .addOnSuccessListener {
                    finish()
                }
        }
    }

    fun parseFloatSafe(input: String): Float {
        return try {
            val format = NumberFormat.getInstance(Locale.getDefault())
            val number = format.parse(input)
            number?.toFloat() ?: 0f
        } catch (e: ParseException) {
            0f
        }
    }

}