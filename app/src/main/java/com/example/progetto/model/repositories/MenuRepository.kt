package com.example.progetto.model.repositories

import android.util.Log
import com.example.progetto.model.dataclasses.Menu
import com.example.progetto.model.dataclasses.MenuDetails
import com.example.progetto.model.dataclasses.MenuImage
import com.example.progetto.model.dataclasses.MenuImageWithVersion
import com.example.progetto.model.datasources.ApiController
import com.example.progetto.model.datasources.DBController
import com.example.progetto.model.datasources.PreferencesController

class MenuRepository(
    private val apiController: ApiController,
    private val dbController: DBController,
    private val preferencesController: PreferencesController
) {
    companion object { //utile per Log
        private val TAG = MenuRepository::class.java.simpleName
    }

    suspend fun saveMenuMidToStorage(mid: Int) {
        preferencesController.setItems(PreferencesController.MENU_MID_KEY, mid)
    }

    suspend fun getMenuMidFromStorage(): Int? {
        return preferencesController.getItemsByKey(PreferencesController.MENU_MID_KEY)
    }

    suspend fun getMenuVicini(sid: String, latitude: Double, longitude: Double): List<Menu> {
        return apiController.fetchAllMenus(sid, latitude, longitude)
    }

    suspend fun getMenuDetails(sid: String, mid: Int, latitude: Double = 45.4642, longitude: Double = 9.19): MenuDetails {
        return apiController.fetchMenuDetails(sid, mid, latitude, longitude)
    }

    suspend fun getMenuImage(sid: String, mid: Int, imageVersion: Int) : MenuImage {
        val imageFromDB = dbController.dao.getMenuImageByVersion(mid, imageVersion)

        if (imageFromDB == null) {
            Log.i(TAG, "Immagine di $mid versione $imageVersion non trovata nel DB")
            val imageFromServer = apiController.fetchMenuImage(sid, mid)

            if (imageFromServer.base64.startsWith("data:image/jpeg;base64,")) {
                //base64 è dichiarata var -> modificabile
                imageFromServer.base64 = imageFromServer.base64.substring(23)
            }
            Log.d(TAG, "Salvataggio immagine nel DB...")
            dbController.dao.insertMenuImage(MenuImageWithVersion(mid, imageVersion, imageFromServer.base64))

            Log.d(TAG, "Immagine recuperata dal Server e salvata nel DB.")
            return imageFromServer
        } else {
            Log.d(TAG, "Immagine $mid versione $imageVersion trovata dal DB.")
            return MenuImage(base64 = imageFromDB.image)
        }
    }

}