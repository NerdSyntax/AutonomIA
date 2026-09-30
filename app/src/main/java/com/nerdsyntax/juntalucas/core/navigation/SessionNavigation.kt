package com.nerdsyntax.juntalucas.core.navigation

import com.nerdsyntax.juntalucas.core.session.BusinessSetupStatus
import com.nerdsyntax.juntalucas.core.session.SessionUiState

internal val onboardingRoutes = setOf(Routes.BUSINESS_INFO, Routes.ACTIVITY_SELECTION, Routes.STARTING_POINT)
internal val publicRoutes = setOf(Routes.LOGIN, Routes.REGISTER, Routes.FORGOT_PASSWORD, Routes.SPLASH, Routes.WELCOME)

internal fun sessionDestination(session: SessionUiState, currentRoute: String?): String? = when {
    currentRoute == null -> null
    !session.isAuthenticated -> if (currentRoute !in publicRoutes) Routes.WELCOME else null
    !session.isEmailVerified -> Routes.VERIFY_EMAIL
    session.businessSetupStatus == BusinessSetupStatus.CHECKING ||
        session.businessSetupStatus == BusinessSetupStatus.ERROR -> Routes.SESSION_CHECK
    session.businessSetupStatus == BusinessSetupStatus.REQUIRED ->
        if (currentRoute in onboardingRoutes) null else Routes.BUSINESS_INFO
    session.canAccessDashboard ->
        if (currentRoute in publicRoutes || currentRoute in onboardingRoutes ||
            currentRoute == Routes.VERIFY_EMAIL || currentRoute == Routes.SESSION_CHECK) Routes.DASHBOARD else null
    else -> Routes.SESSION_CHECK
}
