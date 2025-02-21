package com.example.progetto.model.dataclasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class Menu(
    val mid: Int,
    val name: String,
    val price: Double,
    val location: Location,
    val imageVersion: Int,
    val shortDescription: String,
    val deliveryTime: Int //in minuti
)

@Serializable
data class MenuDetails( //Response getMenuDetails()
    val mid: Int,
    val name: String,
    val price: Double,
    val location: Location,
    val imageVersion: Int,
    val shortDescription: String,
    val deliveryTime: Int,
    val longDescription: String
)

@Serializable
data class MenuImage( //Risposta getMenuImage(), status 200, 401, 404
    var base64: String,
)

// Uso locale, non da serializzare in JSON
data class MenuWithImage( //per inviare alla UI il dato con l'immagine
    val menu: Menu,
    var image: MenuImage? = null
)

data class MenuDetailsWithImage(
    val menuDetails: MenuDetails,
    val image: MenuImage
)

// Room Entity, datasource LOCALE
@Entity
data class MenuImageWithVersion(
    @PrimaryKey val mid : Int,
    val imageVersion : Int,
    val image : String
)