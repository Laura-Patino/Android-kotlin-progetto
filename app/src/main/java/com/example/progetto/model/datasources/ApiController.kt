package com.example.progetto.model.datasources

import android.net.Uri
import android.util.Log
import com.example.progetto.model.dataclasses.Error
import com.example.progetto.model.dataclasses.Location
import com.example.progetto.model.dataclasses.Menu
import com.example.progetto.model.dataclasses.MenuDetails
import com.example.progetto.model.dataclasses.MenuImage
import com.example.progetto.model.dataclasses.OrderDetails
import com.example.progetto.model.dataclasses.OrderRequestBody
import com.example.progetto.model.dataclasses.UpdateUserParamsWithSid
import com.example.progetto.model.dataclasses.UserDetails
import com.example.progetto.model.dataclasses.UserSession
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiController {
    //companion object {
    private const val BASE_URL = "https://develop.ewlab.di.unimi.it/mc/2425"
    private val TAG = ApiController::class.java.simpleName

    enum class HttpMethod {
        GET, POST, PUT, DELETE
    }
    //}

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private suspend fun genericRequest(
        endpoint: String,
        method: HttpMethod,
        queryParams: Map<String, Any> = emptyMap(),
        bodyParams: Any? = null
    ): HttpResponse {
        val urlUri = Uri.parse(BASE_URL + endpoint)
        val urlBuilder = urlUri.buildUpon()
        queryParams.forEach { (key, value) ->
            urlBuilder.appendQueryParameter(key, value.toString())
        }
        val completeUrlString = urlBuilder.build().toString()

        Log.d(TAG, "URL: $completeUrlString")

        val request: HttpRequestBuilder.() -> Unit = {
            bodyParams?.let {
                contentType(ContentType.Application.Json)
                setBody(bodyParams)
            }
        }

        val response = when (method) {
            HttpMethod.GET -> client.get(completeUrlString, request)
            HttpMethod.POST -> client.post(completeUrlString, request)
            HttpMethod.DELETE -> client.delete(completeUrlString, request)
            HttpMethod.PUT -> client.put(completeUrlString, request)
        }

        return response
    }

    //USER
    suspend fun createUser(): UserSession {
        Log.d(TAG, "Creating new User...")

        val url = "/user"
        val httpResponse = genericRequest(
            endpoint = url,
            method = HttpMethod.POST
        )
        Log.d(TAG, "Status code: ${httpResponse.status.value}") //value = 200, description = OK
        return httpResponse.body() as UserSession //corpo della risposta
    }

    suspend fun fetchUserDetails(sid: String, uid: Int): UserDetails {
        Log.d(TAG, "Getting User Details...")
        val url = "/user/$uid"
        val queryParams = mapOf(
            "sid" to sid,
            "uid" to uid
        )
        val httpResponse = genericRequest(
            endpoint = url,
            method = HttpMethod.GET,
            queryParams = queryParams
        )
        Log.d(TAG, "Status code: ${httpResponse.status.value}")

        when (httpResponse.status.value) {
            200 -> return httpResponse.body() as UserDetails
            else -> throw Exception("Error in getting User Details")
        }
    }


    suspend fun apiUpdateUserDetails(uid: Int, userData: UpdateUserParamsWithSid) {
        /** bodyParams need already sid key-value */
        Log.d(TAG, "Updating User Details...")
        val url = "/user/$uid"
        val httpResponse = genericRequest(
            endpoint = url,
            method = HttpMethod.PUT,
            bodyParams = userData
        )
        Log.d(TAG, "Status code: ${httpResponse.status.value}")

        when (httpResponse.status.value) {
            204 -> return //no body
            else -> throw Exception("Error updating User Details")
        }
    }


    // MENUS
    suspend fun fetchAllMenus(sid: String, lat: Double = 45.4642, lng: Double = 9.19): List<Menu> {
        Log.d(TAG, "Getting all menus...")
        val endpoint = "/menu"
        val queryParams = mapOf(
            "lat" to lat,
            "lng" to lng,
            "sid" to sid
        )

        val httpResponse = genericRequest(
            endpoint = endpoint,
            method = HttpMethod.GET,
            queryParams = queryParams
        )
        Log.d(TAG, "Status code: ${httpResponse.status.value}")

        when (httpResponse.status.value) {
            200 -> return httpResponse.body()
            else -> throw Exception("Error in getting all menus")
        }
    }

    suspend fun fetchMenuDetails(
        sid: String,
        mid: Int = 49,
        lat: Double = 45.4642,
        lng: Double = 9.19
    ): MenuDetails {
        Log.d(TAG, "Getting Menu Details...")
        val url = "/menu/$mid"
        val queryParams = mapOf(
            "lat" to lat,
            "lng" to lng,
            "sid" to sid
        )
        val httpResponse = genericRequest(
            endpoint = url,
            method = HttpMethod.GET,
            queryParams = queryParams
        )
        Log.d(TAG, "Status code: ${httpResponse.status.value}")

        when (httpResponse.status.value) {
            200 -> return httpResponse.body() as MenuDetails
            else -> throw Exception("Error in getting Menu Details")
        }
    }

    suspend fun fetchMenuImage(sid: String, mid: Int): MenuImage {
        val url = "/menu/$mid/image"
        val queryParams = mapOf(
            "sid" to sid
        )
        val httpResponse = genericRequest(
            endpoint = url,
            method = HttpMethod.GET,
            queryParams = queryParams
        )
        Log.d(TAG, "Status code: ${httpResponse.status.value}")

        when (httpResponse.status.value) {
            200 -> return httpResponse.body() as MenuImage
            else -> throw Exception("Error in getting Menu Image")
        }
    }

    // ORDER
    suspend fun apiBuyMenu(sid: String, mid: Int, deliveryLocation: Location): OrderDetails {
        Log.d(TAG, "Buying Menu...")
        val url = "/menu/$mid/buy"
        val bodyParams = OrderRequestBody(sid, deliveryLocation)

        val httpResponse = genericRequest(
            endpoint = url,
            method = HttpMethod.POST,
            bodyParams = bodyParams
        )
        Log.d(TAG, "Status code: ${httpResponse.status.value}")

        when (httpResponse.status.value) {
            200 -> return httpResponse.body() as OrderDetails
            403 -> throw Error("Carta di credo invalida")
            else -> throw Exception("Error in buying Menu")
        }
    }

    suspend fun fetchOrderDetails(sid: String, oid: Int): OrderDetails {
        Log.d(TAG, "Getting Order Details...")
        val url = "/order/$oid"
        val queryParams = mapOf(
            "sid" to sid
        )
        val httpResponse = genericRequest(
            endpoint = url,
            method = HttpMethod.GET,
            queryParams = queryParams
        )
        Log.d(TAG, "Status code: ${httpResponse.status.value}")

        when (httpResponse.status.value) {
            200 -> return httpResponse.body() as OrderDetails
            else -> throw Exception("Error in getting Order Details")
        }
    }
}