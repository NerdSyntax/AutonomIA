package com.nerdsyntax.juntalucas.feature.auth.ui.login

import androidx.credentials.exceptions.GetCredentialCancellationException
import com.nerdsyntax.juntalucas.feature.auth.domain.model.AuthUser
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LoginViewModelTest {
    private class Auth : AuthRepository {
        override val currentUser = MutableStateFlow<AuthUser?>(null)
        var receivedToken: String? = null
        val googleUser = AuthUser("google-uid", "user@gmail.com", true, "Nombre", "https://example.com/photo")
        override suspend fun loginWithGoogle(idToken: String): Result<AuthUser> {
            receivedToken = idToken
            currentUser.value = googleUser
            return Result.success(googleUser)
        }
        override suspend fun login(email: String, password: String): Result<AuthUser> = error("Unused")
        override suspend fun register(email: String, password: String): Result<AuthUser> = error("Unused")
        override suspend fun sendPasswordReset(email: String): Result<Unit> = error("Unused")
        override suspend fun sendEmailVerification(): Result<Unit> = error("Unused")
        override suspend fun reloadCurrentUser(): Result<AuthUser?> = error("Unused")
        override suspend fun deleteCurrentUser(): Result<Unit> = error("Unused")
        override fun logout() { currentUser.value = null }
    }

    @Test fun googleDoesNotRequireEmailOrPasswordAndPublishesFirebaseUser() = runTest {
        val auth = Auth()
        val vm = LoginViewModel(auth)
        vm.loginWithGoogle { "google-token" }
        assertEquals("google-token", auth.receivedToken)
        assertEquals(auth.googleUser, auth.currentUser.value)
        assertNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun cancellingSelectorDoesNotAuthenticateOrShowError() = runTest {
        val auth = Auth()
        val vm = LoginViewModel(auth)
        vm.loginWithGoogle { throw GetCredentialCancellationException() }
        assertNull(auth.receivedToken)
        assertNull(auth.currentUser.value)
        assertNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun realFailureShowsFriendlyMessageAndAllowsRetry() = runTest {
        val auth = Auth()
        val vm = LoginViewModel(auth)
        vm.loginWithGoogle { error("technical OAuth details") }
        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.errorMessage!!.contains("technical"))
        assertFalse(vm.uiState.value.isLoading)
        vm.loginWithGoogle { "retry-token" }
        assertEquals("retry-token", auth.receivedToken)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test fun leavingScreenPropagatesCancellationAndReleasesLoading() = runTest {
        val auth = Auth()
        val vm = LoginViewModel(auth)
        try {
            vm.loginWithGoogle { throw CancellationException() }
            fail("Cancellation must propagate")
        } catch (_: CancellationException) {
            assertNull(auth.receivedToken)
            assertNull(vm.uiState.value.errorMessage)
            assertFalse(vm.uiState.value.isLoading)
        }
    }
}
