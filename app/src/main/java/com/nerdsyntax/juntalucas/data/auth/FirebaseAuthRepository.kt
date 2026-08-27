package com.nerdsyntax.juntalucas.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nerdsyntax.juntalucas.domain.model.AuthUser
import com.nerdsyntax.juntalucas.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override fun getCurrentUser(): AuthUser? {
        return firebaseAuth.currentUser?.toAuthUser()
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthUser> {
        return runCatching {

            val result = firebaseAuth
                .signInWithEmailAndPassword(
                    email.trim(),
                    password
                )
                .await()

            val user = result.user
                ?: error("No se pudo obtener el usuario.")

            user.toAuthUser()
        }
    }

    override suspend fun register(
        email: String,
        password: String
    ): Result<AuthUser> {
        return runCatching {

            val result = firebaseAuth
                .createUserWithEmailAndPassword(
                    email.trim(),
                    password
                )
                .await()

            val user = result.user
                ?: error("No se pudo crear el usuario.")

            user.sendEmailVerification().await()

            user.toAuthUser()
        }
    }

    override suspend fun sendPasswordReset(
        email: String
    ): Result<Unit> {
        return runCatching {

            firebaseAuth
                .sendPasswordResetEmail(email.trim())
                .await()

            Unit
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return runCatching {

            val user = firebaseAuth.currentUser
                ?: error("No existe una sesión activa.")

            user.sendEmailVerification().await()

            Unit
        }
    }

    override suspend fun reloadCurrentUser(): Result<AuthUser?> {
        return runCatching {

            val user = firebaseAuth.currentUser
                ?: return@runCatching null

            user.reload().await()

            firebaseAuth.currentUser?.toAuthUser()
        }
    }

    override suspend fun deleteCurrentUser(): Result<Unit> {
        return runCatching {

            val user = firebaseAuth.currentUser
                ?: error("No existe una sesión activa.")

            user.delete().await()

            Unit
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    private fun FirebaseUser.toAuthUser(): AuthUser {
        return AuthUser(
            uid = uid,
            email = email.orEmpty(),
            isEmailVerified = isEmailVerified
        )
    }
}