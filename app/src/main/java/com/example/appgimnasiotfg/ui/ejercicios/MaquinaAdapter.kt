package com.example.appgimnasiotfg.ui.ejercicios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appgimnasiotfg.R
import com.example.appgimnasiotfg.ui.model.Maquina

class MaquinaAdapter (
    private val maquinas: MutableList<Maquina>,
    private val onItemClick: (Maquina) -> Unit
) : RecyclerView.Adapter<MaquinaAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreMaquinaTV: TextView = itemView.findViewById(R.id.nombreMaquinaTV)
        val imagenMaquina: ImageView = itemView.findViewById(R.id.imagenMaquina)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_maquina, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val maquina = maquinas[position]
        holder.nombreMaquinaTV.text = maquina.nombre
        Glide.with(holder.itemView.context).load(maquina.imagen).into(holder.imagenMaquina)

    }

    override fun getItemCount() = maquinas.size
}