package com.example.storynest.dataLocal

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: UserPreferences? = null
        private val Context.dataStore by preferencesDataStore(name = "user_prefs")
        // Keys
        private val USERNAME = stringPreferencesKey("username")
        private val NAME = stringPreferencesKey("name")
        private val SURNAME = stringPreferencesKey("surname")
        private val USER_ID = stringPreferencesKey("id")
        private val TOKEN = stringPreferencesKey("token")
        private val EMAIL = stringPreferencesKey("email")
        private val PROFILE_PHOTO = stringPreferencesKey("profile_photo")


        // Singleton getInstance metodu
        fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    val username: Flow<String?> = context.dataStore.data.map { it[USERNAME] }
    val name: Flow<String?> = context.dataStore.data.map { it[NAME] }
    val surname: Flow<String?> = context.dataStore.data.map { it[SURNAME] }
    val id: Flow<Long?> = context.dataStore.data.map { it[USER_ID]?.toLongOrNull() } // String -> Long
    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN] }
    val email: Flow<String?> = context.dataStore.data.map { it[EMAIL] }
    val profilePhoto: Flow<String?> = context.dataStore.data.map { it[PROFILE_PHOTO] }

    suspend fun saveUser(
        username: String,
        token: String,
        name: String,
        surname: String,
        id: Long,
        email: String,
        profilePhoto: String?
    ) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME] = username
            prefs[TOKEN] = token
            prefs[NAME] = name
            prefs[SURNAME] = surname
            prefs[USER_ID] = id.toString()
            prefs[EMAIL] = email
            prefs[PROFILE_PHOTO] = profilePhoto ?: ""
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
