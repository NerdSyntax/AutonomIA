package com.nerdsyntax.juntalucas.feature.profile.ui

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val businessName: String = "",
    val rubro: String = "",
    val metaMensual: String = "",
    val moneda: String = "",
    val notificacionesEnabled: Boolean = true,
    val biometriaEnabled: Boolean = false
)