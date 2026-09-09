package com.nerdsyntax.juntalucas.feature.business.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BusinessViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BusinessUiState())
    val uiState = _uiState.asStateFlow()
}
