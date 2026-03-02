package com.himank.booksassignment

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.bookMarkedDataStore by preferencesDataStore(name = "book_marked")

class BookMarkedRepository(private val context: Context) {

    fun isBookMarked(bookTitle: String): Flow<Boolean> {
        val key = booleanPreferencesKey(bookTitle)
        return context.bookMarkedDataStore.data.map { prefs ->
            prefs[key] ?: false
        }
    }

    suspend fun toggleBookmark(bookTitle: String) {
        val key = booleanPreferencesKey(bookTitle)
        context.bookMarkedDataStore.edit { prefs ->
            val current = prefs[key] ?: false
            prefs[key] = !current
        }
    }

    fun getAllBookMarkedTitles(): Flow<Set<String>> {
        return context.bookMarkedDataStore.data.map { prefs ->
            prefs.asMap()
                .filter { it.value == true }
                .map { it.key.name }
                .toSet()
        }
    }
}