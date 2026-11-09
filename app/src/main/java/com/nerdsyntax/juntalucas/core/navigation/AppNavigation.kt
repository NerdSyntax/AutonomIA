package com.nerdsyntax.juntalucas.core.navigation

import android.net.Uri
import com.nerdsyntax.juntalucas.core.data.salesUserMessage
import androidx.compose.runtime.*
import com.nerdsyntax.juntalucas.feature.business.data.FirebaseProductPhotoRepository
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.nerdsyntax.juntalucas.core.session.BusinessSetupStatus
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
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
import com.nerdsyntax.juntalucas.feature.movements.data.FirebaseExpensesRepository
import com.nerdsyntax.juntalucas.feature.movements.data.FirebaseSalesRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.Expense
import com.nerdsyntax.juntalucas.feature.movements.ui.*
import com.nerdsyntax.juntalucas.feature.business.ui.*
import com.nerdsyntax.juntalucas.feature.business.ui.product.*
import com.nerdsyntax.juntalucas.feature.ai.ui.*
import com.nerdsyntax.juntalucas.feature.profile.ui.*

private const val EDIT_SALE_PATH = "edit_sale"
private const val EDIT_SALE_ID = "saleId"
private const val EDIT_SALE_ROUTE = "$EDIT_SALE_PATH/{$EDIT_SALE_ID}"

private const val EDIT_EXPENSE_PATH = "edit_expense"
private const val EDIT_EXPENSE_ID = "expenseId"
private const val EDIT_EXPENSE_ROUTE = "$EDIT_EXPENSE_PATH/{$EDIT_EXPENSE_ID}"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val openExpenseEditor: (String) -> Unit = { id ->
        navController.navigate("$EDIT_EXPENSE_PATH/${Uri.encode(id)}") {
            launchSingleTop = true
        }
    }

    val openProductEditor: (String) -> Unit = { id ->
        navController.navigate("${Routes.ADD_PRODUCT}/${Uri.encode(id)}") {
            launchSingleTop = true
        }
    }

    val appContext = LocalContext.current.applicationContext
    val photoRepository = remember(appContext) {
        FirebaseProductPhotoRepository(appContext)
    }

    val authRepository = AppContainer.authRepository
    val factory = remember(authRepository) { AppViewModelFactory(authRepository, AppContainer.businessRepository) }
    val sessionViewModel: SessionViewModel = viewModel(factory = factory)
    val session by sessionViewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    var showBottomBar by remember { mutableStateOf(true) }

    val startDestination = Routes.SPLASH

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        when (navController.currentDestination?.route) {
            Routes.ADD_PRODUCT, Routes.EDIT_PRODUCT -> Unit
            else -> sessionViewModel.refreshBusiness()
        }
    }
    LaunchedEffect(currentRoute) {
        if (currentRoute != Routes.AI && currentRoute != Routes.AI_CHAT) {
            showBottomBar = true
        }
    }

    LaunchedEffect(session, currentRoute) {
        if (currentRoute == null || currentRoute == Routes.SPLASH) return@LaunchedEffect

        val routeForSession = when (currentRoute) {
            EDIT_EXPENSE_ROUTE -> Routes.ADD_EXPENSE
            EDIT_SALE_ROUTE -> Routes.ADD_SALE
            else -> currentRoute
        }
        val target = sessionDestination(session, routeForSession)
        if (target != null && target != routeForSession) navController.navigateAndClear(target)
    }

    Scaffold(
        bottomBar = {
            if (session.canAccessDashboard && currentRoute in bottomRoutes && showBottomBar) {
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

            composable(Routes.DASHBOARD) { entry ->
                if (!session.canAccessDashboard) return@composable
                val vm: DashboardViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                val sharedEntry = remember(entry) {
                    navController.getBackStackEntry(navController.graph.id)
                }
                val alertsVm: BusinessViewModel = viewModel(
                    viewModelStoreOwner = sharedEntry,
                    factory = factory
                )
                val alertsState by alertsVm.uiState.collectAsStateWithLifecycle()
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }

                DashboardScreen(
                    state = state,
                    onNavigateToAi = {
                        navController.navigateToBottomRoute(Routes.AI)
                    },
                    onNavigateToMonthlyAnalysis = {
                        navController.navigateToBottomRoute(Routes.AI)
                    },
                    onPeriodApplied = vm::applyPeriod,
                    onFilterSelected = vm::selectFilter,
                    onCustomStart = vm::onCustomStart,
                    onCustomEnd = vm::onCustomEnd,
                    onRetry = vm::refresh,
                    onViewAll = { navController.navigateToBottomRoute(Routes.MOVEMENTS) },
                    notifications = {
                        StockAlertsButton(
                            alerts = alertsState.stockAlerts,
                            isLoading = alertsState.isLoading,
                            errorMessage = alertsState.errorMessage,
                            onEditProduct = openProductEditor,
                            onRetry = alertsVm::retry
                        )
                    }
                )
            }

            composable(Routes.MOVEMENTS) {
                if (!session.canAccessDashboard) return@composable
                val vm: MovementsViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                val deletion by vm.expenseDeletion.collectAsStateWithLifecycle()
                val saleDeletion by vm.saleDeletion.collectAsStateWithLifecycle()

                MovementsScreen(
                    state = state,
                    onTabSelected = vm::onTabSelected,
                    onSearchChange = vm::onSearchChange,
                    onFilterSelected = vm::onFilterSelected,
                    onRetry = vm::retry,
                    onChangeMonth = vm::changeMonth,
                    onAddClick = { isSale ->
                        navController.navigate(if (isSale) Routes.ADD_SALE else Routes.ADD_EXPENSE)
                    },
                    onEditExpense = openExpenseEditor,
                    onDeleteExpense = vm::deleteExpense,
                    expenseDeletion = deletion,
                    onClearExpenseDeletion = vm::clearExpenseDeletion,
                    onEditSale = { id ->
                        navController.navigate("$EDIT_SALE_PATH/${Uri.encode(id)}") {
                            launchSingleTop = true
                        }
                    },
                    onDeleteSale = vm::deleteSale,
                    saleDeletion = saleDeletion,
                    onClearSaleDeletion = vm::clearSaleDeletion
                )
            }

            composable(Routes.ADD_SALE) {
                if (!session.canAccessDashboard) return@composable
                val vm: AddSaleViewModel = viewModel(factory = factory)
                SaleFormRoute(
                    vm = vm,
                    onCreateProduct = { navController.navigate(Routes.ADD_PRODUCT) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = EDIT_SALE_ROUTE,
                arguments = listOf(navArgument(EDIT_SALE_ID) { type = NavType.StringType })
            ) { entry ->
                if (!session.canAccessDashboard) return@composable
                val id = entry.arguments?.getString(EDIT_SALE_ID)
                    ?.takeIf { it.isNotBlank() && '/' !in it && it != "." && it != ".." }

                if (id == null) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("La venta no es válida.")
                        Button(onClick = { navController.popBackStack() }) { Text("Volver") }
                    }
                    return@composable
                }

                val vm: AddSaleViewModel = viewModel(viewModelStoreOwner = entry) {
                    AddSaleViewModel(
                        auth = authRepository,
                        sales = FirebaseSalesRepository(),
                        catalog = AppContainer.catalogRepository,
                        savedState = createSavedStateHandle(),
                        initialSaleId = id
                    )
                }
                SaleFormRoute(
                    vm = vm,
                    onCreateProduct = {},
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ADD_EXPENSE) {
                if (!session.canAccessDashboard) return@composable
                val vm: AddExpenseViewModel = viewModel(factory = factory)
                ExpenseFormRoute(
                    vm = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = EDIT_EXPENSE_ROUTE,
                arguments = listOf(navArgument(EDIT_EXPENSE_ID) { type = NavType.StringType })
            ) { entry ->
                if (!session.canAccessDashboard) return@composable
                val id = entry.arguments?.getString(EDIT_EXPENSE_ID)
                    ?.takeIf { it.isNotBlank() && '/' !in it }

                if (id == null) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("El gasto no es válido.")
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Volver")
                        }
                    }
                    return@composable
                }

                val vm: AddExpenseViewModel = viewModel(viewModelStoreOwner = entry) {
                    AddExpenseViewModel(
                        auth = authRepository,
                        expenses = FirebaseExpensesRepository(),
                        savedState = createSavedStateHandle(),
                        initialExpenseId = id
                    )
                }

                ExpenseFormRoute(
                    vm = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.BUSINESS) { entry ->
                if (!session.canAccessDashboard) return@composable
                val sharedEntry = remember(entry) {
                    navController.getBackStackEntry(navController.graph.id)
                }
                val vm: BusinessViewModel = viewModel(
                    viewModelStoreOwner = sharedEntry,
                    factory = factory
                )
                val state by vm.uiState.collectAsStateWithLifecycle()
                val user by authRepository.currentUser.collectAsStateWithLifecycle()
                var expenseHistoryRetry by remember { mutableStateOf(0) }
                val expenseHistory = if (state.selectedTab == BusinessTab.GASTOS) {
                    RegisteredExpenseHistory(user?.uid, expenseHistoryRetry)
                } else {
                    RegisteredExpensesUiState()
                }

                BusinessScreen(
                    state = state,
                    onTabSelected = vm::onTabSelected,
                    onSearchChange = vm::onSearchChange,
                    onFilterSelected = vm::onFilterSelected,
                    onAddClick = { navController.navigate(Routes.ADD_PRODUCT) },
                    onEditProduct = openProductEditor,
                    onDeactivateProduct = vm::toggleActive,
                    onRetry = vm::retry,
                    onStartEditingBusiness = vm::startEditingBusiness,
                    onCancelEditingBusiness = vm::cancelEditingBusiness,
                    onSaveBusiness = vm::saveBusiness,
                    onBusinessNameChange = vm::onBusinessNameChange,
                    onBusinessRubroChange = vm::onBusinessRubroChange,
                    onBusinessRegionChange = vm::onBusinessRegionChange,
                    onBusinessComunaChange = vm::onBusinessComunaChange,
                    onBusinessMetaChange = vm::onBusinessMetaChange,
                    registeredExpensesState = expenseHistory,
                    onRetryRegisteredExpenses = { expenseHistoryRetry += 1 },
                    onEditRegisteredExpense = openExpenseEditor,
                    onAddRegisteredExpense = {
                        navController.navigate(Routes.ADD_EXPENSE) {
                            launchSingleTop = true
                        }
                    }
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
                    onSave = { vm.saveProduct(photoRepository::upload) },
                    onPhotoChange = vm::onPhotoChange,
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
                    onSave = { vm.saveProduct(photoRepository::upload) },
                    onPhotoChange = vm::onPhotoChange,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Routes.AI) {
                if (!session.canAccessDashboard) return@composable
                val vm: AiViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()

                AiScreen(
                    state = state,
                    vm = vm,
                    onToggleBottomBar = { showBottomBar = it }
                )
            }

            composable(Routes.AI_CHAT) { navBackStackEntry ->
                if (!session.canAccessDashboard) return@composable

                val parentEntry = remember(key1 = navBackStackEntry) { navController.getBackStackEntry(Routes.AI) }
                val vm: AiViewModel = viewModel(viewModelStoreOwner = parentEntry, factory = factory)
                val state by vm.uiState.collectAsStateWithLifecycle()

                AiScreen(
                    state = state,
                    vm = vm,
                    onToggleBottomBar = { showBottomBar = it }
                )
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
                    state = state,
                    onChangePassword = vm::sendPasswordReset,
                    onLogout = vm::logout,
                    onDeleteAccount = vm::deleteAccount,
                    onBack = navController::popBackStack
                )
            }
        }
    }
}

@Composable
private fun RegisteredExpenseHistory(
    uid: String?,
    retry: Int
): RegisteredExpensesUiState = key(uid, retry) {
    val history by produceState(RegisteredExpensesUiState()) {
        if (uid == null) {
            value = RegisteredExpensesUiState(
                isLoading = false,
                errorMessage = "Inicia sesión para ver tus gastos."
            )
            return@produceState
        }

        val auth = AppContainer.authRepository
        try {
            AppContainer.expensesRepository.observeHistory(uid).collect { result ->
                if (auth.currentUser.value?.uid != uid) return@collect

                value = result.fold(
                    onSuccess = { data ->
                        RegisteredExpensesUiState(
                            items = data.value.sortedWith(
                                compareByDescending<Expense> { it.draft.expenseDate }
                                    .thenByDescending { it.createdAtMillis }
                                    .thenBy { it.draft.id }
                            ),
                            isLoading = false,
                            fromCache = data.fromCache
                        )
                    },
                    onFailure = { error ->
                        value.copy(isLoading = false, errorMessage = error.salesUserMessage())
                    }
                )
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (auth.currentUser.value?.uid == uid) {
                value = value.copy(isLoading = false, errorMessage = error.salesUserMessage())
            }
        }
    }
    history
}

@Composable
private fun SaleFormRoute(
    vm: AddSaleViewModel,
    onCreateProduct: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val sale by vm.editingSale.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onNavigateBack()
    }

    if (vm.isEditing && (sale == null || state.isCatalogLoading)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val error = state.catalogError
            if (error == null) {
                CircularProgressIndicator()
                Text("Cargando venta…")
            } else {
                Text(error)
                Button(onClick = vm::retryCatalog) { Text("Reintentar") }
            }
            Button(onClick = onNavigateBack) { Text("Volver") }
        }
        return
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
        onCreateProduct = onCreateProduct,
        onSave = vm::save,
        onNavigateBack = onNavigateBack,
        editingProductName = sale?.productName
    )
}

@Composable
private fun ExpenseFormRoute(
    vm: AddExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val isLoading by vm.isLoadingExpense.collectAsStateWithLifecycle()
    val suggestionsState by vm.suggestionsState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onNavigateBack()
    }

    if (isLoading) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text("Cargando gasto…")
            Button(onClick = onNavigateBack) { Text("Volver") }
        }
        return
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
        onNavigateBack = onNavigateBack,
        suggestions = if (vm.isEditing) null else suggestionsState.items
    )
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
