package com.nerdsyntax.juntalucas.feature.profile.ui

import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.Business
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private var cachedBusiness: Business? = null
    private var nombreAuth: String = ""

    init {
        loadProfileData()
    }

    //  CARGA

    private fun loadProfileData() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                if (user != null) {
                    val firebaseUser = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
                    nombreAuth = firebaseUser?.displayName?.takeIf { it.isNotBlank() }
                        ?: user.displayName?.takeIf { it.isNotBlank() }
                                ?: ""
                    _uiState.update {
                        it.copy(
                            name = nombreVisible(),
                            email = user.email ?: firebaseUser?.email ?: ""
                        )
                    }
                }
            }
        }

        viewModelScope.launch { cargarDatosNegocio() }
    }

    /** Nombre guardado en el perfil del negocio; si no hay, el de la cuenta de Firebase Auth. */
    private fun nombreVisible(): String =
        cachedBusiness?.nombreDueno?.takeIf { it.isNotBlank() }
            ?: nombreAuth.ifBlank { "Usuario" }

    private suspend fun cargarDatosNegocio() {
        businessRepository.getBusiness()
            .onSuccess { business ->
                cachedBusiness = business
                if (business != null) {
                    _uiState.update {
                        it.copy(
                            name = business.nombreDueno.takeIf { n -> n.isNotBlank() } ?: it.name,
                            businessName = business.nombreNegocio,
                            rubro = business.rubro,
                            metaMensual = formatearClp(business.metaMensual),
                            moneda = MONEDA,
                        )
                    }
                }
            }
            .onFailure { Log.e(TAG, "No se pudo leer el negocio", it) }
    }

    private fun formatearClp(monto: Long): String {
        val symbols = DecimalFormatSymbols().apply { groupingSeparator = '.' }
        val formatter = DecimalFormat("#,###", symbols)
        return "$${formatter.format(monto)}"
    }

    //  EDITAR USUARIO

    fun abrirEditarUsuario() {
        _uiState.update {
            it.copy(
                isEditingUser = true,
                editNameInput = it.name,
                statusMessage = null
            )
        }
    }

    fun cerrarEditarUsuario() {
        _uiState.update { it.copy(isEditingUser = false) }
    }

    fun onEditNameChange(value: String) {
        _uiState.update { it.copy(editNameInput = value) }
    }

    fun guardarDatosUsuario() {
        val nuevoNombre = _uiState.value.editNameInput.trim()
        if (nuevoNombre.isBlank()) {
            _uiState.update { it.copy(statusMessage = "El nombre no puede estar vacío") }
            return
        }
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, statusMessage = null) }

            val authOk = runCatching {
                FirebaseAuth.getInstance().currentUser
                    ?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(nuevoNombre).build())
                    ?.await()
            }.onFailure { Log.e(TAG, "No se pudo actualizar el nombre en Auth", it) }.isSuccess

            val base = cachedBusiness ?: businessRepository.getBusiness().getOrNull()
            val firestoreOk = if (base != null) {
                val actualizado = base.copy(nombreDueno = nuevoNombre)
                businessRepository.saveBusiness(actualizado)
                    .onSuccess { cachedBusiness = actualizado }
                    .onFailure { Log.e(TAG, "No se pudo guardar el nombre en Firestore", it) }
                    .isSuccess
            } else {
                false
            }

            if (authOk || firestoreOk) {
                if (authOk) nombreAuth = nuevoNombre
                _uiState.update {
                    it.copy(
                        name = nuevoNombre,
                        isEditingUser = false,
                        isSaving = false,
                        statusMessage = "Perfil actualizado correctamente"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        statusMessage = "No se pudo actualizar el perfil. Revisa tu conexión e inténtalo de nuevo."
                    )
                }
            }
        }
    }

    //  EDITAR NEGOCIO

    fun abrirEditarNegocio() {
        val metaLimpia = cachedBusiness?.metaMensual?.takeIf { it > 0L }?.toString()
            ?: _uiState.value.metaMensual.filter { it.isDigit() }

        _uiState.update {
            it.copy(
                isEditingBusiness = true,
                editBusinessNameInput = it.businessName,
                editRubroInput = it.rubro,
                editMetaInput = metaLimpia,
                statusMessage = null
            )
        }

        if (cachedBusiness == null) {
            viewModelScope.launch { cargarDatosNegocio() }
        }
    }

    fun cerrarEditarNegocio() {
        _uiState.update { it.copy(isEditingBusiness = false) }
    }

    fun onEditBusinessNameChange(value: String) {
        _uiState.update { it.copy(editBusinessNameInput = value) }
    }

    fun onEditRubroChange(value: String) {
        _uiState.update { it.copy(editRubroInput = value) }
    }

    fun onEditMetaChange(value: String) {
        val soloNumeros = value.filter { it.isDigit() }.take(12)
        _uiState.update { it.copy(editMetaInput = soloNumeros) }
    }

    fun guardarDatosNegocio() {
        val nuevoNegocio = _uiState.value.editBusinessNameInput.trim()
        val nuevoRubro = _uiState.value.editRubroInput.trim()
        val nuevaMeta = _uiState.value.editMetaInput.filter { it.isDigit() }.toLongOrNull() ?: 0L

        if (nuevoNegocio.isBlank() || nuevoRubro.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Completa el nombre del negocio y el rubro") }
            return
        }
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, statusMessage = null) }

            val base = cachedBusiness ?: businessRepository.getBusiness().getOrNull()
            if (base == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        statusMessage = "No se pudieron cargar los datos de tu negocio. Inténtalo de nuevo."
                    )
                }
                return@launch
            }

            val actualizado = base.copy(
                nombreNegocio = nuevoNegocio,
                rubro = nuevoRubro,
                metaMensual = nuevaMeta
            )

            businessRepository.saveBusiness(actualizado)
                .onSuccess {
                    cachedBusiness = actualizado
                    _uiState.update {
                        it.copy(
                            businessName = nuevoNegocio,
                            rubro = nuevoRubro,
                            metaMensual = formatearClp(nuevaMeta),
                            moneda = MONEDA,
                            isEditingBusiness = false,
                            isSaving = false,
                            statusMessage = "Datos del negocio guardados exitosamente."
                        )
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "No se pudo guardar el negocio", error)
                    _uiState.update {
                        it.copy(isSaving = false, statusMessage = "Error al guardar los datos.")
                    }
                }
        }
    }


    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun onNotificacionesChange(enabled: Boolean) {
        _uiState.update { it.copy(notificacionesEnabled = enabled) }
    }

    fun onBiometriaChange(enabled: Boolean) {
        _uiState.update { it.copy(biometriaEnabled = enabled) }
    }

    fun logout() {
        authRepository.logout()
    }

    private companion object {
        const val TAG = "ProfileVM"
        const val MONEDA = "Peso chileno (CLP)"
    }

}