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

    // Guardamos referencias de fragmentos
    private var firstFragment: Fragment? = null
    private var secondFragment: Fragment? = null
    private var thirdFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        if (auth.currentUser == null) {
            noUser()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.navigationView)

        // Restaurar fragmentos si ya existen, para no recrearlos
        firstFragment = supportFragmentManager.findFragmentByTag("homeFragment") ?: HomeFragment()
        secondFragment = supportFragmentManager.findFragmentByTag("ejerciciosFragment") ?: EjerciciosFragment()
        thirdFragment = supportFragmentManager.findFragmentByTag("perfilFragment") ?: PerfilFragment()

        // Si es la primera vez que crea actividad, muestra el fragmento inicial
        if (savedInstanceState == null) {
            setCurrentFragment(firstFragment!!)
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeNav -> setCurrentFragment(firstFragment!!)
                R.id.ejerciciosNav -> setCurrentFragment(secondFragment!!)
                R.id.perfilNav -> setCurrentFragment(thirdFragment!!)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        // Ocultar todos los fragmentos primero
        listOf(firstFragment, secondFragment, thirdFragment).forEach {
            if (it != null && it.isAdded) transaction.hide(it)
        }

        // Si el fragmento ya está agregado, mostrarlo
        if (fragment.isAdded) {
            transaction.show(fragment)
        } else {
            transaction.add(R.id.fragmentContainerView, fragment, fragmentTag(fragment))
        }

        transaction.commit()
    }

    private fun fragmentTag(fragment: Fragment): String = when (fragment) {
        is HomeFragment -> "homeFragment"
        is EjerciciosFragment -> "ejerciciosFragment"
        is PerfilFragment -> "perfilFragment"
        else -> "unknownFragment"
    }

    fun noUser() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}