package com.nerdsyntax.juntalucas.feature.onboarding.ui

import com.nerdsyntax.juntalucas.feature.business.domain.Business
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessAuthenticationException
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

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() {
        Dispatchers.resetMain()
        MockAppDatabase.movements = emptyList()
    }

    private class Repository : BusinessRepository {
        val response = CompletableDeferred<Result<Unit>>()
        val saved = mutableListOf<Business>()
        override suspend fun saveBusiness(business: Business): Result<Unit> {
            saved += business
            return response.await()
        }
        override suspend fun getBusiness(): Result<Business?> = Result.success(null)
    }

    private fun viewModel(repository: Repository) = OnboardingViewModel(repository).apply {
        onNombreChange("Mi negocio")
        onRubroChange("Pastelería")
        onRegionChange("Biobío")
        onComunaChange("Concepción")
        onMetaChange("1500000")
        onTipoActividadChange("productos")
        onPuntoPartidaChange("ejemplo")
    }

    @Test fun savesAllFieldsAndWaitsForConfirmationWithoutDuplicateWrites() = runTest(dispatcher) {
        val repository = Repository()
        val vm = viewModel(repository)
        vm.finalizarConfiguracion()
        vm.finalizarConfiguracion()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.isSuccess)
        assertEquals(listOf(Business("Mi negocio", "Pastelería", "Biobío", "Concepción", 1500000L, "productos", "ejemplo", onboardingCompleted = true)), repository.saved)
        repository.response.complete(Result.success(Unit))
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.isSuccess)
        assertEquals(5, MockAppDatabase.movements.size)
        vm.finalizarConfiguracion()
        advanceUntilIdle()
        assertEquals(1, repository.saved.size)
    }

    @Test fun failureKeepsFormAndDoesNotSignalNavigationOrExposeException() = runTest(dispatcher) {
        val repository = Repository()
        repository.response.complete(Result.failure(IllegalStateException("private technical detail")))
        val vm = viewModel(repository)
        vm.finalizarConfiguracion()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isSuccess)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals("Mi negocio", vm.uiState.value.nombreNegocio)
        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.errorMessage!!.contains("private technical detail"))
        assertTrue(MockAppDatabase.movements.isEmpty())
        vm.finalizarConfiguracion()
        advanceUntilIdle()
        assertEquals(2, repository.saved.size)
    }

    @Test fun missingSessionShowsActionableError() = runTest(dispatcher) {
        val repository = Repository()
        repository.response.complete(Result.failure(BusinessAuthenticationException()))
        val vm = viewModel(repository)
        vm.finalizarConfiguracion()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isSuccess)
        assertTrue(vm.uiState.value.errorMessage!!.contains("iniciar sesión"))
    }

    @Test fun invalidGoalDoesNotWrite() = runTest(dispatcher) {
        val repository = Repository()
        val vm = viewModel(repository)
        vm.onMetaChange("-1")
        vm.finalizarConfiguracion()
        advanceUntilIdle()
        assertTrue(repository.saved.isEmpty())
        assertNotNull(vm.uiState.value.errorMessage)
    }
}
