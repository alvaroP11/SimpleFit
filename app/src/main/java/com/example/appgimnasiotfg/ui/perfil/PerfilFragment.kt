package com.example.appgimnasiotfg.ui.perfil

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.appgimnasiotfg.R
import com.example.appgimnasiotfg.databinding.FragmentHomeBinding
import com.example.appgimnasiotfg.databinding.FragmentPerfilBinding
import com.example.appgimnasiotfg.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class PerfilFragment : Fragment() {

    companion object {
        fun newInstance() = PerfilFragment()
    }

    private val viewModel: PerfilViewModel by viewModels()
    private lateinit var binding: FragmentPerfilBinding
    private val auth = FirebaseAuth.getInstance()

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

        binding.correoTV.setText(auth.currentUser?.email.toString())
    }

    fun logOut() {
        auth.signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}