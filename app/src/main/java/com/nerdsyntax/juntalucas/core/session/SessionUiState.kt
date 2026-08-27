package com.nerdsyntax.juntalucas.core.session

import com.nerdsyntax.juntalucas.feature.auth.domain.model.AuthUser

data class SessionUiState(val currentUser: AuthUser? = null) {
    val isAuthenticated: Boolean get() = currentUser != null
    val isEmailVerified: Boolean get() = currentUser?.isEmailVerified == true
}
