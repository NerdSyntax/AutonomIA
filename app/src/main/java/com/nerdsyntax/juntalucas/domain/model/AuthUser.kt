package com.nerdsyntax.juntalucas.domain.model

data class AuthUser(
    val uid: String,
    val email: String,
    val isEmailVerified: Boolean
)