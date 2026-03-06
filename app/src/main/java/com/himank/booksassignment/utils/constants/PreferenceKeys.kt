package com.himank.booksassignment.utils.constants

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object PreferenceKeys {
    fun quantityKey(title: String) =
        intPreferencesKey("qty_$title")

    fun bookmarkKey(title: String) =
        booleanPreferencesKey("bookmark_$title")
}