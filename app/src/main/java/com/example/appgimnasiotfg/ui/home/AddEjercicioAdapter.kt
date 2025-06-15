package com.example.appgimnasiotfg.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appgimnasiotfg.databinding.ItemEjercicioSeleccionableBinding
import com.example.appgimnasiotfg.ui.model.Ejercicio

class AddEjercicioAdapter(
    listaEjerciciosInicial: List<Ejercicio>
) : RecyclerView.Adapter<AddEjercicioAdapter.ViewHolder>() {

    // Lista completa de ejercicios (sin filtrar)
    private val listaCompletaEjercicios = listaEjerciciosInicial.toMutableList()

    // Lista filtrada (visible en RecyclerView)
    private var listaEjercicios = listaEjerciciosInicial.toMutableList()

    private val selectedIds = mutableSetOf<String>()

    fun actualizarLista(nuevaLista: List<Ejercicio>) {
        listaEjercicios = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    // Filtra los ejercicios por músculo y actualiza la lista visible
    fun filtrarPorMusculo(musculoId: String) {
        listaEjercicios = if (musculoId.isEmpty()) {
            listaCompletaEjercicios.toMutableList()
        } else {
            listaCompletaEjercicios.filter { it.musculoIds.contains(musculoId) }.toMutableList()
        }
        notifyDataSetChanged()
    }

    // Devuelve la lista completa de ejercicios (sin filtrar)
    fun obtenerListaCompleta(): List<Ejercicio> = listaCompletaEjercicios.toList()

    // Devuelve los ejercicios seleccionados (incluso si no están filtrados/visibles)
    fun obtenerSeleccionados(): List<Ejercicio> {
        return listaCompletaEjercicios.filter { selectedIds.contains(it.id) }
    }

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
