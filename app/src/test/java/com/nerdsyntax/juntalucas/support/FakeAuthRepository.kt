package com.nerdsyntax.juntalucas.support

import com.nerdsyntax.juntalucas.feature.auth.domain.model.AuthUser
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow

open class FakeAuthRepository(user: AuthUser? = null) : AuthRepository {
    override val currentUser = MutableStateFlow(user)
    override suspend fun loginWithGoogle(idToken: String): Result<AuthUser> = error("Unused")
    override suspend fun login(email: String, password: String): Result<AuthUser> = error("Unused")
    override suspend fun register(email: String, password: String): Result<AuthUser> = error("Unused")
    override suspend fun sendPasswordReset(email: String): Result<Unit> = error("Unused")
    override suspend fun sendEmailVerification(): Result<Unit> = error("Unused")
    override suspend fun reloadCurrentUser(): Result<AuthUser?> = error("Unused")
    override suspend fun deleteCurrentUser(): Result<Unit> = error("Unused")
    override fun logout() { currentUser.value = null }
}
