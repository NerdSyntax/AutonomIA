package com.nerdsyntax.juntalucas.feature.movements.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MovementsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MovementsUiState())
    val uiState = _uiState.asStateFlow()
}
