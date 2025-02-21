package com.example.progetto.model.dataclasses

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// Error response in caso di 401, 404 return json con un message
@Serializable
data class ResponseError(
    val message: String
)

@Serializable
data class Location(
    val lat: Double,
    val lng: Double,
    @Transient val address: String? = null
)

/* //Per Mapbox
* fun APILocation.toPoint(): Point {
    return Point.fromLngLat(this.longitude, this.latitude)
}
* */

enum class ErrorType {
    NETWORK,
    ACCOUNT_DETAILS,
    POSITION_NOT_ALLOWED,
    INVALID_ACTION
}

data class Error(
    val type: ErrorType,
    val title: String = "Si è verificato un errore",
    override val message: String, //riscrivo il messaggio di errore
    val actionText: String? = null,
    val dismissText: String = "Annulla"
): Exception(message)
