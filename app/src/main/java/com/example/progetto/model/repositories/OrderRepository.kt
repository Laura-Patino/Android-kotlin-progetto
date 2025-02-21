package com.example.progetto.model.repositories

import android.util.Log
import com.example.progetto.model.dataclasses.Error
import com.example.progetto.model.dataclasses.Location
import com.example.progetto.model.dataclasses.OrderDetails
import com.example.progetto.model.datasources.ApiController
import com.example.progetto.model.datasources.DBController
import com.example.progetto.model.datasources.PreferencesController

class OrderRepository(
    private val apiController: ApiController,
    private val dbController: DBController,
    private val preferencesController: PreferencesController
) {
    companion object {
        private val TAG = OrderRepository::class.simpleName
    }

    suspend fun getOrderDetails(sid: String, oid: Int): OrderDetails {
        return apiController.fetchOrderDetails(sid, oid)
    }

    suspend fun buyMenu(sid: String, mid:Int, deliveryLocation: Location) : OrderDetails {
        return apiController.apiBuyMenu(sid, mid, deliveryLocation)
    }
}