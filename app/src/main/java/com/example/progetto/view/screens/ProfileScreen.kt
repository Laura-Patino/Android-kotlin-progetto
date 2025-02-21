package com.example.progetto.view.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progetto.R
import com.example.progetto.model.dataclasses.MenuDetailsWithImage
import com.example.progetto.model.dataclasses.OrderDetails
import com.example.progetto.model.dataclasses.UserDetails
import com.example.progetto.view.commons.CreditCard
import com.example.progetto.view.commons.LoadingScreen
import com.example.progetto.viewmodel.LastOrderState
import com.example.progetto.viewmodel.MainViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val userState by viewModel.userState.collectAsState()
    val orderStatus by viewModel.lastOrderState.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("ProfileScreen", "Recupero i dettagli dell'utente...")
        viewModel.fetchUserDetails()
        viewModel.getOrderDetailsWithImage()
    }

    if (userState.user == null || (userState.user?.lastOrderId != null && orderStatus.lastOrder == null)) {
        LoadingScreen()
    } else if (userState.user?.firstName != "") { //true && true
        Log.d("ProfileScreen", "Utente registrato con orderStatus=$orderStatus")
        UserRegistered(user = userState.user!!, order = orderStatus, viewModel = viewModel, changeScreen = {
            viewModel.resetUserData()
            viewModel.changeScreen("UpdateProfilo")}
        )
    } else {
        NoUserRegistered(changeScreen = { viewModel.changeScreen("UpdateProfilo") })
    }
}

@Composable
fun UserRegistered(user: UserDetails, order: LastOrderState, viewModel: MainViewModel, changeScreen: () -> Unit) {

    LaunchedEffect(Unit) {
        if (user.lastOrderId != null) {
            Log.d("ProfileScreen", "(US) Utente registrato con un ordine...$order")
            //viewModel.getOrderDetailsWithImage()
        } else {
            Log.d("ProfileScreen", "(US) Utente registrato SENZA un ordine.. $order")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.useravatardefault),
            contentDescription = "foto profilo",
            modifier = Modifier.size(120.dp)
        )
        Text(text = "${user.firstName} ${user.lastName}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(colorResource(R.color.verde_progetto)),
            shape = ShapeDefaults.ExtraLarge,
            onClick = changeScreen
        ) {
            Text(text = "AGGIORNA PROFILO")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painter = painterResource(R.drawable.credit_card), contentDescription = "creditCard", Modifier.size(25.dp))
            Text(text = "Carta di credito", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        Divider(
            thickness = 3.dp, color = colorResource(id = R.color.rosso_progetto), modifier = Modifier.padding(bottom = 4.dp)
        )
        CreditCard(
            cardFullName = user.cardFullName,
            cardNumber = user.cardNumber,
            cardExpireMonth = user.cardExpireMonth,
            cardExpireYear = user.cardExpireYear
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painter = painterResource(R.drawable.shopping_bag), contentDescription = "shoppingBag", Modifier.size(25.dp))
            Text(text = "Ultimo ordine", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        Divider(thickness = 3.dp, color = colorResource(id = R.color.rosso_progetto), modifier = Modifier.padding(bottom = 4.dp))

        if (user.lastOrderId != null && user.orderStatus != null && order.lastOrder != null && order.menuLastOrder != null) {
            LastOrder(orderInfo = order.lastOrder, menuInfo = order.menuLastOrder, viewModel = viewModel)
        } else {
            Box(contentAlignment = Alignment.TopStart, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Non hai ancora effettuato alcun ordine.", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun LastOrder(orderInfo: OrderDetails, menuInfo: MenuDetailsWithImage, viewModel: MainViewModel) {
    val completed = orderInfo.status == "COMPLETED"
    var dataFormatta = arrayOf("15/12/24", "12:15")
    if (completed) {
        Log.d("LastOrder", "Ordine completato")
        dataFormatta = convertISO(orderInfo.deliveryTimestamp!!)
    } else {
        Log.d("LastOrder", "Ordine in consegna")
        dataFormatta = convertISO(orderInfo.expectedDeliveryTimestamp!!)
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.5.dp, colorResource(R.color.grigio_progetto))
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                bitmap = getBitmapFromBase64(menuInfo.image.base64).asImageBitmap(),
                contentDescription = "pietanza",
                modifier = Modifier
                    .height(150.dp)
                    .width(150.dp))
            Column {
                Text(text = menuInfo.menuDetails.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Ordine #${orderInfo.oid}", fontSize = 16.sp, color = Color.Gray)
                Text(text = "Status: ${if (completed) "consegnato" else "in consegna"}", fontSize = 16.sp)
                Text(text = "Data consegna: ${dataFormatta[0]}", fontSize = 16.sp)
                Text(text = "Ora consegna: ${dataFormatta[1]}", fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun getBitmapFromBase64(image: String): Bitmap {
    val byteArray = Base64.decode(image)
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

fun convertISO(isoString: String): Array<String> {
    val instant = Instant.parse(isoString)
    val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()

    val formatterGiorno = DateTimeFormatter.ofPattern("dd/MM/yy")
    val formatterOra = DateTimeFormatter.ofPattern("HH:mm")

    val giorno = dateTime.format(formatterGiorno)
    val ora = dateTime.format(formatterOra)
    return arrayOf(giorno, ora)
}

@Composable
fun NoUserRegistered(changeScreen: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Nuovo utente", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = colorResource(id = R.color.rosso_progetto))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Completa il tuo profilo per poter ordinare il tuo menu preferito!", fontSize = 18.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.padding(8.dp))
        Button(
            colors = ButtonDefaults.buttonColors(colorResource(R.color.verde_progetto)),
            shape = ShapeDefaults.ExtraSmall,
            onClick = changeScreen
        ) {
            Text(text = "REGISTRAZIONE")
        }
    }
}
