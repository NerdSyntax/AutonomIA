package com.nerdsyntax.juntalucas.feature.profile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String,
    val displayName: String,
    val onboardingCompleted: Boolean = false
)