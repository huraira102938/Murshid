package com.huraira.murshid.data.remote

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.syncDataStore by preferencesDataStore(name = "murshid_sync_prefs")

/**
 * Tracks the last time each collection was synced so regular app opens only pull
 * documents newer than that timestamp instead of re-fetching everything. A full sync
 * (timestamp = 0) should stay rare/explicit — see Prompt 2's sync rules.
 */
class LastSyncStore(private val context: Context) {

    private fun keyFor(collection: String) = longPreferencesKey("last_sync_$collection")

    suspend fun get(collection: String): Long =
        context.syncDataStore.data.first()[keyFor(collection)] ?: 0L

    suspend fun set(collection: String, timestampMillis: Long) {
        context.syncDataStore.edit { it[keyFor(collection)] = timestampMillis }
    }
}
