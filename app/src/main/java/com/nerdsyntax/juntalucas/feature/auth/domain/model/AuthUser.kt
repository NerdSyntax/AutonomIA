package com.nerdsyntax.juntalucas.feature.auth.domain.model

data class AuthUser(
    val uid: String,
    val email: String,
    val isEmailVerified: Boolean
)
