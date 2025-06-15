package com.example.appgimnasiotfg.ui.ejercicios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appgimnasiotfg.R
import com.example.appgimnasiotfg.ui.model.Musculo

class MusculoAdapter (
    private val musculos: MutableList<Musculo>
) : RecyclerView.Adapter<MusculoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreMusculoTV: TextView = itemView.findViewById(R.id.nombreMusculoTV)
        val imagenMusculo: ImageView = itemView.findViewById(R.id.imagenMusculo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_musculo, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val musculo = musculos[position]
        holder.nombreMusculoTV.text = musculo.nombre
        Glide.with(holder.itemView.context).load(musculo.imagen).into(holder.imagenMusculo)

    }

    override fun getItemCount() = musculos.size
}