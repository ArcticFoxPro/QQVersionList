/*
    QQ Versions Tool for Android™
    Copyright (C) 2023 klxiaoniu

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.xiaoniu.qqversionlist.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.xiaoniu.qqversionlist.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object DataStoreUtil {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "data",
        produceMigrations = { context ->
            listOf(
                SharedPreferencesMigration(context, "data")
            )
        })

    private val dataStore: DataStore<Preferences> by lazy {
        Application.instance.dataStore
    }

    @Throws(IOException::class)
    fun getInt(key: String, defValue: Int): Int {
        return runBlocking {
            dataStore.data.firstOrNull()?.let { preferences ->
                preferences[intPreferencesKey(key)] ?: defValue
            } ?: defValue
        }
    }

    @Throws(IOException::class)
    fun putInt(key: String, value: Int) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(key)] = value
            }
        }
    }

    @Throws(IOException::class)
    fun getString(key: String, defValue: String): String {
        return runBlocking {
            dataStore.data.firstOrNull()?.let { preferences ->
                preferences[stringPreferencesKey(key)] ?: defValue
            } ?: defValue
        }
    }

    @Throws(IOException::class)
    fun putString(key: String, value: String) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey(key)] = value
            }
        }
    }

    @Throws(IOException::class)
    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return runBlocking {
            dataStore.data.firstOrNull()?.let { preferences ->
                preferences[booleanPreferencesKey(key)] ?: defValue
            } ?: defValue
        }
    }

    @Throws(IOException::class)
    fun putBoolean(key: String, value: Boolean) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(key)] = value
            }
        }
    }

    @Throws(IOException::class)
    fun deletePreference(key: String) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences.remove(stringPreferencesKey(key))
                preferences.remove(intPreferencesKey(key))
                preferences.remove(booleanPreferencesKey(key))
                preferences.remove(floatPreferencesKey(key))
                preferences.remove(longPreferencesKey(key))
                preferences.remove(doublePreferencesKey(key))
            }
        }
    }


    fun getIntAsync(key: String, defValue: Int): Deferred<Int> {
        return CoroutineScope(Dispatchers.IO).async {
            dataStore.data.firstOrNull()?.let { preferences ->
                preferences[intPreferencesKey(key)] ?: defValue
            } ?: defValue
        }
    }


    fun putIntAsync(key: String, value: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(key)] = value
            }
        }
    }


    fun getStringAsync(key: String, defValue: String): Deferred<String> {
        return CoroutineScope(Dispatchers.IO).async {
            dataStore.data.firstOrNull()?.let { preferences ->
                preferences[stringPreferencesKey(key)] ?: defValue
            } ?: defValue
        }
    }

    fun putStringAsync(key: String, value: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey(key)] = value
            }
        }
    }

    fun getBooleanAsync(key: String, defValue: Boolean): Deferred<Boolean> {
        return CoroutineScope(Dispatchers.IO).async {
            dataStore.data.firstOrNull()?.let { preferences ->
                preferences[booleanPreferencesKey(key)] ?: defValue
            } ?: defValue
        }
    }


    fun putBooleanAsync(key: String, value: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(key)] = value
            }
        }
    }


    fun deletePreferenceAsync(key: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { preferences ->
                preferences.remove(stringPreferencesKey(key))
                preferences.remove(intPreferencesKey(key))
                preferences.remove(booleanPreferencesKey(key))
                preferences.remove(floatPreferencesKey(key))
                preferences.remove(longPreferencesKey(key))
                preferences.remove(doublePreferencesKey(key))
            }
        }
    }
}

