package com.example.progetto.view.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progetto.R

@Composable
fun StartLoadingScreen(permissionGranted: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.verde_progetto)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Mangia e Basta",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 40.sp,
            color = Color.White
        )
        if (permissionGranted) {
            Text(text = "Ottenimento posizione...", fontSize = 20.sp, fontStyle = FontStyle.Italic, color = Color.White)
        }
        Spacer(modifier = Modifier.height(20.dp))
        CircularProgressIndicator(
            modifier = Modifier.width(52.dp),
            color = Color(0xFFf7e00a),
            trackColor = colorResource(id = R.color.verde_progetto)//Color(0xFFfae8b4)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StartLoadingScreenPreview() {
    StartLoadingScreen(permissionGranted = true)
}