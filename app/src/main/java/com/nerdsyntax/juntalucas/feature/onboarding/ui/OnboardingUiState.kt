package com.nerdsyntax.juntalucas.feature.onboarding.ui

data class OnboardingUiState(
    val nombreNegocio: String = "",
    val rubro: String = "",
    val region: String = "",
    val comuna: String = "",
    val metaMensual: String = "",
    val tipoActividad: String = "ambos",
    val puntoPartida: String = "manual",
    val planSuscripcion: String = "FREE",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)