package com.nerdsyntax.juntalucas.core.navigation

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.padding
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.nerdsyntax.juntalucas.core.session.SessionViewModel
import com.nerdsyntax.juntalucas.feature.auth.data.FirebaseAuthRepository
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.auth.ui.account.*
import com.nerdsyntax.juntalucas.feature.auth.ui.login.*
import com.nerdsyntax.juntalucas.feature.auth.ui.recovery.*
import com.nerdsyntax.juntalucas.feature.auth.ui.register.*
import com.nerdsyntax.juntalucas.feature.auth.ui.verify.*
import com.nerdsyntax.juntalucas.feature.dashboard.ui.*
import com.nerdsyntax.juntalucas.feature.movements.ui.*
import com.nerdsyntax.juntalucas.feature.business.ui.*
import com.nerdsyntax.juntalucas.feature.ai.ui.*
import com.nerdsyntax.juntalucas.feature.profile.ui.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authRepository: AuthRepository = remember { FirebaseAuthRepository() }
    val factory = remember(authRepository) { AppViewModelFactory(authRepository) }
    val sessionViewModel: SessionViewModel = viewModel(factory = factory)
    val session by sessionViewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val startDestination = remember {
        when {
            !session.isAuthenticated -> Routes.LOGIN
            !session.isEmailVerified -> Routes.VERIFY_EMAIL
            else -> Routes.DASHBOARD
        }
    }

    LaunchedEffect(session.currentUser, currentRoute) {
        if (currentRoute == null) return@LaunchedEffect
        val target = when {
            !session.isAuthenticated && currentRoute !in publicRoutes -> Routes.LOGIN
            session.isAuthenticated && !session.isEmailVerified && currentRoute != Routes.VERIFY_EMAIL ->
                Routes.VERIFY_EMAIL
            session.isAuthenticated && session.isEmailVerified && currentRoute in authRoutes ->
                Routes.DASHBOARD
            else -> null
        }
        if (target != null && target != currentRoute) navController.navigateAndClear(target)
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomRoutes) {
                AppBottomNavigation(currentRoute, navController::navigateToBottomRoute)
            }
        }
    ) { innerPadding ->
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Routes.LOGIN) {
            val vm: LoginViewModel = viewModel(factory = factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            LoginScreen(
                state, vm::onEmailChange, vm::onPasswordChange, vm::login,
                onRegisterClick = { navController.navigate(Routes.REGISTER) },
                onForgotPasswordClick = { navController.navigate(Routes.FORGOT_PASSWORD) }
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
            VerifyEmailScreen(state, vm::checkVerification, vm::resendVerification, vm::logout)
        }
        composable(Routes.DASHBOARD) {
            val vm: DashboardViewModel = viewModel(factory = factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            DashboardScreen(state)
        }
        composable(Routes.MOVEMENTS) {
            val vm: MovementsViewModel = viewModel(factory = factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            MovementsScreen(state)
        }
        composable(Routes.BUSINESS) {
            val vm: BusinessViewModel = viewModel(factory = factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            BusinessScreen(state)
        }
        composable(Routes.AI) {
            val vm: AiViewModel = viewModel(factory = factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            AiScreen(state)
        }
        composable(Routes.PROFILE) {
            val vm: ProfileViewModel = viewModel(factory = factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            ProfileScreen(state) { navController.navigate(Routes.ACCOUNT) }
        }
        composable(Routes.ACCOUNT) {
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

private class AppViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(SessionViewModel::class.java) -> SessionViewModel(authRepository)
        modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(authRepository)
        modelClass.isAssignableFrom(RegisterViewModel::class.java) -> RegisterViewModel(authRepository)
        modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java) -> ForgotPasswordViewModel(authRepository)
        modelClass.isAssignableFrom(VerifyEmailViewModel::class.java) -> VerifyEmailViewModel(authRepository)
        modelClass.isAssignableFrom(AccountViewModel::class.java) -> AccountViewModel(authRepository)
        modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(authRepository)
        modelClass.isAssignableFrom(MovementsViewModel::class.java) -> MovementsViewModel()
        modelClass.isAssignableFrom(BusinessViewModel::class.java) -> BusinessViewModel()
        modelClass.isAssignableFrom(AiViewModel::class.java) -> AiViewModel()
        modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(authRepository)
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    } as T
}

private fun NavHostController.navigateAndClear(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private val publicRoutes = setOf(Routes.LOGIN, Routes.REGISTER, Routes.FORGOT_PASSWORD)
private val authRoutes = publicRoutes + Routes.VERIFY_EMAIL

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
