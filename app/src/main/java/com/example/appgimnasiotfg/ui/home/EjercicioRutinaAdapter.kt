package com.example.appgimnasiotfg.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appgimnasiotfg.databinding.ItemEjercicioRutinaBinding
import com.example.appgimnasiotfg.ui.model.EjercicioRutina
import com.example.appgimnasiotfg.ui.model.TipoEjercicio

class EjercicioRutinaAdapter(
    private var listaEjercicios: MutableList<EjercicioRutina>,
    private val onItemClick: (EjercicioRutina) -> Unit,
    private val onEditarClick: (EjercicioRutina) -> Unit,
    private val onEliminarClick: (EjercicioRutina) -> Unit
) : RecyclerView.Adapter<EjercicioRutinaAdapter.EjercicioViewHolder>() {

    inner class EjercicioViewHolder(val binding: ItemEjercicioRutinaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioViewHolder {
        val binding = ItemEjercicioRutinaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EjercicioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EjercicioViewHolder, position: Int) {
        val ejercicioRutina = listaEjercicios[position]
        val ejercicio = ejercicioRutina.ejercicio

        with(holder.binding) {
            if (ejercicio != null) {
                nombreEjercicioEditTV.text = ejercicio.nombre
                descripcionEjercicioTV.text = ejercicio.descripcion

                val tipo = ejercicio.tipo
                val info = when (tipo) {
                    TipoEjercicio.PESO_VARIABLE -> "${ejercicioRutina.series} x ${ejercicioRutina.repeticiones}, Peso: ${ejercicioRutina.peso}kg"
                    TipoEjercicio.PESO_PROPIO -> "${ejercicioRutina.series} x ${ejercicioRutina.repeticiones}"
                    TipoEjercicio.TIEMPO -> "${ejercicioRutina.tiempo} minutos"
                }
                infoEjercicioTV.text = info
            } else {
                nombreEjercicioEditTV.text = "Ejercicio no cargado"
                descripcionEjercicioTV.text = ""
                infoEjercicioTV.text = ""
            }

            // Ir a EjercicioActivity (detalle)
            root.setOnClickListener {
                onItemClick(ejercicioRutina)
            }

            // Ir a editar series/reps/peso
            btnEditar.setOnClickListener {
                onEditarClick(ejercicioRutina)
            }

            // Eliminar ejercicio de la rutina
            btnEliminar.setOnClickListener {
                onEliminarClick(ejercicioRutina)
            }

            checkRealizado.setOnCheckedChangeListener(null) // Evitar triggers reciclados

            checkRealizado.isChecked = ejercicioRutina.realizadoTemporal ?: false

            actualizarEstiloItem(checkRealizado.isChecked, this)

            checkRealizado.setOnCheckedChangeListener { _, isChecked ->
                ejercicioRutina.realizadoTemporal = isChecked
                actualizarEstiloItem(isChecked, this)
            }
        }
    }

    override fun getItemCount(): Int = listaEjercicios.size

    fun updateData(nuevosEjercicios: List<EjercicioRutina>) {
        this.listaEjercicios.clear()
        this.listaEjercicios.addAll(nuevosEjercicios)
        notifyDataSetChanged()
    }

    private fun actualizarEstiloItem(realizado: Boolean, binding: ItemEjercicioRutinaBinding) {
        if (realizado) {
            binding.root.alpha = 0.4f // Atenuar
            // También puedes hacer esto:
            // binding.nombreEjercicioTV.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            binding.root.alpha = 1.0f
            // binding.nombreEjercicioTV.paintFlags = 0
        }
    }

}