package com.example.appgimnasiotfg.ui.model

import java.io.Serializable

data class Usuario(
    var nombre: String = "",
    var altura: Int = 0,
    var peso: Float = 0f
) : Serializable