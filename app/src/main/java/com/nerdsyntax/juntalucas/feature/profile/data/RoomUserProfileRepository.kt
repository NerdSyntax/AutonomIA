package com.nerdsyntax.juntalucas.feature.profile.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nerdsyntax.juntalucas.feature.profile.domain.UserProfile
import com.nerdsyntax.juntalucas.feature.profile.domain.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class RoomUserProfileRepository(
    private val userProfileDao: UserProfileDao
) : UserProfileRepository {

    override fun observeProfile(
        userId: String
    ): Flow<UserProfile?> {
        return userProfileDao
            .observeByUserId(userId)
            .map { entity ->
                entity?.toDomain()
            }
    }

    override suspend fun getProfile(
        userId: String
    ): UserProfile? {
        return userProfileDao
            .getByUserId(userId)
            ?.toDomain()
    }

    override suspend fun saveProfile(
        profile: UserProfile
    ) {
        userProfileDao.upsert(
            profile.toEntity()
        )
        runCatching {
            val datos = mapOf(
                "userId" to profile.userId,
                "displayName" to profile.displayName,
                "name" to profile.displayName,
                "nombre" to profile.displayName,
                "onboardingCompleted" to profile.onboardingCompleted
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(profile.userId)
                .set(datos, SetOptions.merge())
                .await()
        }
    }

    override suspend fun setOnboardingCompleted(
        userId: String,
        completed: Boolean
    ) {
        userProfileDao.updateOnboardingCompleted(
            userId = userId,
            completed = completed
        )
        runCatching {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .set(mapOf("onboardingCompleted" to completed), SetOptions.merge())
                .await()
        }
    }

    override suspend fun deleteProfile(
        userId: String
    ) {
        userProfileDao.deleteByUserId(userId)
    }
}

private fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        userId = userId,
        displayName = displayName,
        onboardingCompleted = onboardingCompleted
    )
}

private fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        userId = userId,
        displayName = displayName,
        onboardingCompleted = onboardingCompleted
    )
}