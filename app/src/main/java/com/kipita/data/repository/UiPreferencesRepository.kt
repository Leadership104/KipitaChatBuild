package com.kipita.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.uiPrefsDataStore by preferencesDataStore(name = "ui_prefs")

@Singleton
class UiPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val showDestinationsKey = booleanPreferencesKey("show_destinations_tab")

    val showDestinations: Flow<Boolean> = context.uiPrefsDataStore.data
        .map { prefs -> prefs[showDestinationsKey] ?: true }

    suspend fun setShowDestinations(enabled: Boolean) {
        context.uiPrefsDataStore.edit { prefs ->
            prefs[showDestinationsKey] = enabled
        }
    }
}
