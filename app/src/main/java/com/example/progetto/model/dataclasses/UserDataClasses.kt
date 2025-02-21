package com.example.progetto.model.dataclasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSession( //Risposta nuovo utente, status 200
    val sid: String,
    val uid: Int
)

@Serializable
data class UserDetails( //Risposta fetchUserDetails(), status 200, 401, 404
    //sono tutti nulli all'inizio, tranne uid. Gestisco i null con try/catch di runWithErrorHandling()
    @SerialName("uid") val uid : Int,
    @SerialName("firstName") val firstName : String,
    @SerialName("lastName") val lastName : String,
    @SerialName("lastOid") val lastOrderId : Int?,
    @SerialName("orderStatus") val orderStatus: String?,
    @SerialName("cardFullName") val cardFullName : String,
    @SerialName("cardNumber") val cardNumber : String,
    @SerialName("cardExpireMonth") val cardExpireMonth : Int,
    @SerialName("cardExpireYear") val cardExpireYear : Int,
    @SerialName("cardCVV") val cardCVV : String
)

@Serializable //PUT
data class UpdateUserParamsWithSid( // body updateUserData(), status 204, 401, 404
    val firstName: String,
    val lastName: String,
    val cardFullName: String,
    val cardNumber: String,
    val cardExpireMonth: Int,
    val cardExpireYear: Int,
    val cardCVV: String,
    val sid: String
)
