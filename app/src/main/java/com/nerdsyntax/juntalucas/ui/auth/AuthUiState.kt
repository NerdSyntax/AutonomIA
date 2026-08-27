package com.nerdsyntax.juntalucas.ui.auth

import com.nerdsyntax.juntalucas.domain.model.AuthUser

data class AuthUiState(
    val currentUser: AuthUser? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)