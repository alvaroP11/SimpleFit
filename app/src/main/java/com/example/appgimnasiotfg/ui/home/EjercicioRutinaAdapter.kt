package com.example.appgimnasiotfg.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appgimnasiotfg.databinding.ItemEjercicioRutinaBinding
import com.example.appgimnasiotfg.ui.model.EjercicioRutina
import com.example.appgimnasiotfg.ui.model.TipoEjercicio

class EjercicioRutinaAdapter(
    private var listaEjercicios: MutableList<EjercicioRutina>,
    private val onItemClick: (EjercicioRutina) -> Unit,
    private val onEditarClick: (EjercicioRutina) -> Unit,
    private val onEliminarClick: (EjercicioRutina) -> Unit,
    private val isEditable: Boolean
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

        if (!isEditable) {
            holder.binding.btnEditar.visibility = View.GONE
            holder.binding.btnEliminar.visibility = View.GONE
        }

        with(holder.binding) {
            if (ejercicio != null) {
                nombreEjercicioEditTV.text = ejercicio.nombre
                descripcionEjercicioTV.text = ejercicio.descripcion

                val tipo = ejercicio.tipo
                val info = when (tipo) {
                    TipoEjercicio.PESO_VARIABLE -> {
                        if (isEditable) {
                            "${ejercicioRutina.series} x ${ejercicioRutina.repeticiones}, Peso: ${ejercicioRutina.peso}kg"
                        } else {
                            "${ejercicioRutina.series} x ${ejercicioRutina.repeticiones}"
                        }
                    }TipoEjercicio.PESO_PROPIO -> "${ejercicioRutina.series} x ${ejercicioRutina.repeticiones}"
                    TipoEjercicio.TIEMPO -> "${ejercicioRutina.tiempo} minutos"
                }
                infoEjercicioTV.text = info
            } else {
                nombreEjercicioEditTV.text = "Ejercicio no cargado"
                descripcionEjercicioTV.text = ""
                infoEjercicioTV.text = ""
            }

            root.setOnClickListener {
                onItemClick(ejercicioRutina)
            }

            btnEditar.setOnClickListener {
                onEditarClick(ejercicioRutina)
            }

            btnEliminar.setOnClickListener {
                onEliminarClick(ejercicioRutina)
            }

            checkRealizado.setOnCheckedChangeListener(null)
            checkRealizado.isChecked = ejercicioRutina.realizadoTemporal

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

    // Cambia la opacidad del ejercicio segun si está marcado el Check
    private fun actualizarEstiloItem(realizado: Boolean, binding: ItemEjercicioRutinaBinding) {
        if (realizado) {
            binding.root.alpha = 0.4f // Opacidad atenuada
        } else {
            binding.root.alpha = 1.0f // Opacidad completa
        }
    }

}