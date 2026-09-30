package com.nerdsyntax.juntalucas.di

import com.nerdsyntax.juntalucas.feature.auth.data.FirebaseAuthRepository
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.data.FirebaseBusinessRepository
import com.nerdsyntax.juntalucas.feature.business.data.FirebaseCatalogRepository
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository
import com.nerdsyntax.juntalucas.feature.business.domain.CatalogRepository
import com.nerdsyntax.juntalucas.feature.movements.data.FirebaseSalesRepository
import com.nerdsyntax.juntalucas.feature.movements.data.FirebaseExpensesRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.SalesRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.ExpensesRepository

object AppContainer {
    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }
    val businessRepository: BusinessRepository by lazy { FirebaseBusinessRepository() }
    val catalogRepository: CatalogRepository by lazy { FirebaseCatalogRepository() }
    val salesRepository: SalesRepository by lazy { FirebaseSalesRepository() }
    val expensesRepository: ExpensesRepository by lazy { FirebaseExpensesRepository() }
}
