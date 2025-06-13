package com.example.appgimnasiotfg.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appgimnasiotfg.R
import com.example.appgimnasiotfg.databinding.ActivityLoginBinding
import com.example.appgimnasiotfg.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding      // VINCULACION DE VISTAS, SIMPLIFICA EL ACCESO A LOS ELEMENTOS DEL LAYOUT
    private lateinit var loginManager: GoogleLoginManager   // CLASE AUXILIAR QUE ENCAPSULA EL CODIGO RELACIONADO CON EL LOGIN DE GOOGLE
    private val auth = FirebaseAuth.getInstance()           // SE SINCRONIZA EN TODOS LOS ACTIVITIES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // --- INICIALIZACIÓN DEL LOGIN EN EL METODO onCreate, DE LO CONTRARIO NO FUNCIONA --- //
        loginManager = GoogleLoginManager(
            activity = this,
            activityResultCaller = this,
            auth = auth,
            // ID en un string, para que no haya visibilidad directa al manipular este fichero
            webClientId = getString(R.string.web_client_id),
            onSuccess = { email ->
                Toast.makeText(this, "Bienvenido $email", Toast.LENGTH_SHORT).show()
                // SI EL LOGIN ES EXITOSO, SE REALIZA EL INTENT
                loginSuccess()
            },
            onError = { error ->
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        )
        // --- //

        // Comprobacion de que haya ya una sesión valida, para omitir el proceso de Login y pasar directamente a la aplicación
        if (auth.currentUser != null) {
            loginSuccess()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun signUp(view: View) {
        val email = binding.emailEdt.text.toString()
        val password = binding.passwordEdt.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Hay algún campo vacío", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance()
            .fetchSignInMethodsForEmail(email)
            .addOnSuccessListener { result ->
                val methods = result.signInMethods ?: emptyList()

                if (methods.isEmpty()) {
                    // No existe otra cuenta con ese correo
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                loginSuccess()
                            } else {
                                Toast.makeText(this, "Error durante el registro.", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // Ya existe una cuenta con ese correo
                    Toast.makeText(
                        this,
                        "Ya existe una cuenta con ese correo (${methods.joinToString()}). Inicia sesión y vincula si deseas.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al comprobar correo: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    fun login(view: View) {
        val email = binding.emailEdt.text.toString()
        val password = binding.passwordEdt.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Hay algun campo vacio", Toast.LENGTH_SHORT).show()
        } else {
            auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        loginSuccess()
                    } else {
                        Toast.makeText(this,"La cuenta introducida no existe.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun googleLogin(view: View) {
        loginManager.startLogin()
    }

    fun loginSuccess() {
        val intent = Intent(this, MainActivity::class.java)
        // Se borra el anterior Activity del historial, para evitar acceder al Activity anterior en un estado incorrecto
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}