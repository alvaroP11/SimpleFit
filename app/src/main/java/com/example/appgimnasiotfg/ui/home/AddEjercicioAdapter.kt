package com.example.appgimnasiotfg.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appgimnasiotfg.databinding.ItemEjercicioSeleccionableBinding
import com.example.appgimnasiotfg.ui.model.Ejercicio

class AddEjercicioAdapter(private val lista: List<Ejercicio>) :
    RecyclerView.Adapter<AddEjercicioAdapter.ViewHolder>() {

    private val seleccionados = mutableSetOf<String>() // ids de ejercicios seleccionados

    fun obtenerSeleccionados(): List<Ejercicio> =
        lista.filter { seleccionados.contains(it.id) }

    inner class ViewHolder(val binding: ItemEjercicioSeleccionableBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ejercicio: Ejercicio) {
            binding.nombreEjercicioTV.text = ejercicio.nombre
            Glide.with(binding.root).load(ejercicio.imagen).into(binding.imagenEjercicioSeleccionable)
            binding.itemCB.setOnCheckedChangeListener(null)
            binding.itemCB.isChecked = seleccionados.contains(ejercicio.id)

            binding.itemCB.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    seleccionados.add(ejercicio.id)
                } else {
                    seleccionados.remove(ejercicio.id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEjercicioSeleccionableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lista[position])
    }
}
