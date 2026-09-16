package com.nerdsyntax.juntalucas.feature.business.domain

data class Business(
    val nombreNegocio: String = "",
    val rubro: String = "",
    val region: String = "",
    val comuna: String = "",
    val metaMensual: Long = 0,
    val tipoActividad: String = "",
    val puntoPartida: String = "manual",
    val onboardingCompleted: Boolean = false
)
