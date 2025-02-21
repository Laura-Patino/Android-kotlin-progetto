package com.example.progetto.view.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progetto.R
import com.example.progetto.model.dataclasses.UpdateUserParamsWithSid
import com.example.progetto.model.dataclasses.UserDetails
import com.example.progetto.view.commons.BeautifulCustomDialog
import com.example.progetto.view.commons.LoadingScreen
import com.example.progetto.viewmodel.MainViewModel
import com.example.progetto.viewmodel.ViewModelFormAccount

@Composable
fun UpdateProfileScreen(viewModel: MainViewModel) {
    val userState by viewModel.userState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUserDetails()
    }

    if (userState.user == null) {
        LoadingScreen()
    } else {
        FormModifica(userData = userState.user!!, viewModel = viewModel)
        //Ricomposizione quando aggiorno i dati e modifico lo stato userState.isRegistered = true
    }
}

@Composable
fun FormModifica(userData: UserDetails, viewModel: MainViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf(emptyMap<String, String?>()) }
    var fieldsToUpdate by remember { mutableStateOf(UpdateUserParamsWithSid(
        firstName = userData.firstName,
        lastName = userData.lastName,
        cardFullName = userData.cardFullName,
        cardNumber = userData.cardNumber,
        cardExpireMonth = userData.cardExpireMonth,
        cardExpireYear = userData.cardExpireYear,
        cardCVV = userData.cardCVV,
        sid = ""
    ))}
    var testoMese by remember { mutableStateOf(
            if (fieldsToUpdate.cardExpireMonth == -1) ""
            else fieldsToUpdate.cardExpireMonth.toString().padStart(2, '0')
    )}
    var testoAnno by remember { mutableStateOf(
            if (fieldsToUpdate.cardExpireYear == -1) ""
            else fieldsToUpdate.cardExpireYear.toString()
    )}

    LaunchedEffect(errors) {
        if (errors.isNotEmpty()) {
            Log.w("UpdateProfileScreen", "Errori ricevuti nello screen: $errors")

            if (errors.values.count { it != null } == 0) {
                Log.w("UpdateProfileScreen", "Possibile invio dati al server...")
                showDialog = true
                if (viewModel.updateUserData(fieldsToUpdate)) {
                    Log.d("UpdateProfileScreen", "Dati inviati con successo")
                }
            }
        }
    }

    when {  /*Dialog modifiche dati avvenute con successo*/
        showDialog -> {
            BeautifulCustomDialog(
                onDismissRequest = { viewModel.changeScreen("Profilo") },
                title = "Dati modificati correttamente",
                description = "Puoi tornare nel tuo profilo",
                showPainter = true
            )
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 15.dp, bottom = 5.dp, start = 16.dp, end = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Informazioni personali", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
        Divider(thickness = 3.dp, color = colorResource(id = R.color.rosso_progetto), modifier = Modifier.padding(bottom = 4.dp))
        Text(text = "Nome", fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))
        OutlinedTextField(
            singleLine = true,
            value = fieldsToUpdate.firstName,
            onValueChange = { if (it.length <= 15) fieldsToUpdate = fieldsToUpdate.copy(firstName = it) },
            isError = errors["firstName"] != null,
            supportingText = { errors["firstName"]?.let { Text(it)}},
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            )
        )
        Text(text = "Cognome", fontSize = 16.sp)
        OutlinedTextField(
            singleLine = true,
            value = fieldsToUpdate.lastName,
            onValueChange = { if (it.length <= 15) fieldsToUpdate = fieldsToUpdate.copy(lastName = it) },
            isError = errors["lastName"] != null,
            supportingText = { errors["lastName"]?.let { Text(it)}},
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            )
        )

        //Spacer(modifier = Modifier.padding(4.dp))

        Text(text = "Dati carta di credito", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
        Divider(
            thickness = 3.dp, color = colorResource(id = R.color.rosso_progetto), modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(text = "Nome completo sulla carta", fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))
        OutlinedTextField(
            singleLine = true,
            value = fieldsToUpdate.cardFullName,
            placeholder = { Text(text = "Mario Rossi") },
            onValueChange = { if (it.length <= 31) fieldsToUpdate = fieldsToUpdate.copy(cardFullName = it)},
            isError = errors["cardFullName"] != null,
            supportingText = { errors["cardFullName"]?.let { Text(it)}},
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
            )
        )
        Text(text = "Numero carta", fontSize = 16.sp)
        OutlinedTextField(
            singleLine = true,
            value = fieldsToUpdate.cardNumber,
            onValueChange = { if (it.length <= 16) fieldsToUpdate = fieldsToUpdate.copy(cardNumber = it.trim()) },
            isError = errors["cardNumber"] != null,
            supportingText = { errors["cardNumber"]?.let { Text(it)}},
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )
        Text(text = "Scadenza carta", fontSize = 16.sp)
        Row {
            OutlinedTextField(
                singleLine = true,
                placeholder = { Text(text = "MM") },
                value = testoMese,
                onValueChange = {
                    if (it.length <= 2 && (it.toIntOrNull() != null || it.isEmpty())) {
                        testoMese = it
                        fieldsToUpdate = fieldsToUpdate.copy(cardExpireMonth = it.toIntOrNull() ?: -1)
                    }
                },
                isError = errors["cardExpireMonth"] != null,
                supportingText = { errors["cardExpireMonth"]?.let { Text(it)}},
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
            OutlinedTextField(
                singleLine = true,
                placeholder = { Text(text = "AAAA") },
                value = testoAnno,
                onValueChange = {
                    if (it.length <= 4 && (it.toIntOrNull() != null || it.isEmpty())) {
                        testoAnno = it
                        fieldsToUpdate = fieldsToUpdate.copy(cardExpireYear = it.toIntOrNull() ?: -1)
                    }
                },
                isError = errors.getOrDefault("cardExpireYear", null) != null,
                supportingText = { errors["cardExpireYear"]?.let { Text(it)}},
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
            )
        }
        Text(text = "CVV", fontSize = 16.sp)
        OutlinedTextField(
            singleLine = true,
            placeholder = { Text(text = "123") },
            value = fieldsToUpdate.cardCVV,
            onValueChange = { if (it.length <= 3) fieldsToUpdate = fieldsToUpdate.copy(cardCVV = it) },
            isError = errors.get("cardCVV") != null,
            supportingText = { errors["cardCVV"]?.let { Text(it)}},
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )
        //Spacer(modifier = Modifier.padding(4.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = ShapeDefaults.ExtraSmall,
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.verde_progetto)),
            onClick = {
                Log.d("UpdateProfileScreen", "Dati da inviare: $fieldsToUpdate")
                errors = ViewModelFormAccount.handleSubmitForm(fieldsToUpdate)
                // Ad ogni modifica, errors si aggiorna, quando tutti i campi sono null, invio i dati al server
                //Log.d("UpdateProfileScreen", "Errori arrivati nello screen: $errors") //non riceve le modifiche in tempo
            }
        ) {
            Text(text = "CONFERMA MODIFICHE")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOutlinedTextField() {
    val errorsPreview = mapOf(
        "cardCV" to "CVV non valido",
    )
    Column {
        Text(text = "CVV", fontSize = 16.sp, fontFamily = FontFamily.Serif)
        OutlinedTextField(
            singleLine = true,
            placeholder = { Text(text = "123") },
            //modifier = Modifier.padding(4.dp),
            textStyle = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            ),
            value = "Pietanza alla cotoletta",
            onValueChange = { },
            isError = false,
            supportingText = { errorsPreview["cardCVV"]?.let { Text(it)}},
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )
    }

}
