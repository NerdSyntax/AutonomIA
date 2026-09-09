package com.nerdsyntax.juntalucas.feature.ai.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AiViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AiUiState())
    val uiState = _uiState.asStateFlow()
}
