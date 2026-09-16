package com.nerdsyntax.juntalucas.feature.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import com.nerdsyntax.juntalucas.feature.auth.domain.model.AuthUser
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {
    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser?.toAuthUser())
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        _currentUser.value = auth.currentUser?.toAuthUser()
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override suspend fun login(email: String, password: String): Result<AuthUser> = runCatching {
        val user = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await().user
            ?: error("No se pudo obtener el usuario.")
        user.toAuthUser().also { _currentUser.value = it }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<AuthUser> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val user = firebaseAuth.signInWithCredential(credential).await().user
            ?: error("No se pudo obtener el usuario de Google.")
        Result.success(user.toAuthUser().also { _currentUser.value = it })
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun register(email: String, password: String): Result<AuthUser> = runCatching {
        val user = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: error("No se pudo crear el usuario.")
        user.sendEmailVerification().await()
        user.toAuthUser().also { _currentUser.value = it }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        firebaseAuth.sendPasswordResetEmail(email.trim()).await()
    }

    override suspend fun sendEmailVerification(): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser ?: error("No existe una sesión activa.")
        user.sendEmailVerification().await()
    }

    override suspend fun reloadCurrentUser(): Result<AuthUser?> = runCatching {
        val user = firebaseAuth.currentUser ?: return@runCatching null
        user.reload().await()
        firebaseAuth.currentUser?.toAuthUser().also { _currentUser.value = it }
    }

    override suspend fun deleteCurrentUser(): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser ?: error("No existe una sesión activa.")
        user.delete().await()
        _currentUser.value = null
    }

    override fun logout() {
        firebaseAuth.signOut()
        _currentUser.value = null
    }

    private fun FirebaseUser.toAuthUser() = AuthUser(
        uid = uid,
        email = email.orEmpty(),
        isEmailVerified = isEmailVerified,
        displayName = displayName,
        photoUrl = photoUrl?.toString()
    )
}
