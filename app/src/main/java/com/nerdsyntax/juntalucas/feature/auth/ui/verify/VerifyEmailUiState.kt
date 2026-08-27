package com.nerdsyntax.juntalucas.feature.auth.ui.verify

import com.nerdsyntax.juntalucas.feature.auth.domain.model.AuthUser

data class VerifyEmailUiState(
    val currentUser: AuthUser? = null,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
