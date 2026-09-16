package com.nerdsyntax.juntalucas.core.session

import com.nerdsyntax.juntalucas.feature.auth.domain.model.AuthUser
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.Business
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.nerdsyntax.juntalucas.core.navigation.sessionDestination
import com.nerdsyntax.juntalucas.core.navigation.Routes

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val userA = AuthUser("a", "a@example.com", true)
    private val userB = AuthUser("b", "b@example.com", true)

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private class Auth(user: AuthUser?) : AuthRepository {
        override val currentUser = MutableStateFlow(user)
        override suspend fun loginWithGoogle(idToken: String): Result<AuthUser> = error("Unused")
        override suspend fun login(email: String, password: String): Result<AuthUser> = error("Unused")
        override suspend fun register(email: String, password: String): Result<AuthUser> = error("Unused")
        override suspend fun sendPasswordReset(email: String): Result<Unit> = error("Unused")
        override suspend fun sendEmailVerification(): Result<Unit> = error("Unused")
        override suspend fun reloadCurrentUser(): Result<AuthUser?> = error("Unused")
        override suspend fun deleteCurrentUser(): Result<Unit> = error("Unused")
        override fun logout() { currentUser.value = null }
    }

    private class Repository(val read: suspend () -> Result<Business?>) : BusinessRepository {
        override suspend fun getBusiness() = read()
        override suspend fun saveBusiness(business: Business): Result<Unit> = error("Unused")
    }

    @Test fun verifiedUserMustWaitForValidBusiness() = runTest(dispatcher) {
        val pending = CompletableDeferred<Result<Business?>>()
        val vm = SessionViewModel(Auth(userA), Repository { pending.await() })
        advanceUntilIdle()
        assertEquals(BusinessSetupStatus.CHECKING, vm.uiState.value.businessSetupStatus)
        assertFalse(vm.uiState.value.canAccessDashboard)
        pending.complete(Result.success(Business(nombreNegocio = "Negocio", rubro = "Comercio", tipoActividad = "ambos", onboardingCompleted = true)))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.canAccessDashboard)
    }

    @Test fun absentOrIncompleteBusinessRequiresOnboardingAndRefreshReadsAgain() = runTest(dispatcher) {
        var business: Business? = null
        val vm = SessionViewModel(Auth(userA), Repository { Result.success(business) })
        advanceUntilIdle()
        assertEquals(BusinessSetupStatus.REQUIRED, vm.uiState.value.businessSetupStatus)
        business = Business(nombreNegocio = "Negocio", rubro = "Comercio")
        vm.refreshBusiness()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canAccessDashboard)
        business = business!!.copy(tipoActividad = "servicios", onboardingCompleted = true)
        vm.refreshBusiness()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.canAccessDashboard)
    }

    @Test fun errorDoesNotGrantAccessAndRetryRecovers() = runTest(dispatcher) {
        var fail = true
        val vm = SessionViewModel(Auth(userA), Repository {
            if (fail) Result.failure(IllegalStateException("technical")) else Result.success(null)
        })
        advanceUntilIdle()
        assertEquals(BusinessSetupStatus.ERROR, vm.uiState.value.businessSetupStatus)
        assertFalse(vm.uiState.value.canAccessDashboard)
        assertFalse(vm.uiState.value.errorMessage!!.contains("technical"))
        fail = false
        vm.refreshBusiness()
        advanceUntilIdle()
        assertEquals(BusinessSetupStatus.REQUIRED, vm.uiState.value.businessSetupStatus)
    }

    @Test fun unverifiedAndLoggedOutUsersDoNotReadBusiness() = runTest(dispatcher) {
        val auth = Auth(userA.copy(isEmailVerified = false))
        var reads = 0
        val vm = SessionViewModel(auth, Repository { reads++; Result.success(null) })
        advanceUntilIdle()
        assertEquals(0, reads)
        assertFalse(vm.uiState.value.canAccessDashboard)
        auth.logout()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isAuthenticated)
        assertEquals(0, reads)
    }

    @Test fun switchingAccountsCancelsPendingRead() = runTest(dispatcher) {
        val auth = Auth(userA)
        val pending = CompletableDeferred<Result<Business?>>()
        val vm = SessionViewModel(auth, Repository {
            if (auth.currentUser.value?.uid == "a") pending.await() else Result.success(null)
        })
        advanceUntilIdle()
        auth.currentUser.value = userB
        advanceUntilIdle()
        pending.complete(Result.success(Business(nombreNegocio = "A", rubro = "Comercio", tipoActividad = "ambos", onboardingCompleted = true)))
        advanceUntilIdle()
        assertEquals(userB, vm.uiState.value.currentUser)
        assertEquals(BusinessSetupStatus.REQUIRED, vm.uiState.value.businessSetupStatus)
    }
    @Test fun reopeningAfterVerificationWithoutBusinessAlwaysGoesToOnboarding() = runTest(dispatcher) {
        val repository = Repository { Result.success(null) }
        val auth = Auth(userA.copy(isEmailVerified = false))
        val firstSession = SessionViewModel(auth, repository)
        advanceUntilIdle()
        assertEquals(Routes.VERIFY_EMAIL, sessionDestination(firstSession.uiState.value, Routes.LOGIN))
        auth.currentUser.value = userA
        advanceUntilIdle()
        assertEquals("business_info", sessionDestination(firstSession.uiState.value, Routes.VERIFY_EMAIL))
        val reopenedSession = SessionViewModel(Auth(userA), repository)
        assertEquals("session_check", sessionDestination(reopenedSession.uiState.value, Routes.DASHBOARD))
        advanceUntilIdle()
        for (route in listOf(Routes.LOGIN, "session_check", Routes.DASHBOARD)) {
            assertEquals("business_info", sessionDestination(reopenedSession.uiState.value, route))
        }
        assertFalse(reopenedSession.uiState.value.canAccessDashboard)
    }

    @Test fun savedBusinessOnlyGoesToDashboardWithCompletionFlag() = runTest(dispatcher) {
        var business = Business(nombreNegocio = "Negocio", rubro = "Comercio", tipoActividad = "ambos")
        val vm = SessionViewModel(Auth(userA), Repository { Result.success(business) })
        advanceUntilIdle()
        assertEquals("business_info", sessionDestination(vm.uiState.value, Routes.LOGIN))
        business = business.copy(onboardingCompleted = true)
        val reopenedSession = SessionViewModel(Auth(userA), Repository { Result.success(business) })
        assertEquals("session_check", sessionDestination(reopenedSession.uiState.value, Routes.LOGIN))
        advanceUntilIdle()
        assertEquals(Routes.DASHBOARD, sessionDestination(reopenedSession.uiState.value, Routes.LOGIN))
        assertTrue(reopenedSession.uiState.value.canAccessDashboard)
    }
}
