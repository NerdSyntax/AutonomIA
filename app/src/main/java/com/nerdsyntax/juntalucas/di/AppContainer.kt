package com.nerdsyntax.juntalucas.di

import com.nerdsyntax.juntalucas.feature.business.data.FirebaseBusinessRepository
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository

object AppContainer {
    val businessRepository: BusinessRepository by lazy { FirebaseBusinessRepository() }
}
