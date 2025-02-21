package com.example.progetto.model.datasources

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

class PreferencesController constructor(
    private val dataStore: DataStore<Preferences>
){ //anzichè object

    companion object {
        private val TAG = PreferencesController::class.java.simpleName
        // KEYS da salvare nel DataStore
        val SID_KEY = stringPreferencesKey("sid")
        val UID_KEY = intPreferencesKey("uid")
        val IS_FIRST_RUN_KEY = booleanPreferencesKey("isFirstRun")
        val IS_REGISTERED_KEY = booleanPreferencesKey("isRegistered")

        val LAST_SCREEN_KEY = stringPreferencesKey("lastScreen")
        val MENU_MID_KEY = intPreferencesKey("menuMid")

        // private INSTANCE TODO: se problemi di creazioni multiple
    }

    suspend fun <T> getItemsByKey(prefKey: Preferences.Key<T>) : T? {
        val prefs = dataStore.data.first()
        val result = prefs[prefKey]
        Log.i(TAG, "Recupero -> $prefKey = $result")
        return result
    }

    suspend fun <T> setItems(prefKey: Preferences.Key<T>, value: T) {
        Log.i(TAG, "Salvataggio -> $prefKey = $value")
        dataStore.edit { prefs ->
            prefs[prefKey] = value
        }
    }

    suspend fun saveSessionKeys(sid: String, uid: Int) {
        setItems(SID_KEY, sid)
        setItems(UID_KEY, uid)
    }

    suspend fun isFirstLaunch(): Boolean {
        val isFirstRun = this.getItemsByKey(IS_FIRST_RUN_KEY)
        if (isFirstRun == null) {
            this.setItems(IS_FIRST_RUN_KEY, true)
            this.setItems(IS_REGISTERED_KEY, false)
            return true
        }
        return false
    }
}