package com.himank.booksassignment

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.bookDataStore by preferencesDataStore(name = "book_quantity")

class BookQuantityRepository(private val context: Context) {

    fun getQuantity(bookTitle: String): Flow<Int> {
        val key = intPreferencesKey(bookTitle)
        return context.bookDataStore.data.map { prefs ->
            prefs[key] ?: 0
        }
    }

    suspend fun increment(bookTitle: String) {
        val key = intPreferencesKey(bookTitle)
        context.bookDataStore.edit { prefs ->
            prefs[key] = (prefs[key] ?: 0) + 1
        }
    }

    suspend fun decrement(bookTitle: String) {
        val key = intPreferencesKey(bookTitle)
        context.bookDataStore.edit { prefs ->
            val current = prefs[key] ?: 0
            if (current > 0) prefs[key] = current - 1
        }
    }

    suspend fun reset(bookTitle: String) {
        val key = intPreferencesKey(bookTitle)
        context.bookDataStore.edit { prefs ->
            prefs[key] = 0
        }
    }
}
