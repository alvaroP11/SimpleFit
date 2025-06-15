package com.example.appgimnasiotfg.ui.home

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.appgimnasiotfg.R

class RutinaFragmentDialog : DialogFragment() {

    interface OnNombreConfirmadoListener {
        fun onNombreConfirmado(nombre: String)
    }

    private var listener: OnNombreConfirmadoListener? = null

    private var tituloDialogo: String = "Crear nueva rutina"
    private var textoBotonPositivo: String = "Crear"
    private var nombreInicial: String? = null
    private val maxLength = 30

    companion object {
        fun newInstance(
            titulo: String,
            textoBoton: String,
            nombreInicial: String? = null
        ): RutinaFragmentDialog {
            val fragment = RutinaFragmentDialog()
            val args = Bundle()
            args.putString("titulo", titulo)
            args.putString("textoBoton", textoBoton)
            args.putString("nombreInicial", nombreInicial)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = when {
            parentFragment is OnNombreConfirmadoListener -> parentFragment as OnNombreConfirmadoListener
            context is OnNombreConfirmadoListener -> context
            else -> null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tituloDialogo = it.getString("titulo") ?: tituloDialogo
            textoBotonPositivo = it.getString("textoBoton") ?: textoBotonPositivo
            nombreInicial = it.getString("nombreInicial")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_nueva_rutina, null)
        val nombreRutinaEDT = view.findViewById<EditText>(R.id.nombreRutinaEDT)

        nombreInicial?.let {
            nombreRutinaEDT.setText(it)
            nombreRutinaEDT.setSelection(it.length)
        }

        builder.setView(view)
            .setTitle(tituloDialogo)
            .setPositiveButton(textoBotonPositivo, null)
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.setOnShowListener {
            val botonPositivo = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            botonPositivo.setOnClickListener {
                val nombre = nombreRutinaEDT.text.toString().trim()
                when {
                    nombre.isEmpty() -> {
                        nombreRutinaEDT.error = "El nombre no puede estar vacío"
                    }
                    nombre.length > maxLength -> {
                        nombreRutinaEDT.error = "Máximo $maxLength caracteres"
                    }
                    else -> {
                        listener?.onNombreConfirmado(nombre)
                        dialog.dismiss()
                    }
                }
            }
        }
        return dialog
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
