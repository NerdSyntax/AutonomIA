package com.nerdsyntax.juntalucas.feature.profile.ui

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val businessName: String = "",
    val rubro: String = "",
    val metaMensual: String = "",
    val moneda: String = "",
    val notificacionesEnabled: Boolean = true,
    val biometriaEnabled: Boolean = false,
    val isUploadingPhoto: Boolean = false,

    // ── Edición ──
    val isEditingUser: Boolean = false,
    val editNameInput: String = "",
    val isEditingBusiness: Boolean = false,
    val editBusinessNameInput: String = "",
    val editRubroInput: String = "",
    val editMetaInput: String = "",
    val isSaving: Boolean = false,
    val statusMessage: String? = null
)