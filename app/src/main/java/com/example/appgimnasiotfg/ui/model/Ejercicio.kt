package com.example.appgimnasiotfg.ui.model

import java.io.Serializable

data class Ejercicio (
    var id: String = "",
    var nombre: String = "",
    var descripcion: String = "",
    var imagen: String = "",
    var maquinaIds: List<String> = ArrayList(),
    var musculoIds: List<String> = ArrayList(),
    var tipo: TipoEjercicio = TipoEjercicio.PESO_VARIABLE
) : Serializable

