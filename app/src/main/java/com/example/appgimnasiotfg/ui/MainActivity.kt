package com.example.appgimnasiotfg.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.appgimnasiotfg.R
import com.example.appgimnasiotfg.ui.ejercicios.EjerciciosFragment
import com.example.appgimnasiotfg.ui.home.HomeFragment
import com.example.appgimnasiotfg.ui.login.LoginActivity
import com.example.appgimnasiotfg.ui.perfil.PerfilFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Si no hay usuario activo válido, Intent al LoginActivity
        if (auth.currentUser == null) {
            noUser()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.navigationView)

        // Mostrar el fragmento inicial solo al iniciar por primera vez
        if (savedInstanceState == null) {
            setCurrentFragment(HomeFragment())
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeNav -> setCurrentFragment(HomeFragment())
                R.id.ejerciciosNav -> setCurrentFragment(EjerciciosFragment())
                R.id.perfilNav -> setCurrentFragment(PerfilFragment())
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment, fragment::class.java.simpleName)
            .commit()
    }

    private fun noUser() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}