package com.nerdsyntax.juntalucas.feature.dashboard.ui

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

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
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

    @Test fun loadsBusinessAndEmailWithEmptyFinancialData() = runTest(dispatcher) {
        val vm = DashboardViewModel(Auth(userA), Repository {
            Result.success(Business(nombreNegocio = "Negocio A", metaMensual = 500000L))
        })
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals("a@example.com", state.userEmail)
        assertEquals("Negocio A", state.nombreNegocio)
        assertEquals(500000L, state.metaMensual)
        assertEquals(0L, state.ventasTotales)
        assertEquals(0L, state.gastosTotales)
        assertEquals(0L, state.ganancia)
        assertEquals("—", state.margen)
        assertTrue(state.movimientosRecientes.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test fun accountChangeCancelsOldReadAndLogoutClearsBusiness() = runTest(dispatcher) {
        val auth = Auth(userA)
        val pendingA = CompletableDeferred<Result<Business?>>()
        val pendingB = CompletableDeferred<Result<Business?>>()
        val vm = DashboardViewModel(auth, Repository {
            if (auth.currentUser.value?.uid == "a") pendingA.await() else pendingB.await()
        })
        advanceUntilIdle()
        auth.currentUser.value = userB
        advanceUntilIdle()
        assertEquals("b@example.com", vm.uiState.value.userEmail)
        assertEquals("", vm.uiState.value.nombreNegocio)
        assertTrue(vm.uiState.value.isLoading)
        pendingB.complete(Result.success(Business(nombreNegocio = "Negocio B", metaMensual = 700000)))
        advanceUntilIdle()
        pendingA.complete(Result.success(Business(nombreNegocio = "Negocio A")))
        advanceUntilIdle()
        assertEquals("Negocio B", vm.uiState.value.nombreNegocio)
        assertEquals(700000L, vm.uiState.value.metaMensual)
        auth.logout()
        advanceUntilIdle()
        assertEquals("", vm.uiState.value.nombreNegocio)
        assertEquals("", vm.uiState.value.userEmail)
        assertEquals(0L, vm.uiState.value.metaMensual)
    }

    @Test fun missingBusinessAndReadErrorNeverUseExampleData() = runTest(dispatcher) {
        val auth = Auth(userA)
        val vm = DashboardViewModel(auth, Repository {
            if (auth.currentUser.value?.uid == "a") Result.success(null)
            else Result.failure(IllegalStateException("technical details"))
        })
        advanceUntilIdle()
        assertEquals("", vm.uiState.value.nombreNegocio)
        assertNotNull(vm.uiState.value.errorMessage)
        auth.currentUser.value = userB
        advanceUntilIdle()
        assertEquals("b@example.com", vm.uiState.value.userEmail)
        assertEquals("", vm.uiState.value.nombreNegocio)
        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.errorMessage!!.contains("technical details"))
        assertTrue(vm.uiState.value.movimientosRecientes.isEmpty())
    }
}
