package com.example.appgimnasiotfg.ui.model

import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class EjercicioRutina(
    var ejercicioId: String = "",
    var series: Int = 0,
    var repeticiones: Int = 0,
    var peso: Double = 0.0,
    var tiempo: Int = 0,
    @get:Exclude var ejercicio: Ejercicio? = null,
    @Transient var realizadoTemporal: Boolean = false
) : Serializable

