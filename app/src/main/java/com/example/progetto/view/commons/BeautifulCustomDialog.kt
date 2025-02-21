package com.example.progetto.view.commons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.progetto.R

@Composable
fun BeautifulCustomDialog(
    onDismissRequest: () -> Unit,
    title: String,
    description: String,
    showPainter: Boolean = false
    //painter: Painter? = null
    //onConfirmation: () -> Unit,
    //imageDescription: String,
) {
    Dialog(onDismissRequest = {
        onDismissRequest()
    }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp),
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                if (showPainter) {
                    Image(
                        painter = painterResource(id = R.drawable.done),
                        contentDescription = null
                    )
                }
                Button(
                    onClick = { onDismissRequest() },
                    modifier = Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.verde_progetto))
                ) {
                    Text("OK")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlertDialogPreview() {
    BeautifulCustomDialog(
        //dialogTitle = "",
        //dialogSubTitle = "",
        onDismissRequest = { },
        title = "Lettura posizione non autorizzata",
        description = "Per il momento verrano visualizzati i ristoranti vicini a Milano"
        //onConfirmation = {}
    )
}