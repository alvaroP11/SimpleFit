package com.example.appgimnasiotfg.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
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
    private var isPasswordVisible = false

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

        binding.alRegistroTV.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Comprobacion de que haya ya una sesión valida, para omitir el proceso de Login y pasar directamente a la aplicación
        if (auth.currentUser != null) {
            loginSuccess()
        }

        configurarTogglePassword(binding.passwordEdt)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun login(view: View) {
        val email = binding.emailEdt.text.toString().trim()
        val password = binding.passwordEdt.text.toString().trim()

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

    private fun loginSuccess() {
        val intent = Intent(this, MainActivity::class.java)
        // Se borra el anterior Activity del historial, para evitar acceder al Activity anterior en un estado incorrecto
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Boton "ojo" para alternar el poder ver o no la contraseña
    @SuppressLint("ClickableViewAccessibility")
    private fun configurarTogglePassword(editText: EditText) {
        editText.setOnTouchListener { v, event ->
            val drawableEnd = 2
            if (event.action == MotionEvent.ACTION_UP) {
                val drawable = editText.compoundDrawables[drawableEnd]
                if (drawable != null && event.rawX >= (editText.right - drawable.bounds.width())) {
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0)
                        editText.typeface = ResourcesCompat.getFont(this,R.font.alatsi)
                    } else {
                        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0)
                        editText.typeface = ResourcesCompat.getFont(this,R.font.alatsi)
                    }
                    editText.setSelection(editText.text.length)  // Mantiene el cursor al final
                }
            }
            false
        }
    }
}