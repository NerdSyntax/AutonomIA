package com.nerdsyntax.juntalucas.feature.business.domain

data class Suscripcion(
    val plan: String = "FREE",
    val estado: String = "ACTIVO",
    val consultasIaUsadas: Int = 0,
    val fechaReinicio: String = ""
)

data class Business(
    val nombreNegocio: String = "",
    val rubro: String = "",
    val region: String = "",
    val comuna: String = "",
    val metaMensual: Long = 0,
    val tipoActividad: String = "",
    val puntoPartida: String = "manual",
    val onboardingCompleted: Boolean = false,
    val nombreDueno: String = "",
    val photoBase64: String = "",
    val suscripcion: Suscripcion = Suscripcion()
)