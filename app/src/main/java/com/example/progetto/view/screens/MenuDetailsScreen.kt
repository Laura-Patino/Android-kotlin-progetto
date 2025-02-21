package com.example.progetto.view.screens

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progetto.R
import com.example.progetto.model.dataclasses.MenuDetailsWithImage
import com.example.progetto.view.commons.BeautifulCustomDialog
import com.example.progetto.view.commons.LoadingScreen
import com.example.progetto.viewmodel.AppState
import com.example.progetto.viewmodel.MainViewModel
import com.mapbox.maps.extension.style.style
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
fun MenuDetailsScreen(viewModel: MainViewModel) {
    val menuState by viewModel.menuState.collectAsState()
    val appState by viewModel.appState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchMenuDetails(menuState.selectedMenuMid ?: 49)
        viewModel.canDoOrder()
    }

    if (menuState.selectedMenu == null) {
        LoadingScreen()
    } else {
        ContenutoMenuDetails(menu = menuState.selectedMenu!!, viewModel = viewModel, appState = appState)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetMenuSelection() }
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun ContenutoMenuDetails(menu: MenuDetailsWithImage, viewModel: MainViewModel, appState: AppState) {
    var showDialog by remember { mutableStateOf(false) }

    when {  /*Dialog carta di credito non valida*/
        showDialog -> {
            BeautifulCustomDialog(
                onDismissRequest = { viewModel.changeScreen("Profilo") },
                title = "Carta di credito non valida",
                description = "La carta di credito inserita non è valida. Inserirne una valida sul profilo.",
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = menu.menuDetails.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        val image: String = menu.image.base64
        val byteArray = Base64.decode(image)
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.FillWidth,
        )
        Text(text = menu.menuDetails.shortDescription, fontStyle = FontStyle.Italic, fontSize = 16.sp, textAlign = TextAlign.Center, color = colorResource(R.color.rosso_progetto))
        Text(
            text = menu.menuDetails.longDescription,
            //style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp,
            textAlign = TextAlign.Start,
            //letterSpacing = 0.2.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Divider(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), thickness = 2.dp)
        val priceStr = "%.2f".format(menu.menuDetails.price)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Prezzo: $priceStr €", fontSize = 18.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(end = 20.dp))
            Text(text = "Pronto in: ${viewModel.formattedTime(menu.menuDetails.deliveryTime)}", fontWeight = FontWeight.Light, fontSize = 18.sp)
        }
        Divider(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), thickness = 2.dp)
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.rosso_progetto)),
            shape = ShapeDefaults.ExtraSmall,
            enabled = appState.avvisi.isEmpty(),
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = viewModel.buyMenu(menu.menuDetails.mid)
                    Log.d("MenuDetailsScreen", "Risultato ordine: $result")
                    if (!result) {
                        showDialog = true
                    } else {
                        viewModel.changeScreen("Ordine")
                    }
                }
            }
        ) {
            Text(text = "Ordina", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }

        if (appState.avvisi.isNotEmpty()){
            Column(
                modifier = Modifier.padding(bottom =  4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Non è ancora possibile ordinare un menu:", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colorResource(R.color.rosso_progetto))
                appState.avvisi.withIndex().forEach { (index, avviso) ->
                    Text(text = "${index+1}. $avviso", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colorResource(R.color.rosso_progetto))
                }
            }
        }
//        Column {
//            Text(text = "Non è ancora possibile ordinare un menu:", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colorResource(R.color.rosso_progetto))
//            Text("- Non hai ancora completato il profilo", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colorResource(R.color.rosso_progetto))
//            Text("- Hai negato l'accesso alla tua posizione", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colorResource(R.color.rosso_progetto))
//            Text("- Hai un ordine in corso, attendi la consegna", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colorResource(R.color.rosso_progetto))
//        }
    }
}