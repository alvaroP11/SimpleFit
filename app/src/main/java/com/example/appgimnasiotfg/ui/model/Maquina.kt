package com.example.appgimnasiotfg.ui.model

import java.io.Serializable

data class Maquina(
    var id: String = "",
    var nombre: String = "",
    var descripcion: String = "",
    var imagen: String = ""
) : Serializable