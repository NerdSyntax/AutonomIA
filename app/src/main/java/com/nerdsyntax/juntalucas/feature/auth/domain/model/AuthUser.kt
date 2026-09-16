package com.nerdsyntax.juntalucas.feature.auth.domain.model

data class AuthUser(
    val uid: String,
    val email: String,
    val isEmailVerified: Boolean,
    val displayName: String? = null,
    val photoUrl: String? = null
)
