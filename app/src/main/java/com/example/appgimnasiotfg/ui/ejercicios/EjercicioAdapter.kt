package com.example.appgimnasiotfg.ui.ejercicios

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.view.LayoutInflater
import android.widget.ImageView
import com.example.appgimnasiotfg.R
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appgimnasiotfg.ui.model.Ejercicio

class EjercicioAdapter(
    private val ejercicios: MutableList<Ejercicio>,
    private val onItemClick: (Ejercicio) -> Unit
) : RecyclerView.Adapter<EjercicioAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreText: TextView = itemView.findViewById(R.id.nombreEjercicioTV)
        val descripcionText: TextView = itemView.findViewById(R.id.descripcionEjercicioTV)
        val imagen: ImageView = itemView.findViewById(R.id.imagenEjercicio)

        init {
            itemView.setOnClickListener {
                val posicion = adapterPosition
                if (posicion != RecyclerView.NO_POSITION) {
                    onItemClick(ejercicios[posicion])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ejercicio, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ejercicio = ejercicios[position]
        holder.nombreText.text = ejercicio.nombre
        holder.descripcionText.text = ejercicio.descripcion
        Glide.with(holder.itemView.context).load(ejercicio.imagen).into(holder.imagen)

    }

    override fun getItemCount() = ejercicios.size

    fun setEjercicios(nuevosEjercicios: List<Ejercicio>) {
        ejercicios.clear()
        ejercicios.addAll(nuevosEjercicios)
        notifyDataSetChanged()
    }
}
