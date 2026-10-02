package com.nerdsyntax.juntalucas.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nerdsyntax.juntalucas.core.session.SessionViewModel
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository

import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.auth.ui.account.AccountViewModel
import com.nerdsyntax.juntalucas.feature.auth.ui.login.LoginViewModel
import com.nerdsyntax.juntalucas.feature.auth.ui.recovery.ForgotPasswordViewModel
import com.nerdsyntax.juntalucas.feature.auth.ui.register.RegisterViewModel
import com.nerdsyntax.juntalucas.feature.auth.ui.verify.VerifyEmailViewModel
import com.nerdsyntax.juntalucas.feature.onboarding.ui.OnboardingViewModel
import com.nerdsyntax.juntalucas.feature.dashboard.ui.DashboardViewModel
import com.nerdsyntax.juntalucas.feature.movements.ui.MovementsViewModel
import com.nerdsyntax.juntalucas.feature.movements.ui.AddSaleViewModel
import com.nerdsyntax.juntalucas.feature.movements.domain.SalesRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.ExpensesRepository
import com.nerdsyntax.juntalucas.feature.movements.ui.AddExpenseViewModel
import com.nerdsyntax.juntalucas.feature.business.domain.CatalogRepository
import androidx.lifecycle.SavedStateHandle
import com.nerdsyntax.juntalucas.feature.business.ui.BusinessViewModel
import com.nerdsyntax.juntalucas.feature.business.ui.product.AddProductViewModel
import com.nerdsyntax.juntalucas.feature.ai.ui.AiViewModel
import com.nerdsyntax.juntalucas.feature.profile.ui.ProfileViewModel

class AppViewModelFactory(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessRepository
    , private val salesRepository: SalesRepository = AppContainer.salesRepository
    , private val catalogRepository: CatalogRepository = AppContainer.catalogRepository
    , private val expensesRepository: ExpensesRepository = AppContainer.expensesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(SessionViewModel::class.java) -> SessionViewModel(authRepository, businessRepository)
        modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(authRepository)
        modelClass.isAssignableFrom(RegisterViewModel::class.java) -> RegisterViewModel(authRepository)
        modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java) -> ForgotPasswordViewModel(authRepository)
        modelClass.isAssignableFrom(VerifyEmailViewModel::class.java) -> VerifyEmailViewModel(authRepository)
        modelClass.isAssignableFrom(AccountViewModel::class.java) -> AccountViewModel(authRepository)
        modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(authRepository, businessRepository, salesRepository, expensesRepository)
        modelClass.isAssignableFrom(MovementsViewModel::class.java) -> MovementsViewModel(authRepository, salesRepository, expensesRepository)
        modelClass.isAssignableFrom(AddExpenseViewModel::class.java) -> AddExpenseViewModel(authRepository, expensesRepository)
        modelClass.isAssignableFrom(AddSaleViewModel::class.java) -> AddSaleViewModel(authRepository, salesRepository, catalogRepository)
        modelClass.isAssignableFrom(BusinessViewModel::class.java) -> BusinessViewModel(authRepository, businessRepository, catalogRepository, salesRepository)
        modelClass.isAssignableFrom(AddProductViewModel::class.java) -> AddProductViewModel(authRepository, catalogRepository)
        modelClass.isAssignableFrom(AiViewModel::class.java) -> AiViewModel()
        modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(authRepository, businessRepository)
        modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> OnboardingViewModel(businessRepository)
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    } as T
}
