package com.nerdsyntax.juntalucas.di

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.CatalogRepository
import com.nerdsyntax.juntalucas.feature.business.ui.product.AddProductViewModel

class ProductViewModelFactory(
    private val auth: AuthRepository,
    private val catalog: CatalogRepository,
    private val productId: String?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AddProductViewModel(auth, catalog, SavedStateHandle(), productId) as T
}
