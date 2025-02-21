package com.example.progetto.view.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progetto.view.commons.LoadingScreen
import com.example.progetto.view.commons.MenuItem
import com.example.progetto.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel
) {
    val menuStateResult by viewModel.menuState.collectAsState()
    val positionState by viewModel.positionState.collectAsState()
    val indirizzo = positionState.positionUser?.address

    LaunchedEffect(key1 = Unit) {
        viewModel.fetchAllMenus()
    }

    if (menuStateResult.menus.isEmpty()) {
        Log.d("HomeScreen", "Nessun menu disponibile per ora...")
        LoadingScreen()
    } else {
        Log.d("HomeScreen", "Menu disponibili: ${menuStateResult.menus.size}")

        Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Menu vicini a $indirizzo", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))

            LazyColumn {
                items(menuStateResult.menus) { menu ->
                    MenuItem(
                        viewModel = viewModel,
                        menu = menu,
                        onPress = {
                            viewModel.onMenuSelection(menu.menu.mid)
                            viewModel.changeScreen("Dettagli")
                        }
                    )
                }
            }
        }
    }
}
