package com.example.progetto.view.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progetto.R

@Composable
fun LoadingScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .background(colorResource(R.color.verde_progetto)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
//        Text(
//            text = "Caricamento...",
//            fontSize = 30.sp,
//            color = Color.White,//Color(0xFFf7e00a),
//            style = MaterialTheme.typography.titleSmall
//        )
        CircularProgressIndicator(
            modifier = Modifier.width(52.dp),
            color = Color(0xFFf7e00a),
            trackColor = colorResource(R.color.verde_progetto)
        )
    }
}

@Preview
@Composable
fun LoadingScreenPreview() {
    LoadingScreen()
}