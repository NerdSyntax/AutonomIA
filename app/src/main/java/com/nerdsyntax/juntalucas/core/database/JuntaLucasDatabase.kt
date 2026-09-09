package com.nerdsyntax.juntalucas.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nerdsyntax.juntalucas.feature.profile.data.UserProfileDao
import com.nerdsyntax.juntalucas.feature.profile.data.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JuntaLucasDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
}