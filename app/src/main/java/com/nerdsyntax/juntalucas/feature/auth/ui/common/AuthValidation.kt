package com.nerdsyntax.juntalucas.feature.auth.ui.common

import android.util.Patterns

internal fun isValidEmail(email: String): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(email).matches()
