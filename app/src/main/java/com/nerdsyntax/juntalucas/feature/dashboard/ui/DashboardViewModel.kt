package com.nerdsyntax.juntalucas.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                // Clear the previous account and cancel its pending read on session changes.
                _uiState.value = DashboardUiState(userEmail = user?.email.orEmpty(), isLoading = user != null)
                if (user == null) {
                    _uiState.value = DashboardUiState(errorMessage = "Inicia sesión para ver tu negocio.")
                    return@collectLatest
                }
                val result = businessRepository.getBusiness()
                if (authRepository.currentUser.value?.uid != user.uid) return@collectLatest
                _uiState.value = result.fold(
                    onSuccess = { business ->
                        DashboardUiState(
                            userEmail = user.email,
                            nombreNegocio = business?.nombreNegocio.orEmpty(),
                            metaMensual = business?.metaMensual ?: 0,
                            errorMessage = if (business == null) "Aún no tienes un negocio configurado." else null
                        )
                    },
                    onFailure = {
                        DashboardUiState(
                            userEmail = user.email,
                            errorMessage = "No pudimos cargar tu negocio. Revisa tu conexión e inténtalo nuevamente."
                        )
                    }
                )
            }
        }
    }
}
