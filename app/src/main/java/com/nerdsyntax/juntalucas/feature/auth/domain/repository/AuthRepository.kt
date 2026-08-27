package com.nerdsyntax.juntalucas.feature.auth.domain.repository

import com.nerdsyntax.juntalucas.feature.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>

    suspend fun login(email: String, password: String): Result<AuthUser>
    suspend fun register(email: String, password: String): Result<AuthUser>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun reloadCurrentUser(): Result<AuthUser?>
    suspend fun deleteCurrentUser(): Result<Unit>
    fun logout()
}
