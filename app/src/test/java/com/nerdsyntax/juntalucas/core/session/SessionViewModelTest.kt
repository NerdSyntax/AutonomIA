package com.nerdsyntax.juntalucas.core.session

import com.nerdsyntax.juntalucas.feature.auth.domain.model.AuthUser
import com.nerdsyntax.juntalucas.support.FakeAuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.Business
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private class Repository(val read: suspend () -> Result<Business?>) : BusinessRepository {
        override suspend fun getBusiness() = read()
        override suspend fun saveBusiness(business: Business): Result<Unit> = error("Unused")
    }

    @Test fun verifiedUserMustWaitForValidBusiness() = runTest(dispatcher) {
        val pending = CompletableDeferred<Result<Business?>>()
        val vm = SessionViewModel(FakeAuthRepository(userA), Repository { pending.await() })
        advanceUntilIdle()
        assertEquals(BusinessSetupStatus.CHECKING, vm.uiState.value.businessSetupStatus)
        assertFalse(vm.uiState.value.canAccessDashboard)
        pending.complete(Result.success(Business(nombreNegocio = "Negocio", rubro = "Comercio", tipoActividad = "ambos", onboardingCompleted = true)))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.canAccessDashboard)
    }

    @Test fun absentOrIncompleteBusinessRequiresOnboardingAndRefreshReadsAgain() = runTest(dispatcher) {
        var business: Business? = null
        val vm = SessionViewModel(FakeAuthRepository(userA), Repository { Result.success(business) })
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
        val vm = SessionViewModel(FakeAuthRepository(userA), Repository {
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
        val auth = FakeAuthRepository(userA.copy(isEmailVerified = false))
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
        val auth = FakeAuthRepository(userA)
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
        val auth = FakeAuthRepository(userA.copy(isEmailVerified = false))
        val firstSession = SessionViewModel(auth, repository)
        advanceUntilIdle()
        assertEquals(Routes.VERIFY_EMAIL, sessionDestination(firstSession.uiState.value, Routes.LOGIN))
        auth.currentUser.value = userA
        advanceUntilIdle()
        assertEquals(Routes.BUSINESS_INFO, sessionDestination(firstSession.uiState.value, Routes.VERIFY_EMAIL))
        val reopenedSession = SessionViewModel(FakeAuthRepository(userA), repository)
        assertEquals(Routes.SESSION_CHECK, sessionDestination(reopenedSession.uiState.value, Routes.DASHBOARD))
        advanceUntilIdle()
        for (route in listOf(Routes.LOGIN, Routes.SESSION_CHECK, Routes.DASHBOARD)) {
            assertEquals(Routes.BUSINESS_INFO, sessionDestination(reopenedSession.uiState.value, route))
        }
        assertFalse(reopenedSession.uiState.value.canAccessDashboard)
    }

    @Test fun savedBusinessOnlyGoesToDashboardWithCompletionFlag() = runTest(dispatcher) {
        var business = Business(nombreNegocio = "Negocio", rubro = "Comercio", tipoActividad = "ambos")
        val vm = SessionViewModel(FakeAuthRepository(userA), Repository { Result.success(business) })
        advanceUntilIdle()
        assertEquals(Routes.BUSINESS_INFO, sessionDestination(vm.uiState.value, Routes.LOGIN))
        business = business.copy(onboardingCompleted = true)
        val reopenedSession = SessionViewModel(FakeAuthRepository(userA), Repository { Result.success(business) })
        assertEquals(Routes.SESSION_CHECK, sessionDestination(reopenedSession.uiState.value, Routes.LOGIN))
        advanceUntilIdle()
        assertEquals(Routes.DASHBOARD, sessionDestination(reopenedSession.uiState.value, Routes.LOGIN))
        assertTrue(reopenedSession.uiState.value.canAccessDashboard)
    }
}
