package com.nerdsyntax.juntalucas.feature.business.ui.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.salesUserMessage
import com.nerdsyntax.juntalucas.core.format.MAX_QUANTITY
import com.nerdsyntax.juntalucas.core.format.parseClp
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.CatalogProduct
import com.nerdsyntax.juntalucas.feature.business.domain.CatalogRepository
import com.nerdsyntax.juntalucas.feature.business.domain.validateProduct
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AddProductViewModel(
    private val auth: AuthRepository,
    private val catalog: CatalogRepository,
    private val savedState: SavedStateHandle = SavedStateHandle(),
    private val initialProductId: String? = null
) : ViewModel() {

    private val owner: String? = savedState["owner"] ?: auth.currentUser.value?.uid
    private val requestId: String = savedState["requestId"] ?: UUID.randomUUID().toString()

    private val _uiState = MutableStateFlow(
        AddProductUiState(
            isProduct = savedState["isProduct"] ?: true,
            nombre = savedState["nombre"] ?: "",
            categoria = savedState["categoria"] ?: "",
            precio = savedState["precio"] ?: "",
            costo = savedState["costo"] ?: "",
            descripcion = savedState["descripcion"] ?: "",
            stockActual = savedState["stockActual"] ?: "0",
            stockMinimo = savedState["stockMinimo"] ?: "2",
            unidad = savedState["unidad"] ?: "unidad",
            isActive = savedState["isActive"] ?: true,
            trackStock = savedState["trackStock"] ?: true,
            selectedPhotoUri = savedState["selectedPhotoUri"],
            storedPhotoPath = savedState["storedPhotoPath"],
            removeStoredPhoto = savedState["removeStoredPhoto"] ?: false
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        persist()

        if (!initialProductId.isNullOrBlank() && savedState.get<Boolean>("editLoaded") != true) {
            viewModelScope.launch {
                val uid = owner?.takeIf { it == auth.currentUser.value?.uid } ?: return@launch

                val product = catalog.observeAll(uid)
                    .mapNotNull { result ->
                        result.getOrNull()?.value?.firstOrNull { it.id == initialProductId }
                    }
                    .firstOrNull()

                if (product != null && auth.currentUser.value?.uid == uid) {
                    loadProduct(product)
                }
            }
        }
    }

    private fun loadProduct(product: CatalogProduct) {
        if (_uiState.value.isSaving || savedState.get<Boolean>("editLoaded") == true) return

        savedState["editLoaded"] = true
        savedState["isAsset"] = product.isAsset

        _uiState.update {
            it.copy(
                isProduct = product.isProduct,
                nombre = product.name,
                categoria = product.category,
                precio = product.price.toString(),
                costo = product.unitCost?.toString().orEmpty(),
                descripcion = product.description,
                stockActual = product.stock.toString(),
                stockMinimo = product.stockMinimum.toString(),
                unidad = product.unit,
                isActive = product.active,
                trackStock = product.trackStock,
                storedPhotoPath = product.photoStoragePath,
                errorMessage = null
            )
        }

        persist()
    }

    private fun change(transform: (AddProductUiState) -> AddProductUiState) {
        if (_uiState.value.isSaving || _uiState.value.isSuccess) return
        _uiState.update { transform(it).copy(errorMessage = null) }
        persist()
    }
    fun ocultarPaywallSuscripcion() = change { it.copy(mostrarPaywallSuscripcion = false) }

    fun onIsProductChange(value: Boolean) = change { it.copy(isProduct = value) }
    fun onNombreChange(value: String) = change { it.copy(nombre = value) }
    fun onCategoriaChange(value: String) = change { it.copy(categoria = value) }
    fun onPrecioChange(value: String) = change { it.copy(precio = value) }
    fun onCostoChange(value: String) = change { it.copy(costo = value) }
    fun onDescripcionChange(value: String) = change { it.copy(descripcion = value) }
    fun onStockActualChange(value: String) = change { it.copy(stockActual = value) }
    fun onStockMinimoChange(value: String) = change { it.copy(stockMinimo = value) }
    fun onUnidadChange(value: String) = change { it.copy(unidad = value) }
    fun onIsActiveChange(value: Boolean) = change { it.copy(isActive = value) }
    fun onTrackStockChange(value: Boolean) = change { it.copy(trackStock = value) }

    fun onPhotoChange(uri: String?) = change {
        it.copy(
            selectedPhotoUri = uri,
            removeStoredPhoto = uri == null && it.storedPhotoPath != null
        )
    }

    fun saveProduct() = saveProduct(uploadPhoto = null)

    fun saveProduct(uploadPhoto: (suspend (String, String, String) -> String)?) {
        val state = _uiState.value
        if (state.isSaving || state.isSuccess) return

        val product = try {
            val uid = owner?.takeIf { it == auth.currentUser.value?.uid }
                ?: throw DataValidationException("Tu sesión cambió. Vuelve a iniciar sesión.")

            if (initialProductId != null && savedState.get<Boolean>("editLoaded") != true) {
                throw DataValidationException("Espera a que se cargue el producto.")
            }

            fun stock(text: String): Long = text.trim()
                .takeIf { it.matches(Regex("[0-9]+")) }
                ?.toLongOrNull()
                ?.takeIf { it in 0..MAX_QUANTITY }
                ?: throw DataValidationException(
                    "El stock debe ser un entero entre 0 y 1.000.000."
                )

            val price = parseClp(state.precio)
                ?: throw DataValidationException("Ingresa un precio válido en CLP.")

            val unitCost = if (state.costo.isBlank()) {
                null
            } else {
                parseClp(state.costo)
                    ?: throw DataValidationException(
                        "Ingresa un costo válido o déjalo vacío si lo desconoces."
                    )
            }

            val trackStock = state.isProduct && state.trackStock

            CatalogProduct(
                id = initialProductId ?: requestId,
                userId = uid,
                name = state.nombre.trim(),
                isProduct = state.isProduct,
                price = price,
                unitCost = unitCost,
                active = state.isActive,
                trackStock = trackStock,
                stock = if (trackStock) stock(state.stockActual) else 0L,
                category = state.categoria.trim(),
                description = state.descripcion.trim(),
                stockMinimum = if (trackStock) stock(state.stockMinimo) else 0L,
                unit = state.unidad.trim(),
                isAsset = savedState.get<Boolean>("isAsset") ?: false
            ).also(::validateProduct)
        } catch (error: DataValidationException) {
            _uiState.update { it.copy(errorMessage = error.message) }
            return
        }

        persist()
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val uid = owner ?: return@launch
            if (initialProductId == null) {
                try {
                    val db = FirebaseFirestore.getInstance()

                    val profileDoc = db.collection("users").document(uid).collection("business").document("profile").get().await()
                    val susMap = profileDoc.get("suscripcion") as? Map<*, *>
                    val plan = susMap?.get("plan") as? String ?: "FREE"

                    if (plan == "FREE") {
                        val snapshot = db.collection("users").document(uid).collection("products").get().await()
                        val cantidadProductos = snapshot.size()
                        if (cantidadProductos >= 50) {
                            _uiState.update {
                                it.copy(
                                    isSaving = false,
                                    mostrarPaywallSuscripcion = true,
                                    errorMessage = null
                                )
                            }
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                }
            }


            val productToSave = try {
                val uri = state.selectedPhotoUri
                if (uri == null) {
                    product
                } else {
                    val upload = checkNotNull(uploadPhoto)
                    val path = upload(product.userId, product.id, uri)
                    product.copy(photoStoragePath = path)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "No se pudo subir la foto. Intenta nuevamente."
                    )
                }
                return@launch
            }

            if (auth.currentUser.value?.uid != owner) {
                showSessionChanged()
                return@launch
            }

            val result = when {
                initialProductId == null -> catalog.create(productToSave)
                state.selectedPhotoUri != null || state.removeStoredPhoto ->
                    catalog.updateWithPhotoChange(productToSave)
                else -> catalog.update(productToSave)
            }

            if (auth.currentUser.value?.uid != owner) {
                showSessionChanged()
                return@launch
            }

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, isSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = error.salesUserMessage())
                    }
                }
            )
        }
    }

    private fun showSessionChanged() {
        _uiState.update {
            it.copy(
                isSaving = false,
                errorMessage = "Tu sesión cambió. Vuelve a iniciar sesión."
            )
        }
    }

    private fun persist() {
        val state = _uiState.value
        savedState["owner"] = owner
        savedState["requestId"] = requestId
        savedState["isProduct"] = state.isProduct
        savedState["nombre"] = state.nombre
        savedState["categoria"] = state.categoria
        savedState["precio"] = state.precio
        savedState["costo"] = state.costo
        savedState["descripcion"] = state.descripcion
        savedState["stockActual"] = state.stockActual
        savedState["stockMinimo"] = state.stockMinimo
        savedState["unidad"] = state.unidad
        savedState["isActive"] = state.isActive
        savedState["trackStock"] = state.trackStock
        savedState["selectedPhotoUri"] = state.selectedPhotoUri
        savedState["storedPhotoPath"] = state.storedPhotoPath
        savedState["removeStoredPhoto"] = state.removeStoredPhoto
    }
}