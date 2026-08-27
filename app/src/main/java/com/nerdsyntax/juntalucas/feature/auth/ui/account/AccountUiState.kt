package com.nerdsyntax.juntalucas.feature.auth.ui.account

import com.nerdsyntax.juntalucas.feature.auth.domain.model.AuthUser

data class AccountUiState(
    val currentUser: AuthUser? = null,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
