package com.example.appgimnasiotfg.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appgimnasiotfg.databinding.ItemEjercicioBinding
import com.example.appgimnasiotfg.databinding.ItemEjercicioSeleccionableBinding
import com.example.appgimnasiotfg.ui.model.Ejercicio

class AddEjercicioAdapter(
    private var listaEjercicios: List<Ejercicio>
) : RecyclerView.Adapter<AddEjercicioAdapter.ViewHolder>() {

    private val selectedIds = mutableSetOf<String>()

    fun actualizarLista(nuevaLista: List<Ejercicio>) {
        listaEjercicios = nuevaLista
        notifyDataSetChanged()
    }

    fun isSelected(id: String): Boolean = selectedIds.contains(id)

    fun getSelectedIds(): Set<String> = selectedIds.toSet()

    inner class ViewHolder(private val binding: ItemEjercicioSeleccionableBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ejercicio: Ejercicio) {
            binding.nombreEjercicioEditTV.text = ejercicio.nombre
            Glide.with(binding.root).load(ejercicio.imagen).into(binding.imagenEjercicioSeleccionable)
            // Evitar llamada repetida del listener al reciclar
            binding.itemCB.setOnCheckedChangeListener(null)
            binding.itemCB.isChecked = selectedIds.contains(ejercicio.id)
            binding.itemCB.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedIds.add(ejercicio.id) else selectedIds.remove(ejercicio.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEjercicioSeleccionableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listaEjercicios[position])
    }

    override fun getItemCount(): Int = listaEjercicios.size
}
