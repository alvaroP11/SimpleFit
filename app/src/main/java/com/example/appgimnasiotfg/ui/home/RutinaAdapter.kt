package com.example.appgimnasiotfg.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.appgimnasiotfg.databinding.ItemRutinaBinding
import com.example.appgimnasiotfg.ui.model.Rutina
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RutinaAdapter(
    private var lista: MutableList<Rutina>,
    private val onItemClick: (Rutina) -> Unit,
    private val onLongClick: (Rutina) -> Unit,
    private val editable: Boolean = true
) : RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder>() {

    inner class RutinaViewHolder(val binding: ItemRutinaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutinaViewHolder {
        val binding = ItemRutinaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RutinaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RutinaViewHolder, position: Int) {
        val rutina = lista[position]
        holder.binding.nombreRutinaTV.text = rutina.nombreRutina

        holder.itemView.setOnClickListener {
            onItemClick(rutina)
        }

        if (editable) {
            holder.itemView.setOnLongClickListener {
                onLongClick(rutina)
                true
            }
        }
    }

    override fun getItemCount(): Int = lista.size

    fun updateData(nuevasRutinas: List<Rutina>) {
        lista.clear()
        lista.addAll(nuevasRutinas)
        notifyDataSetChanged()
    }
}
