package com.nerdsyntax.juntalucas.core.navigation

import com.nerdsyntax.juntalucas.core.session.BusinessSetupStatus
import com.nerdsyntax.juntalucas.core.session.SessionUiState

internal val onboardingRoutes = setOf("business_info", "activity_selection", "starting_point")
internal val publicRoutes = setOf(Routes.LOGIN, Routes.REGISTER, Routes.FORGOT_PASSWORD, "splash", "welcome")

internal fun sessionDestination(session: SessionUiState, currentRoute: String?): String? = when {
    currentRoute == null -> null
    !session.isAuthenticated -> if (currentRoute !in publicRoutes) "welcome" else null
    !session.isEmailVerified -> Routes.VERIFY_EMAIL
    session.businessSetupStatus == BusinessSetupStatus.CHECKING ||
        session.businessSetupStatus == BusinessSetupStatus.ERROR -> "session_check"
    session.businessSetupStatus == BusinessSetupStatus.REQUIRED ->
        if (currentRoute in onboardingRoutes) null else "business_info"
    session.canAccessDashboard ->
        if (currentRoute in publicRoutes || currentRoute in onboardingRoutes ||
            currentRoute == Routes.VERIFY_EMAIL || currentRoute == "session_check") Routes.DASHBOARD else null
    else -> "session_check"
}
