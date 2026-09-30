package com.nerdsyntax.juntalucas.core.navigation

import com.nerdsyntax.juntalucas.core.session.BusinessSetupStatus
import com.nerdsyntax.juntalucas.core.session.SessionUiState
import com.nerdsyntax.juntalucas.feature.auth.domain.model.AuthUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionNavigationTest {
    private val verifiedUser = AuthUser("user", "user@example.com", true)
    private val protectedRoutes = listOf(
        Routes.DASHBOARD, Routes.MOVEMENTS, Routes.BUSINESS, Routes.AI,
        Routes.PROFILE, Routes.ACCOUNT, Routes.ADD_SALE, Routes.ADD_EXPENSE, Routes.ADD_PRODUCT
    )

    @Test fun signedOutUsersCanBrowsePublicScreensButCannotOpenProtectedScreens() {
        val session = SessionUiState()
        publicRoutes.forEach { assertNull(sessionDestination(session, it)) }
        (protectedRoutes + onboardingRoutes + Routes.VERIFY_EMAIL + Routes.SESSION_CHECK).forEach {
            assertEquals(Routes.WELCOME, sessionDestination(session, it))
        }
    }

    @Test fun unverifiedUsersMustVerifyBeforeOnboardingOrProtectedScreens() {
        val session = SessionUiState(verifiedUser.copy(isEmailVerified = false))
        (protectedRoutes + onboardingRoutes).forEach {
            assertEquals(Routes.VERIFY_EMAIL, sessionDestination(session, it))
        }
    }

    @Test fun pendingAndFailedBusinessChecksKeepProtectedScreensClosed() {
        listOf(BusinessSetupStatus.CHECKING, BusinessSetupStatus.ERROR).forEach { status ->
            val session = SessionUiState(verifiedUser, status)
            protectedRoutes.forEach {
                assertEquals(Routes.SESSION_CHECK, sessionDestination(session, it))
            }
        }
    }

    @Test fun incompleteBusinessKeepsCurrentOnboardingStepAndRedirectsProtectedScreens() {
        val session = SessionUiState(verifiedUser, BusinessSetupStatus.REQUIRED)
        onboardingRoutes.forEach { assertNull(sessionDestination(session, it)) }
        protectedRoutes.forEach {
            assertEquals(Routes.BUSINESS_INFO, sessionDestination(session, it))
        }
    }

    @Test fun completeSessionKeepsProtectedDestinationsAndLeavesSetupScreens() {
        val session = SessionUiState(verifiedUser, BusinessSetupStatus.COMPLETE)
        protectedRoutes.forEach { assertNull(sessionDestination(session, it)) }
        (publicRoutes + onboardingRoutes + Routes.VERIFY_EMAIL + Routes.SESSION_CHECK).forEach {
            assertEquals(Routes.DASHBOARD, sessionDestination(session, it))
        }
    }

    @Test fun missingBackStackEntryDoesNotTriggerNavigation() {
        assertNull(sessionDestination(SessionUiState(), null))
    }
}
