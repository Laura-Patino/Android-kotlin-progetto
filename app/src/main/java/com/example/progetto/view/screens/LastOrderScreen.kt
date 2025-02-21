package com.example.progetto.view.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.progetto.R
import com.example.progetto.view.commons.LoadingScreen
import com.example.progetto.viewmodel.MainViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor

@Composable
fun LastOrderScreen(viewModel: MainViewModel) {
    val userState by viewModel.userState.collectAsState()
    val positionState by viewModel.positionState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUserDetails()
    }

    if (userState.user == null || positionState.positionUser == null) { //attende il calcolo della posizione, serve?
        LoadingScreen()
    } else if (userState.user?.lastOrderId != null && userState.user?.orderStatus != null) {
        Log.d("LastOrderScreen", "Utente ha effettuato un ordine.. user=${userState.user} orderId=${userState.user?.lastOrderId} orderStatus=${userState.user?.orderStatus}")
        UltimoOrdine(viewModel, orderStatus = userState.user?.orderStatus!!)//, userPos = positionState.positionUser!!)
    } else { // Ancora nessun ordine effettuato
        NessunOrdine(changeScreen = { viewModel.changeScreen("Home") })
    }
}

@Composable
fun NessunOrdine(changeScreen: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Non hai ancora effettuato un ordine.",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = colorResource(id = R.color.rosso_progetto)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            fontSize = 18.sp,
            text = stringResource(R.string.testo_no_ordine1)
        )
        Text(
            fontSize = 18.sp,
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(horizontal = 20.dp),
            text = stringResource(R.string.testo_no_ordine2)
        )
        Button(
            modifier = Modifier.padding(top = 20.dp),
            shape = ShapeDefaults.ExtraSmall,
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.verde_progetto)),
            onClick = changeScreen
        ) {
            Text(text = "VEDI I MENU", fontWeight = FontWeight.Bold)
        }
    }

}

@Composable
fun UltimoOrdine(viewModel: MainViewModel, orderStatus: String) {
    val marker1 by remember { mutableStateOf(R.drawable.marker) }
    val marker2 by remember { mutableStateOf(R.drawable.menuicon) }
    val marker3 by remember { mutableStateOf(R.drawable.droneicon1) }

    // Create and remember the icon to use for the points
    val userMarker = rememberIconImage(key = marker1, painter = painterResource(marker1))
    val menuMarker = rememberIconImage(key = marker2, painterResource(id = marker2))
    val droneMarker = rememberIconImage(key = marker3, painterResource(id = marker3))

    // Iscrizione allo stato dell'ultimo ordine (contiene dati ordine e menu)
    val orderInfo = viewModel.lastOrderState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                Log.d("LastOrderScreen", "Parametri passati STATUS=$orderStatus")
                viewModel.getOrderDetailsWithImage()
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    if (orderInfo.value.lastOrder == null || orderInfo.value.menuLastOrder == null) { //errore se &&
        LoadingScreen()
    } else {
        Log.d("LastOrderScreen", "Sezione Ultimo ordine orderInfo.menuLastOrder=${orderInfo.value.menuLastOrder}")
        val menu = orderInfo.value.menuLastOrder?.menuDetails!!
        val order = orderInfo.value.lastOrder!!

        // Definizione dei points da visualizzare sulla mappa
        val points = mutableListOf(
            Point.fromLngLat(order.deliveryLocation.lng, order.deliveryLocation.lat), //primo marker utente
            Point.fromLngLat(menu.location.lng, menu.location.lat), //secondo marker menu
        )
        if (order.status != "COMPLETED") {
            points.add(Point.fromLngLat(order.currentPosition.lng, order.currentPosition.lat)) //drone
        }

        //MAPPA
        val mapViewportState = rememberMapViewportState {
            //Set initial camera position
            setCameraOptions {
                center(Point.fromLngLat(order.deliveryLocation.lng, order.deliveryLocation.lat))
                zoom(13.5)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            //verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MapboxMap(
                Modifier
                    .fillMaxWidth()
                    .height(580.dp), //560
                mapViewportState = mapViewportState
            ) {
                MapEffect(key1 = Unit) {
                    val cameraOptions = mapViewportState.cameraForCoordinates(
                        coordinates = points,
                        coordinatesPadding = EdgeInsets(120.0, 100.0, 100.0, 100.0)
                    )
                    mapViewportState.setCameraOptions(cameraOptions)
                }

                //zip crea una lista di coppie, una collection rappresenta i Point e una collection rappresenta i marker
                //val pointWithMarkers = points.zip(listOf(userMarker, menuMarker, droneMarker))
//                    pointWithMarkers.forEach {
//                        PointAnnotation(point = it.component1()) {
//                            iconImage = it.component2()
//                            iconSize = 0.3
//                            iconAnchor = IconAnchor.BOTTOM
//                        }
//                    }
                PointAnnotation(point = points[0]) {
                    iconImage = userMarker
                    iconSize = 0.3
                    iconAnchor = IconAnchor.BOTTOM
                }
                PointAnnotation(point = points[1]) {
                    iconImage = menuMarker
                    iconSize = 0.3
                    iconAnchor = IconAnchor.BOTTOM
                }
                if (order.status != "COMPLETED") {
                    PointAnnotation(point = points[2]) {
                        iconImage = droneMarker
                        iconSize = 0.3
                        iconAnchor = IconAnchor.BOTTOM
                    }
                }

                PolylineAnnotation(points = points) {
                    lineColor = Color.Red
                    lineWidth = 3.0
                }
            }

            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                //verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Ordine #7200", fontSize = 16.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.ExtraLight, color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (order.status != "COMPLETED") { // orderStatus? no, non si aggiorna ad ogni delay
                    val dataformattata = convertISO(order.expectedDeliveryTimestamp!!)
                    Text(
                        text = buildAnnotatedString {
                            append("Il tuo ordine")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(" ${menu.name} ")
                            }
                            append("è in arrivo")
                        }, fontSize = 16.sp
                    )
                    Text(text = "Consegna prevista: ${dataformattata[0]} alle ${dataformattata[1]}", fontSize = 16.sp)
                } else {
                    val dataformattata = convertISO(order.deliveryTimestamp!!)
                    Text(text = buildAnnotatedString {
                            append("Il tuo ordine")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(" ${menu.name} ")
                            }
                            append("è stato consegnato alle ${dataformattata[1]} del ${dataformattata[0]}")
                        }, fontSize = 18.sp, textAlign = TextAlign.Center
                    )
                }
//                Text("Ordine: $orderStatus")
//                Text(text = "order dati: $order",  fontSize = 14.sp)
//                Text(text = "menu dati: $menu",  fontSize = 14.sp)
            }
        }
    }
}


