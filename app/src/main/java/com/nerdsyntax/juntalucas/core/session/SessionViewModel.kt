package com.nerdsyntax.juntalucas.core.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SessionViewModel(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessRepository
) : ViewModel() {
    private val refresh = MutableStateFlow(0)
    private val _uiState = MutableStateFlow(SessionUiState(authRepository.currentUser.value))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(authRepository.currentUser, refresh) { user, _ -> user }.collectLatest { user ->
                _uiState.value = SessionUiState(user)
                if (user?.isEmailVerified != true) return@collectLatest
                val result = businessRepository.getBusiness()
                if (authRepository.currentUser.value != user) return@collectLatest
                _uiState.value = result.fold(
                    onSuccess = { business ->
                        val complete = business != null && business.onboardingCompleted && business.nombreNegocio.isNotBlank() &&
                            business.rubro.isNotBlank() && business.tipoActividad in setOf("productos", "servicios", "ambos")
                        SessionUiState(user, if (complete) BusinessSetupStatus.COMPLETE else BusinessSetupStatus.REQUIRED)
                    },
                    onFailure = {
                        SessionUiState(user, BusinessSetupStatus.ERROR,
                            "No pudimos comprobar la configuración de tu negocio. Revisa tu conexión e inténtalo nuevamente.")
                    }
                )
            }
        }
    }

    fun refreshBusiness() {
        _uiState.value = SessionUiState(authRepository.currentUser.value)
        refresh.value += 1
    }
    fun logout() = authRepository.logout()
}
