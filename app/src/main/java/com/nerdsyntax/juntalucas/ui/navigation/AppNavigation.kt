package com.nerdsyntax.juntalucas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nerdsyntax.juntalucas.data.auth.FirebaseAuthRepository
import com.nerdsyntax.juntalucas.ui.auth.AccountScreen
import com.nerdsyntax.juntalucas.ui.auth.AuthViewModel
import com.nerdsyntax.juntalucas.ui.auth.ForgotPasswordScreen
import com.nerdsyntax.juntalucas.ui.auth.LoginScreen
import com.nerdsyntax.juntalucas.ui.auth.RegisterScreen
import com.nerdsyntax.juntalucas.ui.auth.VerifyEmailScreen
import com.nerdsyntax.juntalucas.ui.home.HomeScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val authRepository = remember {
        FirebaseAuthRepository()
    }

    val factory = remember(authRepository) {

        object : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {

                if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {

                    return AuthViewModel(
                        authRepository = authRepository
                    ) as T
                }

                throw IllegalArgumentException(
                    "ViewModel desconocido: ${modelClass.name}"
                )
            }
        }
    }

    val authViewModel: AuthViewModel = viewModel(
        factory = factory
    )

    val state by authViewModel
        .uiState
        .collectAsStateWithLifecycle()

    val backStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute =
        backStackEntry?.destination?.route

    val startDestination = remember {

        val user = authRepository.getCurrentUser()

        when {

            user == null ->
                Routes.LOGIN

            user.isEmailVerified ->
                Routes.HOME

            else ->
                Routes.VERIFY_EMAIL
        }
    }

    /*
     * Este bloque observa si cambia la sesión.
     *
     * Ejemplos:
     *
     * Login correcto -> Home
     * Registro -> Verificar correo
     * Logout -> Login
     */
    LaunchedEffect(
        state.currentUser,
        currentRoute
    ) {

        val user = state.currentUser

        when {

            // No hay usuario, pero intenta entrar a una pantalla protegida.
            user == null &&
                    currentRoute !in publicRoutes -> {

                navController.navigateAndClear(
                    Routes.LOGIN
                )
            }

            // Existe usuario, pero todavía no verificó el correo.
            user != null &&
                    !user.isEmailVerified &&
                    currentRoute != Routes.VERIFY_EMAIL -> {

                navController.navigateAndClear(
                    Routes.VERIFY_EMAIL
                )
            }

            // Usuario completamente autenticado.
            user != null &&
                    user.isEmailVerified &&
                    currentRoute in authRoutes -> {

                navController.navigateAndClear(
                    Routes.HOME
                )
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // LOGIN
        composable(
            route = Routes.LOGIN
        ) {

            LoginScreen(
                state = state,

                onLogin = authViewModel::login,

                onRegisterClick = {

                    authViewModel.clearMessages()

                    navController.navigate(
                        Routes.REGISTER
                    )
                },

                onForgotPasswordClick = {

                    authViewModel.clearMessages()

                    navController.navigate(
                        Routes.FORGOT_PASSWORD
                    )
                }
            )
        }

        // CREAR CUENTA
        composable(
            route = Routes.REGISTER
        ) {

            RegisterScreen(
                state = state,

                onRegister = authViewModel::register,

                onBack = {

                    authViewModel.clearMessages()

                    navController.popBackStack()
                }
            )
        }

        // RECUPERAR CONTRASEÑA
        composable(
            route = Routes.FORGOT_PASSWORD
        ) {

            ForgotPasswordScreen(
                state = state,

                onSendReset =
                    authViewModel::sendPasswordReset,

                onBack = {

                    authViewModel.clearMessages()

                    navController.popBackStack()
                }
            )
        }

        // VERIFICAR CORREO
        composable(
            route = Routes.VERIFY_EMAIL
        ) {

            VerifyEmailScreen(
                state = state,

                onCheckVerification =
                    authViewModel::reloadCurrentUser,

                onResendVerification =
                    authViewModel::resendVerificationEmail,

                onLogout =
                    authViewModel::logout
            )
        }

        // HOME
        composable(
            route = Routes.HOME
        ) {

            HomeScreen(
                email = state.currentUser
                    ?.email
                    .orEmpty(),

                onAccountClick = {

                    authViewModel.clearMessages()

                    navController.navigate(
                        Routes.ACCOUNT
                    )
                }
            )
        }

        // MI CUENTA
        composable(
            route = Routes.ACCOUNT
        ) {

            AccountScreen(
                state = state,

                onResetPassword =
                    authViewModel::sendPasswordReset,

                onLogout =
                    authViewModel::logout,

                onDeleteAccount =
                    authViewModel::deleteAccount,

                onBack = {

                    authViewModel.clearMessages()

                    navController.popBackStack()
                }
            )
        }
    }
}


/*
 * Navega a una pantalla y limpia las anteriores.
 *
 * Sirve para que después del logout el usuario
 * no pueda presionar "Atrás" y volver al Home.
 */
private fun NavHostController.navigateAndClear(
    route: String
) {

    navigate(route) {

        popUpTo(
            graph.startDestinationId
        ) {
            inclusive = true
        }

        launchSingleTop = true
    }
}


/*
 * Pantallas que puede visitar alguien sin iniciar sesión.
 */
private val publicRoutes = setOf(
    Routes.LOGIN,
    Routes.REGISTER,
    Routes.FORGOT_PASSWORD
)


/*
 * Pantallas relacionadas con autenticación.
 *
 * Si el usuario ya está autenticado y verificado,
 * no tiene sentido que permanezca en ellas.
 */
private val authRoutes = setOf(
    Routes.LOGIN,
    Routes.REGISTER,
    Routes.FORGOT_PASSWORD,
    Routes.VERIFY_EMAIL
)