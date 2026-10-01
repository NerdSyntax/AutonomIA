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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.nerdsyntax.juntalucas.core.session.SessionViewModel
import com.nerdsyntax.juntalucas.di.AppContainer
import com.nerdsyntax.juntalucas.di.AppViewModelFactory
import com.nerdsyntax.juntalucas.di.ProductViewModelFactory
import com.nerdsyntax.juntalucas.feature.auth.ui.account.*
import com.nerdsyntax.juntalucas.feature.auth.ui.login.*
import com.nerdsyntax.juntalucas.feature.auth.ui.recovery.*
import com.nerdsyntax.juntalucas.feature.auth.ui.register.*
import com.nerdsyntax.juntalucas.feature.auth.ui.verify.*
import com.nerdsyntax.juntalucas.feature.auth.ui.welcome.*
import com.nerdsyntax.juntalucas.feature.onboarding.ui.*
import com.nerdsyntax.juntalucas.feature.dashboard.ui.*
import com.nerdsyntax.juntalucas.feature.movements.ui.*
import com.nerdsyntax.juntalucas.feature.movements.domain.PaymentMethod
import com.nerdsyntax.juntalucas.feature.business.ui.*
import com.nerdsyntax.juntalucas.feature.business.ui.product.*
import com.nerdsyntax.juntalucas.feature.ai.ui.*
import com.nerdsyntax.juntalucas.feature.profile.ui.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authRepository = AppContainer.authRepository
    val factory = remember(authRepository) { AppViewModelFactory(authRepository, AppContainer.businessRepository) }
    val sessionViewModel: SessionViewModel = viewModel(factory = factory)
    val session by sessionViewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val startDestination = Routes.SPLASH

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { sessionViewModel.refreshBusiness() }

    LaunchedEffect(session, currentRoute) {
        if (currentRoute == null || currentRoute == Routes.SPLASH) return@LaunchedEffect

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

            composable(Routes.SPLASH) {
                SplashScreen(
                    onTimeout = {
                        val nextRoute = when {
                            !session.isAuthenticated -> Routes.WELCOME
                            !session.isEmailVerified -> Routes.VERIFY_EMAIL
                            else -> Routes.SESSION_CHECK
                        }
                        navController.navigate(nextRoute) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.SESSION_CHECK) {
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
            composable(Routes.WELCOME) {
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


            composable(Routes.BUSINESS_INFO) {
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
                            navController.navigate(Routes.ACTIVITY_SELECTION)
                        }
                    }
                )
            }

            composable(Routes.ACTIVITY_SELECTION) { navBackStackEntry ->
                if (!session.isEmailVerified || session.businessSetupStatus != BusinessSetupStatus.REQUIRED) return@composable
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry(Routes.BUSINESS_INFO) }
                val vm: OnboardingViewModel = viewModel(parentEntry, factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                ActivitySelectionScreen(
                    state = state,
                    onTipoActividadChange = vm::onTipoActividadChange,
                    onNavigateBack = { navController.popBackStack() },
                    onContinueClick = { navController.navigate(Routes.STARTING_POINT) }
                )
            }

            composable(Routes.STARTING_POINT) { navBackStackEntry ->
                if (!session.isEmailVerified || session.businessSetupStatus != BusinessSetupStatus.REQUIRED) return@composable
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry(Routes.BUSINESS_INFO) }
                val vm: OnboardingViewModel = viewModel(parentEntry, factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.isSuccess) {
                    if (state.isSuccess) {
                        navController.navigateAndClear(Routes.SESSION_CHECK)
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
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }

                DashboardScreen(
                    state = state,
                    onNavigateToAi = {
                        navController.navigateToBottomRoute(Routes.AI)
                    },
                    onFilterSelected = vm::selectFilter,
                    onCustomStart = vm::onCustomStart,
                    onCustomEnd = vm::onCustomEnd,
                    onRetry = vm::refresh,
                    onViewAll = { navController.navigateToBottomRoute(Routes.MOVEMENTS) }
                )
            }
            composable(Routes.MOVEMENTS) {
                if (!session.canAccessDashboard) return@composable
                val vm: MovementsViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                MovementsScreen(
                    state = state,
                    onTabSelected = vm::onTabSelected,
                    onSearchChange = vm::onSearchChange,
                    onFilterSelected = vm::onFilterSelected,
                    onRetry = vm::retry,
                    onChangeMonth = vm::changeMonth,
                    onAddClick = { isSale ->
                        navController.navigate(if (isSale) Routes.ADD_SALE else Routes.ADD_EXPENSE)
                    }
                )
            }
            composable(Routes.ADD_SALE) {
                if (!session.canAccessDashboard) return@composable
                val vm: AddSaleViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.isSuccess) {
                    if (state.isSuccess) navController.popBackStack()
                }
                AddSaleScreen(
                    state = state,
                    onDateChange = vm::onDateChange,
                    onProductSelected = vm::onProductSelected,
                    onQuantityChange = vm::onQuantityChange,
                    onPriceChange = vm::onPriceChange,
                    onDiscountChange = vm::onDiscountChange,
                    onPaymentChange = vm::onPaymentChange,
                    onNoteChange = vm::onNoteChange,
                    onRetryCatalog = vm::retryCatalog,
                    onCreateProduct = { navController.navigate(Routes.ADD_PRODUCT) },
                    onSave = vm::save,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ADD_EXPENSE) {
                if (!session.canAccessDashboard) return@composable
                val vm: AddExpenseViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.isSuccess) {
                    if (state.isSuccess) navController.popBackStack()
                }
                AddExpenseScreen(
                    state = state,
                    onDescriptionChange = vm::onDescriptionChange,
                    onCategoryChange = vm::onCategoryChange,
                    onAmountChange = vm::onAmountChange,
                    onDateChange = vm::onDateChange,
                    onTypeChange = vm::onTypeChange,
                    onPaymentChange = vm::onPaymentChange,
                    onNoteChange = vm::onNoteChange,
                    onSave = vm::save,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.BUSINESS) {
                if (!session.canAccessDashboard) return@composable
                val vm: BusinessViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                BusinessScreen(
                    state = state,
                    onTabSelected = vm::onTabSelected,
                    onSearchChange = vm::onSearchChange,
                    onFilterSelected = vm::onFilterSelected,
                    onAddClick = { navController.navigate(Routes.ADD_PRODUCT) },
                    onEditProduct = { id -> navController.navigate("${Routes.ADD_PRODUCT}/$id") },
                    onDeactivateProduct = vm::toggleActive,
                    onRetry = vm::retry,
                    onStartEditingBusiness = vm::startEditingBusiness,
                    onCancelEditingBusiness = vm::cancelEditingBusiness,
                    onSaveBusiness = vm::saveBusiness,
                    onBusinessNameChange = vm::onBusinessNameChange,
                    onBusinessRubroChange = vm::onBusinessRubroChange,
                    onBusinessRegionChange = vm::onBusinessRegionChange,
                    onBusinessComunaChange = vm::onBusinessComunaChange
                )
            }

            composable(Routes.ADD_PRODUCT) {
                if (!session.canAccessDashboard) return@composable
                val vm: AddProductViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.isSuccess) {
                    if (state.isSuccess) navController.popBackStack()
                }

                AddProductScreen(
                    state = state,
                    onIsProductChange = vm::onIsProductChange,
                    onNombreChange = vm::onNombreChange,
                    onCategoriaChange = vm::onCategoriaChange,
                    onPrecioChange = vm::onPrecioChange,
                    onCostoChange = vm::onCostoChange,
                    onDescripcionChange = vm::onDescripcionChange,
                    onStockActualChange = vm::onStockActualChange,
                    onStockMinimoChange = vm::onStockMinimoChange,
                    onUnidadChange = vm::onUnidadChange,
                    onIsActiveChange = vm::onIsActiveChange,
                    onSave = vm::saveProduct,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.EDIT_PRODUCT) { entry ->
                if (!session.canAccessDashboard) return@composable
                val productId = entry.arguments?.getString("productId")
                val vm: AddProductViewModel = viewModel(factory = ProductViewModelFactory(authRepository, AppContainer.catalogRepository, productId))
                val state by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.isSuccess) { if (state.isSuccess) navController.popBackStack() }
                AddProductScreen(
                    state = state,
                    onIsProductChange = vm::onIsProductChange,
                    onNombreChange = vm::onNombreChange,
                    onCategoriaChange = vm::onCategoriaChange,
                    onPrecioChange = vm::onPrecioChange,
                    onCostoChange = vm::onCostoChange,
                    onDescripcionChange = vm::onDescripcionChange,
                    onStockActualChange = vm::onStockActualChange,
                    onStockMinimoChange = vm::onStockMinimoChange,
                    onUnidadChange = vm::onUnidadChange,
                    onIsActiveChange = vm::onIsActiveChange,
                    onSave = vm::saveProduct,
                    onNavigateBack = { navController.popBackStack() }
                )
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

                ProfileScreen(
                    state = state,
                    vm = vm,
                    onNavigateToAccount = { navController.navigate(Routes.ACCOUNT) }
                )
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

private fun NavHostController.navigateAndClear(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavHostController.navigateToBottomRoute(route: String) {
    navigate(route) {
        popUpTo(Routes.DASHBOARD) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
