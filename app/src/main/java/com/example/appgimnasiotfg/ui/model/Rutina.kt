package com.example.appgimnasiotfg.ui.model

import java.io.Serializable

data class Rutina(
    var id: String = "",
    var nombreRutina: String = "",
    var usuarioId: String = ""
) : Serializable
