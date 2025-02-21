package com.example.progetto.model.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class OrderRequestBody( //body buyMenu(), status response 200, 401, 403, 404
    val sid: String,
    val deliveryLocation: Location
)

@Serializable //onDelivery e Delivered
data class OrderDetails( //Risposta getStatusOrder e buyMenu, status 200, 401, 404
    val oid: Int,
    val mid: Int,
    val uid: Int,
    val creationTimestamp: String,
    val status: String,
    val deliveryLocation: Location,
    val expectedDeliveryTimestamp: String? = null, //gestisco il timestamp qui?
    val deliveryTimestamp: String? = null,
    val currentPosition: Location
)