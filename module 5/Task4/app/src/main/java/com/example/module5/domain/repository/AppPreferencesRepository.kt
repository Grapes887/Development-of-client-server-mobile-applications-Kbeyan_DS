package com.example.module5.domain.repository

import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    fun observeCompletedColorEnabled(): Flow<Boolean>
    suspend fun setCompletedColorEnabled(enabled: Boolean)
    suspend fun isSeedImported(): Boolean
    suspend fun setSeedImported()
}
