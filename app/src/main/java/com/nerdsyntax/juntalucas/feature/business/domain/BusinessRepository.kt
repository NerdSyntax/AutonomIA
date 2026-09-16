package com.nerdsyntax.juntalucas.feature.business.domain

interface BusinessRepository {
    suspend fun saveBusiness(business: Business): Result<Unit>
    // null means that the authenticated user has not created a business yet.
    suspend fun getBusiness(): Result<Business?>
}

class BusinessAuthenticationException : IllegalStateException("An authenticated user is required")
