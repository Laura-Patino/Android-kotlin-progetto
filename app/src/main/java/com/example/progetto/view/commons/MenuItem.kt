package com.example.progetto.view.commons

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progetto.R
import com.example.progetto.model.dataclasses.MenuWithImage
import com.example.progetto.viewmodel.MainViewModel
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun MenuItem(viewModel: MainViewModel, menu: MenuWithImage, onPress: () -> Unit) {
    Card(
        //border = BorderStroke(1.dp, color = Color.Black),
        shape = ShapeDefaults.ExtraSmall,
        colors = CardDefaults.cardColors(Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.height(150.dp)) { //height(150.dp)
            val image: String? = menu.image?.base64
            if (image != null) {
                val byteArray = Base64.decode(image)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "pietanza",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(150.dp)
                        .height(150.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.imagenotavailable),
                    contentDescription = "Immagine non disponibile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(150.dp)
                        .height(150.dp)
                )
            }
            Box {
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp) //MODIFICATO prima 4.dp
                        .fillMaxSize()
                ) {
                    Text(
                        text = menu.menu.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val priceStr = "%.2f".format(menu.menu.price)
                        Text(text = "€ $priceStr | ", fontSize = 16.sp)
                        Icon(
                            painter = painterResource(id = R.drawable.timeoutline),
                            contentDescription = "orologio",
                            modifier = Modifier.size(14.dp)
                        )
                        Text(text = " ${viewModel.formattedTime(menu.menu.deliveryTime)}", fontSize = 16.sp)
                    }
                    Text(
                        fontSize = 16.sp,
                        textAlign = TextAlign.Start,
                        lineHeight = 16.sp,
                        text = menu.menu.shortDescription,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                FilledIconButton(
                    modifier = Modifier
                        //.padding(end = 4.dp)
                        .align(Alignment.BottomEnd),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFFa9412a)
                    ),
                    onClick = { onPress() }
                ) {
                    Icon(
                        modifier = Modifier.width(34.dp).height(34.dp),
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
           }
        }
    }
    Divider(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        color = colorResource(id = R.color.grigio_progetto),
        thickness = 3.dp
    )
}