package com.example.progetto

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.progetto.model.datasources.ApiController
import com.example.progetto.model.datasources.DBController
import com.example.progetto.model.datasources.PreferencesController
import com.example.progetto.model.repositories.MenuRepository
import com.example.progetto.model.repositories.OrderRepository
import com.example.progetto.model.repositories.UserRepository
import com.example.progetto.ui.theme.ProgettoTheme
import com.example.progetto.view.commons.BeautifulCustomDialog
import com.example.progetto.view.commons.StartLoadingScreen
import com.example.progetto.view.screens.HomeScreen
import com.example.progetto.view.screens.LastOrderScreen
import com.example.progetto.view.screens.MenuDetailsScreen
import com.example.progetto.view.screens.ProfileScreen
import com.example.progetto.view.screens.UpdateProfileScreen
import com.example.progetto.viewmodel.MainViewModel
import com.example.progetto.viewmodel.ViewModelFormAccount
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val Context.dataStore by preferencesDataStore(name = "mc")

    //private val formViewModel: ViewModelFormAccount by viewModels()
    //by viewModels<ViewModelFormAccount> { viewModelFactory { ViewModelFormAccount() } }
    private lateinit var myViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiController = ApiController //TODO ()
        val dbController = DBController(this)
        val preferencesController = PreferencesController(dataStore) //todo getInstance(dataStore)

        val userRepository = UserRepository(apiController, dbController = dbController, preferencesController = preferencesController)
        val menuRepository = MenuRepository(apiController, dbController = dbController, preferencesController = preferencesController)
        val orderRepository = OrderRepository(apiController, dbController = dbController, preferencesController = preferencesController)

        val geocoder = Geocoder(this, Locale.getDefault())
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val viewModelFactory = viewModelFactory {
            initializer {
                MainViewModel(
                    userRepository,
                    menuRepository,
                    orderRepository,
                    geocoder = geocoder,
                    locationClient = fusedLocationClient
                )
            }
        }

        myViewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        //val myViewModel: MainViewModel by viewModels<MainViewModel> { viewModelFactory }
        enableEdgeToEdge()
        setContent {
            ProgettoTheme {
                MangiaEBasta(myViewModel/*, formViewModel*/) //TODO
            }
        }
    }

    override fun onStop() {
        super.onStop()
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("MainActivity", "onStop called -> lastScreen = ${myViewModel.appState.value.currentScreen}")
            myViewModel.saveLastScreenToStorage(myViewModel.appState.value.currentScreen)
        }
    }
}

@Composable
fun MangiaEBasta(viewModel: MainViewModel/*, formViewModel: ViewModelFormAccount*/) {
    var showDialog by remember { mutableStateOf(false) }
    val appState by viewModel.appState.collectAsState()
    val positionState by viewModel.positionState.collectAsState()

    val context = LocalContext.current //passare a checkLocationPermission

    //Column {
    when {
        showDialog -> {
            BeautifulCustomDialog(
                onDismissRequest = { showDialog = false },
                title = "Lettura posizione non autorizzata",
                description = "Per il momento verrano visualizzati i ristoranti vicini a Milano"
            )
        }
    }
    //}

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "isGranted=true Posso calcolare la posizione...")
            viewModel.calcoloPosizione()
        } else {
            Log.d("MainActivity", "isGranted=false Permessi non concessi -> set DefaultLocation...")
            viewModel.setDefaultUserLocation()
            showDialog = true
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.checkLocationPermission(context)) { //check se l'utente ha autorizzato l'accesso
            viewModel.calcoloPosizione()
        } else {
            Log.d("MainActivity", "Avvio richiesta permessi...")
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    if (appState.isLoading || positionState.positionUser == null) {
        StartLoadingScreen(permissionGranted = positionState.permissionsGranted)
    } else {
        Scaffold(
            containerColor = colorResource(id = R.color.verde_progetto),
            topBar = {
                TopNavigation(viewModel)
            },
            bottomBar = {
                BottomNavigation(viewModel)
            }
        ) { innerPadding ->
            Column(
                Modifier
                    .padding(innerPadding)
                    .background(Color.White)
                    .fillMaxSize()
            ) {
                when (appState.currentScreen) {
                    "Home" -> HomeScreen(viewModel)
                    "Dettagli" -> MenuDetailsScreen(viewModel)
                    "Ordine" -> LastOrderScreen(viewModel)
                    "Profilo" -> ProfileScreen(viewModel)
                    "UpdateProfilo" -> UpdateProfileScreen (viewModel/*, formViewModel*/) //TODO
                }
            }
        }
    }
}

@Composable
fun TopNavigation(viewModel: MainViewModel) {
    val appState by viewModel.appState.collectAsState()

    Column (
        modifier = Modifier
            .background(colorResource(id = R.color.verde_progetto))
            .fillMaxWidth()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.fillMaxWidth()) {
            if (appState.currentScreen == "Dettagli" || appState.currentScreen == "UpdateProfilo") {
                FilledIconButton(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 4.dp),
                    colors = IconButtonDefaults.iconButtonColors(Color.White),//colorResource(R.color.rosso_progetto)),
                    onClick = {
                        if (appState.currentScreen == "Dettagli") {
                            viewModel.changeScreen("Home")
                        } else { //if screen == "UpdateProfilo"
                            viewModel.changeScreen("Profilo")
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "indietro",
                        tint = colorResource(id = R.color.verde_progetto),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            // Titolo topbar
            Text(
                text = when (appState.currentScreen) {
                    "Dettagli" -> "Dettagli Menu"
                    "Ordine" -> "Ultimo Ordine"
                    "Profilo" -> "Profilo"
                    "UpdateProfilo" -> "Modifica Profilo"
                    else -> "Mangia e Basta"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .align(Alignment.Center)
            )
        }

    }
}

@Composable
fun BottomNavigation(viewModel: MainViewModel) {
    val appState by viewModel.appState.collectAsState()

    NavigationBar(
        containerColor = colorResource(id = R.color.verde_progetto),
    ) {
        NavigationBarItem(
            selected = appState.currentScreen == "Home",
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = colorResource(R.color.verde_chiaro_progetto),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White
            ),
            onClick = {
                viewModel.changeScreen("Home")
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "home",
                    tint = Color.White
                )
            },
            label = { Text(text = "Home", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        )
        NavigationBarItem(
            selected = appState.currentScreen == "Ordine",
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = colorResource(R.color.verde_chiaro_progetto),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White
            ),
            onClick = {
                viewModel.changeScreen("Ordine")
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "ordine",
                    tint = Color.White
                )
            },
            label = { Text(text = "Ordine", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        )
        NavigationBarItem(
            selected = appState.currentScreen == "Profilo",
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = colorResource(R.color.verde_chiaro_progetto),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White
            ),
            onClick = {
                viewModel.changeScreen("Profilo")
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            label = { Text(text = "Profilo", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        )
    }
}