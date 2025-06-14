package com.example.appgimnasiotfg.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import com.example.appgimnasiotfg.databinding.ActivityRegisterBinding
import com.example.appgimnasiotfg.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        binding.volverAlLoginBT.setOnClickListener{
            finish() //Volver al activity anterior cancelando el Intent
        }

        binding.registerBt.setOnClickListener{
            signUp()
        }

        configurarTogglePassword(binding.passwordRegisterEdt)
        configurarTogglePassword(binding.passwordConfirmEdt)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun signUp() {
        val email = binding.emailRegisterEdt.text.toString().trim()
        val password = binding.passwordRegisterEdt.text.toString().trim()
        val confirmPassword = binding.passwordConfirmEdt.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Hay algún campo vacío", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Bienvenido: ${FirebaseAuth.getInstance().currentUser?.email}", Toast.LENGTH_SHORT).show()
                    registerSuccess()
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            this,
                            "Correo en uso. Si lo registraste con Google, inicia sesión con Google.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(this, "Error: ${exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    fun registerSuccess(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun configurarTogglePassword(editText: EditText) {
        editText.setOnTouchListener { v, event ->
            val drawableEnd = 2  // Posición del drawableEnd
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
                    true
                }
            }
            false
        }
    }
}