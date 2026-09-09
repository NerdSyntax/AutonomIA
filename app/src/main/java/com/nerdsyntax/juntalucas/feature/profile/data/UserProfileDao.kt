package com.nerdsyntax.juntalucas.feature.profile.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query(
        """
        SELECT *
        FROM user_profiles
        WHERE userId = :userId
        LIMIT 1
        """
    )
    fun observeByUserId(userId: String): Flow<UserProfileEntity?>

    @Query(
        """
        SELECT *
        FROM user_profiles
        WHERE userId = :userId
        LIMIT 1
        """
    )
    suspend fun getByUserId(userId: String): UserProfileEntity?

    @Upsert
    suspend fun upsert(profile: UserProfileEntity)

    @Query(
        """
        UPDATE user_profiles
        SET onboardingCompleted = :completed
        WHERE userId = :userId
        """
    )
    suspend fun updateOnboardingCompleted(
        userId: String,
        completed: Boolean
    )

    @Query(
        """
        DELETE FROM user_profiles
        WHERE userId = :userId
        """
    )
    suspend fun deleteByUserId(userId: String)
}