package com.nerdsyntax.juntalucas.feature.profile.domain

import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {

    fun observeProfile(userId: String): Flow<UserProfile?>

    suspend fun getProfile(userId: String): UserProfile?

    suspend fun saveProfile(profile: UserProfile)

    suspend fun setOnboardingCompleted(
        userId: String,
        completed: Boolean
    )

    suspend fun deleteProfile(userId: String)
}