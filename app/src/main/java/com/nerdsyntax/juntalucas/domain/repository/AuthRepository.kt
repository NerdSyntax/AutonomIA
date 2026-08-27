package com.nerdsyntax.juntalucas.domain.repository

import com.nerdsyntax.juntalucas.domain.model.AuthUser

interface AuthRepository {

    fun getCurrentUser(): AuthUser?

    suspend fun login(
        email: String,
        password: String
    ): Result<AuthUser>

    suspend fun register(
        email: String,
        password: String
    ): Result<AuthUser>

    suspend fun sendPasswordReset(
        email: String
    ): Result<Unit>

    suspend fun sendEmailVerification(): Result<Unit>

    suspend fun reloadCurrentUser(): Result<AuthUser?>

    suspend fun deleteCurrentUser(): Result<Unit>

    fun logout()
}