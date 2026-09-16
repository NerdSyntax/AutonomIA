package com.nerdsyntax.juntalucas.core.session

import com.nerdsyntax.juntalucas.feature.auth.domain.model.AuthUser

enum class BusinessSetupStatus { CHECKING, REQUIRED, COMPLETE, ERROR }

data class SessionUiState(
    val currentUser: AuthUser? = null,
    val businessSetupStatus: BusinessSetupStatus = BusinessSetupStatus.CHECKING,
    val errorMessage: String? = null
) {
    val isAuthenticated: Boolean get() = currentUser != null
    val isEmailVerified: Boolean get() = currentUser?.isEmailVerified == true
    val canAccessDashboard: Boolean get() = isEmailVerified && businessSetupStatus == BusinessSetupStatus.COMPLETE
}
