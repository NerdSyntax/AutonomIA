package com.nerdsyntax.juntalucas.feature.profile.domain

data class UserProfile(
    val userId: String,
    val displayName: String,
    val onboardingCompleted: Boolean = false
)