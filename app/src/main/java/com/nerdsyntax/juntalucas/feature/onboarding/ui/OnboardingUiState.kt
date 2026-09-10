package com.nerdsyntax.juntalucas.feature.onboarding.ui

data class OnboardingUiState(
    //Datos de negocio
    val nombreNegocio: String = "",
    val rubro: String = "", //falta añadir dropdown
    val region: String = "Región Metropolitana", //falta añadir el dropdown
    val comuna: String = "", //falta añadir dropdown según región
    val metaMensual: String = "",

    //Tipo de actividad
    val tipoActividad: String = "ambos", // Opciones: productos, servicios, ambos

    //Forma de configuración
    val puntoPartida: String = "manual", // Opciones: manual, importar, ejemplo

    //Estado general
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)