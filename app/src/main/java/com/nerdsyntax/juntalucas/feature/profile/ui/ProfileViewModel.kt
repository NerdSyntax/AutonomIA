package com.nerdsyntax.juntalucas.feature.profile.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.Business
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.ExpensesRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.SaleDates
import com.nerdsyntax.juntalucas.feature.movements.domain.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessRepository,
    private val salesRepository: SalesRepository,
    private val expensesRepository: ExpensesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private var cachedBusiness: Business? = null
    private var nombreAuth: String = ""

    init {
        loadProfileData()
    }

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
            }.onFailure { Log.e(TAG, "No se pudo actualizar en Auth", it) }.isSuccess

            val base = cachedBusiness ?: businessRepository.getBusiness().getOrNull()
            val firestoreOk = if (base != null) {
                val actualizado = base.copy(nombreDueno = nuevoNombre)
                businessRepository.saveBusiness(actualizado)
                    .onSuccess { cachedBusiness = actualizado }
                    .onFailure { Log.e(TAG, "No se pudo guardar en Firestore", it) }
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
                        statusMessage = "No se pudo actualizar el perfil. Revisa tu conexión."
                    )
                }
            }
        }
    }

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
            _uiState.update { it.copy(statusMessage = "Completa el nombre y el rubro") }
            return
        }
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, statusMessage = null) }

            val base = cachedBusiness ?: businessRepository.getBusiness().getOrNull()
            if (base == null) {
                _uiState.update {
                    it.copy(isSaving = false, statusMessage = "No se pudieron cargar los datos.")
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
                            statusMessage = "Datos guardados exitosamente."
                        )
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "No se pudo guardar el negocio", error)
                    _uiState.update {
                        it.copy(isSaving = false, statusMessage = "Error al guardar.")
                    }
                }
        }
    }

    fun exportarDatosCsv(context: android.content.Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = "Generando reporte de Firebase...") }

            try {
                val uid = authRepository.currentUser.value?.uid
                if (uid == null) {
                    _uiState.update { it.copy(statusMessage = "Debes iniciar sesión para exportar.") }
                    return@launch
                }

                val business = cachedBusiness
                val nombreNegocio = business?.nombreNegocio ?: "Mi Negocio"
                val metaMensual = business?.metaMensual ?: 0L
                val currentMonth = SaleDates.today().take(7)

                val salesResult = salesRepository.observeMonth(uid, currentMonth).first()
                val expensesResult = expensesRepository.observeMonth(uid, currentMonth).first()

                val sales = salesResult.getOrNull()?.value ?: emptyList()
                val expenses = expensesResult.getOrNull()?.value ?: emptyList()

                val csvContent = buildString {
                    appendLine("sep=;")
                    appendLine("REPORTE FINANCIERO - ${nombreNegocio.uppercase()}")
                    appendLine("Periodo analizado:;$currentMonth")
                    appendLine("Meta mensual configurada:;$metaMensual")
                    appendLine()

                    appendLine("VENTAS;;;;;GASTOS;;;;")
                    appendLine("FECHA;PRODUCTO;MONTO;MEDIO PAGO;;FECHA;DESCRIPCION;CATEGORIA;MONTO;MEDIO PAGO")

                    val sortedSales = sales.sortedByDescending { it.draft.saleDate }
                    val sortedExpenses = expenses.sortedByDescending { it.draft.expenseDate }

                    val maxRows = maxOf(sortedSales.size, sortedExpenses.size)

                    for (i in 0 until maxRows) {
                        val sale = sortedSales.getOrNull(i)
                        val expense = sortedExpenses.getOrNull(i)

                        val saleCols = if (sale != null) {
                            val nombre = sale.productName.replace(";", ",")
                            "${sale.draft.saleDate};$nombre;${sale.draft.total};${sale.draft.paymentMethod.label}"
                        } else {
                            ";;;"
                        }

                        val sepCol = ""

                        val expenseCols = if (expense != null) {
                            val desc = expense.draft.description.replace(";", ",")
                            val cat = expense.draft.category.replace(";", ",")
                            "${expense.draft.expenseDate};$desc;$cat;${expense.draft.amount};${expense.draft.paymentMethod.label}"
                        } else {
                            ";;;;"
                        }

                        appendLine("$saleCols;$sepCol;$expenseCols")
                    }
                }

                val fileName = "Reporte_${nombreNegocio.replace(" ", "_")}_$currentMonth.csv"
                val file = java.io.File(context.cacheDir, fileName)
                file.writeText("\uFEFF$csvContent", Charsets.UTF_8)

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Reporte Financiero $currentMonth")
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(android.content.Intent.createChooser(intent, "Exportar movimientos"))
                clearStatusMessage()

            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "Error al generar CSV", e)
                _uiState.update { it.copy(statusMessage = "Error al exportar los datos.") }
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