package com.nerdsyntax.juntalucas.core.navigation

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.nerdsyntax.juntalucas.core.session.BusinessSetupStatus
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.nerdsyntax.juntalucas.core.session.SessionViewModel
import com.nerdsyntax.juntalucas.di.AppContainer
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository
import com.nerdsyntax.juntalucas.feature.auth.data.FirebaseAuthRepository
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.auth.ui.account.*
import com.nerdsyntax.juntalucas.feature.auth.ui.login.*
import com.nerdsyntax.juntalucas.feature.auth.ui.recovery.*
import com.nerdsyntax.juntalucas.feature.auth.ui.register.*
import com.nerdsyntax.juntalucas.feature.auth.ui.verify.*
import com.nerdsyntax.juntalucas.feature.auth.ui.welcome.*
import com.nerdsyntax.juntalucas.feature.onboarding.ui.*
import com.nerdsyntax.juntalucas.feature.dashboard.ui.*
import com.nerdsyntax.juntalucas.feature.movements.ui.*
import com.nerdsyntax.juntalucas.feature.business.ui.*
import com.nerdsyntax.juntalucas.feature.ai.ui.*
import com.nerdsyntax.juntalucas.feature.profile.ui.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authRepository: AuthRepository = remember { FirebaseAuthRepository() }
    val factory = remember(authRepository) { AppViewModelFactory(authRepository, AppContainer.businessRepository) }
    val sessionViewModel: SessionViewModel = viewModel(factory = factory)
    val session by sessionViewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val startDestination = "splash"

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { sessionViewModel.refreshBusiness() }

    LaunchedEffect(session, currentRoute) {
        if (currentRoute == null || currentRoute == "splash") return@LaunchedEffect

        val target = sessionDestination(session, currentRoute)
        if (target != null && target != currentRoute) navController.navigateAndClear(target)
    }

    Scaffold(
        bottomBar = {
            if (session.canAccessDashboard && currentRoute in bottomRoutes) {
                AppBottomNavigation(currentRoute, navController::navigateToBottomRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("splash") {
                SplashScreen(
                    onTimeout = {
                        val nextRoute = when {
                            !session.isAuthenticated -> "welcome"
                            !session.isEmailVerified -> Routes.VERIFY_EMAIL
                            else -> "session_check"
                        }
                        navController.navigate(nextRoute) {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            composable("session_check") {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (session.businessSetupStatus == BusinessSetupStatus.ERROR) {
                        Text(session.errorMessage.orEmpty())
                        Button(onClick = sessionViewModel::refreshBusiness) { Text("Reintentar") }
                        Button(onClick = sessionViewModel::logout) { Text("Cerrar sesión") }
                    } else {
                        CircularProgressIndicator()
                        Text("Comprobando la configuración de tu negocio…")
                    }
                }
            }
            composable("welcome") {
                WelcomeScreen(
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                    onNavigateToLogin = { navController.navigate(Routes.LOGIN) }
                )
            }


            composable(Routes.LOGIN) {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val vm: LoginViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                LoginScreen(
                    state, vm::onEmailChange, vm::onPasswordChange, vm::login,
                    onForgotPasswordClick = { navController.navigate(Routes.FORGOT_PASSWORD) },
                    onRegisterClick = { navController.navigate(Routes.REGISTER) },
                    onGoogleClick = { scope.launch { vm.loginWithGoogle { getGoogleIdToken(context) } } }
                )
            }
            composable(Routes.REGISTER) {
                val vm: RegisterViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                RegisterScreen(
                    state, vm::onEmailChange, vm::onPasswordChange, vm::onConfirmPasswordChange,
                    vm::register, navController::popBackStack
                )
            }
            composable(Routes.FORGOT_PASSWORD) {
                val vm: ForgotPasswordViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                ForgotPasswordScreen(state, vm::onEmailChange, vm::sendReset, navController::popBackStack)
            }

            composable(Routes.VERIFY_EMAIL) {
                val vm: VerifyEmailViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                VerifyEmailScreen(
                    state = state,
                    onCheckVerification = vm::checkVerification,
                    onResendVerification = vm::resendVerification,
                    onLogout = vm::logout
                )
            }


            composable("business_info") {
                if (!session.isEmailVerified || session.businessSetupStatus != BusinessSetupStatus.REQUIRED) return@composable
                val vm: OnboardingViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                BusinessInfoScreen(
                    state = state,
                    onNombreChange = vm::onNombreChange,
                    onRubroChange = vm::onRubroChange,
                    onRegionChange = vm::onRegionChange,
                    onComunaChange = vm::onComunaChange,
                    onMetaChange = vm::onMetaChange,
                    onContinueClick = {
                        if (vm.validarPaso1()) {
                            navController.navigate("activity_selection")
                        }
                    }
                )
            }

            composable("activity_selection") { navBackStackEntry ->
                if (!session.isEmailVerified || session.businessSetupStatus != BusinessSetupStatus.REQUIRED) return@composable
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry("business_info") }
                val vm: OnboardingViewModel = viewModel(parentEntry, factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                ActivitySelectionScreen(
                    state = state,
                    onTipoActividadChange = vm::onTipoActividadChange,
                    onNavigateBack = { navController.popBackStack() },
                    onContinueClick = { navController.navigate("starting_point") }
                )
            }

            composable("starting_point") { navBackStackEntry ->
                if (!session.isEmailVerified || session.businessSetupStatus != BusinessSetupStatus.REQUIRED) return@composable
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry("business_info") }
                val vm: OnboardingViewModel = viewModel(parentEntry, factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.isSuccess) {
                    if (state.isSuccess) {
                        navController.navigateAndClear("session_check")
                        sessionViewModel.refreshBusiness()
                    }
                }
                StartingPointScreen(
                    state = state,
                    onPuntoPartidaChange = vm::onPuntoPartidaChange,
                    onNavigateBack = { navController.popBackStack() },
                    onFinishSetup = vm::finalizarConfiguracion
                )
            }


            composable(Routes.DASHBOARD) {
                if (!session.canAccessDashboard) return@composable
                val vm: DashboardViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()

                DashboardScreen(
                    state = state,
                    onNavigateToAi = {
                        navController.navigateToBottomRoute(Routes.AI)
                    }
                )
            }
            composable(Routes.MOVEMENTS) {
                if (!session.canAccessDashboard) return@composable
                val vm: MovementsViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                MovementsScreen(state)
            }
            composable(Routes.BUSINESS) {
                if (!session.canAccessDashboard) return@composable
                val vm: BusinessViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                BusinessScreen(state)
            }
            composable(Routes.AI) {
                if (!session.canAccessDashboard) return@composable
                val vm: AiViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                AiScreen(state)
            }
            composable(Routes.PROFILE) {
                if (!session.canAccessDashboard) return@composable
                val vm: ProfileViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                ProfileScreen(state) { navController.navigate(Routes.ACCOUNT) }
            }
            composable(Routes.ACCOUNT) {
                if (!session.canAccessDashboard) return@composable
                val vm: AccountViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                AccountScreen(
                    state, vm::sendPasswordReset, vm::logout, vm::deleteAccount,
                    navController::popBackStack
                )
            }
        }
    }
}

private class AppViewModelFactory(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(SessionViewModel::class.java) -> SessionViewModel(authRepository, businessRepository)
        modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(authRepository)
        modelClass.isAssignableFrom(RegisterViewModel::class.java) -> RegisterViewModel(authRepository)
        modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java) -> ForgotPasswordViewModel(authRepository)
        modelClass.isAssignableFrom(VerifyEmailViewModel::class.java) -> VerifyEmailViewModel(authRepository)
        modelClass.isAssignableFrom(AccountViewModel::class.java) -> AccountViewModel(authRepository)
        modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(authRepository, businessRepository)
        modelClass.isAssignableFrom(MovementsViewModel::class.java) -> MovementsViewModel()
        modelClass.isAssignableFrom(BusinessViewModel::class.java) -> BusinessViewModel()
        modelClass.isAssignableFrom(AiViewModel::class.java) -> AiViewModel()
        modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(authRepository)
        modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> OnboardingViewModel(businessRepository)
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    } as T
}

private fun NavHostController.navigateAndClear(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}





private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomDestinations = listOf(
    BottomDestination(Routes.DASHBOARD, "Inicio", Icons.Default.Home),
    BottomDestination(Routes.MOVEMENTS, "Movimientos", Icons.Default.ReceiptLong),
    BottomDestination(Routes.BUSINESS, "Negocio", Icons.Default.Business),
    BottomDestination(Routes.AI, "IA", Icons.Default.AutoAwesome),
    BottomDestination(Routes.PROFILE, "Perfil", Icons.Default.AccountCircle)
)

private val bottomRoutes = bottomDestinations.mapTo(mutableSetOf()) { it.route }

@Composable
private fun AppBottomNavigation(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar {
        bottomDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination.route) },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) }
            )
        }
    }
}

private fun NavHostController.navigateToBottomRoute(route: String) {
    navigate(route) {
        popUpTo(Routes.DASHBOARD) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
