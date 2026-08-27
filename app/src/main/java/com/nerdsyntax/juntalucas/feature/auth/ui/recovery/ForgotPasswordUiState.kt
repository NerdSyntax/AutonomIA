package com.nerdsyntax.juntalucas.feature.auth.ui.recovery

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
