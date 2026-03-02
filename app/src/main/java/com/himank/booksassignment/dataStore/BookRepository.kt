package com.himank.booksassignment.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "book_preferences")

class BookRepository(private val context: Context) {

    private fun quantityKey(title: String) = intPreferencesKey("qty_$title")
    private fun bookmarkKey(title: String) = booleanPreferencesKey("bookmark_$title")

    fun getQuantity(bookTitle: String): Flow<Int> {
        return context.dataStore.data.map { prefs ->
            prefs[quantityKey(bookTitle)] ?: 0
        }
    }

    suspend fun increment(bookTitle: String) {
        context.dataStore.edit { prefs ->
            val key = quantityKey(bookTitle)
            val current = prefs[key] ?: 0
            prefs[key] = current + 1
        }
    }

    suspend fun decrement(bookTitle: String) {
        context.dataStore.edit { prefs ->
            val key = quantityKey(bookTitle)
            val current = prefs[key] ?: 0
            if (current > 0) {
                prefs[key] = current - 1
            }
        }
    }

    suspend fun reset(bookTitle: String) {
        context.dataStore.edit { prefs ->
            prefs[quantityKey(bookTitle)] = 0
        }
    }

    fun isBookMarked(bookTitle: String): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[bookmarkKey(bookTitle)] ?: false
        }
    }

    suspend fun toggleBookmark(bookTitle: String) {
        context.dataStore.edit { prefs ->
            val key = bookmarkKey(bookTitle)
            val current = prefs[key] ?: false
            prefs[key] = !current
        }
    }

    fun getAllBookMarkedTitles(): Flow<Set<String>> {
        return context.dataStore.data.map { prefs ->
            prefs.asMap().keys
                .filter { it.name.startsWith("bookmark_") }
                .filter { key -> prefs[key] == true }
                .map { it.name.removePrefix("bookmark_") }
                .toSet()
        }
    }
}
