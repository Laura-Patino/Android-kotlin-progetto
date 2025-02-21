package com.example.progetto.model.repositories

import android.util.Log
import com.example.progetto.model.dataclasses.UpdateUserParamsWithSid
import com.example.progetto.model.dataclasses.UserDetails
import com.example.progetto.model.dataclasses.UserSession
import com.example.progetto.model.datasources.ApiController
import com.example.progetto.model.datasources.DBController
import com.example.progetto.model.datasources.PreferencesController

class UserRepository(
    private val apiController: ApiController,
    private val dbController: DBController,
    private val preferencesController: PreferencesController
) {
    companion object {
        private val TAG = UserRepository::class.simpleName
    }

    suspend fun isFirstLaunch(): Boolean {
        //PreferencesController fa già la verifica se è il primo avvio
        return preferencesController.isFirstLaunch()
    }

    suspend fun isRegistered() : Boolean {
        //se key non esiste ritorna false, altrimenti ritorna il valore true/false
        return preferencesController.getItemsByKey(PreferencesController.IS_REGISTERED_KEY) ?: false
    }

    suspend fun saveLastScreen(screen: String) {
        preferencesController.setItems(PreferencesController.LAST_SCREEN_KEY, screen)
    }

    suspend fun getLastScreen() : String? {
        return preferencesController.getItemsByKey(PreferencesController.LAST_SCREEN_KEY)
    }

    suspend fun getUserSession(): UserSession {
        // Recupero sid e uid dal DataStore, se presenti
        //TODO: isFirstRun != true get session keys from DataStore, else call apiController.createUser()
        val sid = preferencesController.getItemsByKey(PreferencesController.SID_KEY)
        val uid = preferencesController.getItemsByKey(PreferencesController.UID_KEY)

        if (sid != null  && uid != null) {
            Log.d(TAG, "Utente registrato, credenziali prese dal DataStore.")
            //TODO modificare firstRun a false ?
            return UserSession(sid, uid)
        } else {
            Log.d(TAG, "Creazione nuova sessione e salvataggio nello Storage..")
            val sessionKeys = apiController.createUser()
            // Salvataggio sid e uid nel DataStore
            preferencesController.saveSessionKeys(sessionKeys.sid, sessionKeys.uid)
            return sessionKeys
        }
    }

    suspend fun getUserDetails(sid: String, uid: Int): UserDetails {
        return apiController.fetchUserDetails(sid, uid)
    }

    suspend fun updateUserDetails(uid: Int, userData: UpdateUserParamsWithSid) {
        apiController.apiUpdateUserDetails(uid, userData)
        preferencesController.setItems(PreferencesController.IS_REGISTERED_KEY, true)
    }
}