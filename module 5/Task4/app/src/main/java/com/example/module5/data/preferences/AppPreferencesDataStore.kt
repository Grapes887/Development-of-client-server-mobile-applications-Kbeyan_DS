package com.example.module5.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.module5.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val TODO_SETTINGS = "todo_settings"
private val Context.dataStore by preferencesDataStore(name = TODO_SETTINGS)

class AppPreferencesDataStore(
    private val context: Context
) : AppPreferencesRepository {
    override fun observeCompletedColorEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[Keys.COMPLETED_COLOR_ENABLED] ?: true
        }
    }

    override suspend fun setCompletedColorEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.COMPLETED_COLOR_ENABLED] = enabled
        }
    }

    override suspend fun isSeedImported(): Boolean {
        return context.dataStore.data.first()[Keys.SEED_IMPORTED] ?: false
    }

    override suspend fun setSeedImported() {
        context.dataStore.edit { preferences ->
            preferences[Keys.SEED_IMPORTED] = true
        }
    }

    private object Keys {
        val COMPLETED_COLOR_ENABLED: Preferences.Key<Boolean> =
            booleanPreferencesKey("completed_color_enabled")
        val SEED_IMPORTED: Preferences.Key<Boolean> =
            booleanPreferencesKey("seed_imported")
    }
}
