package com.example.progetto.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progetto.model.dataclasses.Location
import com.example.progetto.model.dataclasses.Menu
import com.example.progetto.model.dataclasses.MenuDetailsWithImage
import com.example.progetto.model.dataclasses.MenuWithImage
import com.example.progetto.model.dataclasses.OrderDetails
import com.example.progetto.model.dataclasses.UpdateUserParamsWithSid
import com.example.progetto.model.dataclasses.UserDetails
import com.example.progetto.model.repositories.MenuRepository
import com.example.progetto.model.repositories.OrderRepository
import com.example.progetto.model.repositories.UserRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.floor

//DATA CLASSES STATEs ciascuna contiene i dati pronti per le componenti
data class UserState( //Profilo
    val user: UserDetails? = null,
    val isUserRegistered: Boolean = false,
)

data class MenuState( //Home e MenuDetails
    val menus: List<MenuWithImage> = emptyList(),
    val selectedMenuMid: Int? = null,
    val selectedMenu: MenuDetailsWithImage? = null
)

data class LastOrderState( //Ordine e Profilo
    val lastOrder: OrderDetails? = null,
    val menuLastOrder: MenuDetailsWithImage? = null
)

data class PositionState( //Home o App, MenuDetails
    val positionUser: Location? = null,
    val permissionsGranted: Boolean = false,
    val hasCheckedPermissions: Boolean = false
)

data class AppState(
    val isLoading: Boolean = true,
    val isFirstLaunch: Boolean = true,
    val currentScreen: String = "Home",
    val avvisi: List<String> = emptyList(),
    val error: Error? = null
)

class MainViewModel(
    private val userRepository: UserRepository,
    private val menuRepository: MenuRepository,
    private val orderRepository: OrderRepository,
    private val geocoder : Geocoder,
    private val locationClient: FusedLocationProviderClient
) : ViewModel() {

    companion object {
        val DEFAULT_LOCATION = Location(
            lat = 45.4642,
            lng = 9.19,
            address = "Milano"
        )
    }
    private val TAG = MainViewModel::class.simpleName
    private val _sid = MutableStateFlow(null as String?)
    private val _uid = MutableStateFlow(null as Int?)

    // Variabili ad uso interno
    private var menusSenzaImmagini by mutableStateOf(emptyList<Menu>())

    // Stati da esporre alle componenti UI
    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState

    private val _menuState = MutableStateFlow(MenuState())
    val menuState: StateFlow<MenuState> = _menuState

    private val _lastOderState = MutableStateFlow(LastOrderState())
    val lastOrderState: StateFlow<LastOrderState> = _lastOderState

    private val _positionState = MutableStateFlow(PositionState())
    val positionState: StateFlow<PositionState> = _positionState

    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState

    // Eseguito all'inizializzazione del ViewModel
    init {
        setLoading(true) //all'avvio -> isLoading = true
        viewModelScope.launch {
            getSessionUser()
            //ottengo l'ultimo schermata visitata
            getLastScreenFromStorage()?.let {
                changeScreen(it)
            }
            if (_appState.value.currentScreen == "Dettagli") {
                _menuState.value = _menuState.value.copy(selectedMenuMid = menuRepository.getMenuMidFromStorage())
            }
            setLoading(false)
        }
    }


    // GESTIONE AppSTATE
    private fun setLoading(flag: Boolean) {
        _appState.value = _appState.value.copy(isLoading = flag)
    }

    fun changeScreen(screen: String) {
        _appState.value = _appState.value.copy(currentScreen = screen)
    }

    suspend fun saveLastScreenToStorage(screen: String) {
        if (screen == "Dettagli") {
            Log.d(TAG, "MenuMid da salvare: ${_menuState.value.selectedMenuMid}")
            menuRepository.saveMenuMidToStorage(_menuState.value.selectedMenuMid!!)
        }
        userRepository.saveLastScreen(screen)
    }

    private suspend fun getLastScreenFromStorage(): String? {
        //return una stringa, o null se non è salvata alcuna pagina
        return userRepository.getLastScreen()
    }

    fun onMenuSelection(mid: Int) {
        _menuState.value = _menuState.value.copy(selectedMenuMid = mid)
    }

    fun resetMenuSelection() {
        //resetto solo i dati del menu selezionato, non il mid, altrimenti non riesce a salvarlo quando va in backgroung, perchè null
        _menuState.value = _menuState.value.copy(selectedMenu = null)
        _appState.value = _appState.value.copy(avvisi = emptyList())
    }

    fun resetUserData() {
        _userState.value = _userState.value.copy(user = null)
    }

//    private suspend fun checkIsUserRegistered(): Boolean {
//        val isRegistered = userRepository.isRegistered()
//        _userState.value = _userState.value.copy(isUserRegistered = isRegistered)
//        return isRegistered
//    }

    // ************************************* GESTIONE DATI UTENTE: registrazione, recupero, e aggiornamento ************************
    private suspend fun getSessionUser() {
        try {
            val credentials = userRepository.getUserSession()
            _sid.value = credentials.sid
            _uid.value = credentials.uid
            //_appState.value = _appState.value.copy(isFirstLaunch = false)
        } catch (e: Error) {
            Log.e(TAG, "createSessioneUser Error: ${e.message}")
        } catch (e: CancellationException) { //cancellazione delle coroutine es. JSON non valido
            Log.e(TAG, "createSessioneUser CancellationError: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "createSessioneUser Exception: ${e.message}")
        }
    }

    suspend fun fetchUserDetails() { //modifica il valore di userState
        try {
            if (!userRepository.isRegistered()) { //modifico una porzione dello stato
                Log.d(TAG, "Utente non registrato")
                _userState.update { currentState ->
                    val initialValues = UserDetails(
                        firstName = "",
                        lastName = "",
                        cardFullName = "",
                        cardNumber = "",
                        cardExpireMonth = -1,
                        cardExpireYear = -1,
                        cardCVV = "",
                        uid = _uid.value!!,
                        lastOrderId = null,
                        orderStatus = null
                    )
                    currentState.copy(user = initialValues,isUserRegistered = false)
                }
            } else {
                Log.d(TAG, "Utente registrato. Ottenimento dati...")
                val userData = userRepository.getUserDetails(_sid.value!!, _uid.value!!)
                Log.i(TAG, "Dati utente ottenuti: $userData")
                _userState.value = _userState.value.copy(user = userData, isUserRegistered = true)
            }
        } catch (e: Error) {
            Log.e(TAG, "fetchUserDetails Error: ${e.message}")
        } catch (e: CancellationException) { //cancellazione delle coroutine es. JSON non valido
            Log.e(TAG, "fetchUserDetails CancellationError: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "fetchUserDetails Exception: ${e.message}")
        }
    }

    suspend fun updateUserData(userData: UpdateUserParamsWithSid): Boolean {
        // Aggiunta del sid al body della richiesta
        val newUserData = userData.copy(sid = _sid.value!!)
        Log.d(TAG, "updateUserData() aggiunta del sid ai dati: $newUserData")
        var success = false
        try {
            userRepository.updateUserDetails(_uid.value!!, newUserData)
            success = true
            _userState.value = _userState.value.copy(isUserRegistered = true)
        } catch (e: Error) {
            Log.e(TAG, "updateUserData Error: ${e.message}")
        } catch (e: CancellationException) { //cancellazione delle coroutine es. JSON non valido
            Log.e(TAG, "updateUserData CancellationError: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "updateUserData Exception: ${e.message}")
        }
        return success
    }

    //*********************************************** GESTIONE POSIZIONE UTENTE*************************************
    fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun calcoloPosizione() {
        _positionState.value = _positionState.value.copy(hasCheckedPermissions = true, permissionsGranted = true)

        val task = locationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        )
        try {
            viewModelScope.launch {
                val location = task.await()
                Log.i(TAG, "Posizione utente: $location")
                Log.i(TAG, "Posizione utente -> Lat: ${location.latitude}, Long: ${location.longitude}")
                _positionState.value = _positionState.value.copy(
                    positionUser = Location(
                        lat = location.latitude,
                        lng = location.longitude
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Errore ottenimento posizione ${e.message}")
        }
    }

    fun setDefaultUserLocation() {
        _positionState.value = _positionState.value.copy(positionUser = DEFAULT_LOCATION, permissionsGranted = false, hasCheckedPermissions = true)
    }

    private fun getUserLocation() : Location {
        val location = _positionState.value.positionUser

        return if (location != null && _positionState.value.permissionsGranted) {
            Log.d(TAG, "getUserLocation() User location: $location")
            Location(
                lat = location.lat,
                lng = location.lng
            )
        } else {
            Log.d(TAG, "getUserLocation() Default location: $DEFAULT_LOCATION")
            DEFAULT_LOCATION
        }
    }

    @Suppress("Deprecation")
    private suspend fun getAddressFromCoordinates(lat: Double, lng: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (addresses.isNullOrEmpty()) return@withContext ""

                val address = addresses[0]
                Log.i(TAG, "Indirizzo thoroughfare: ${address.thoroughfare}")
                Log.i(TAG, "Indirizzo completo lunghissimo: ${address.getAddressLine(0)}")
                address.thoroughfare ?: ""
            } catch (e: Exception) {
                Log.e(TAG, "getAddressFromCoordinates Errore: $e")
                ""
            }
        }
    }

    //*********************************** GESTIONE DATI MENU ********************************************
    suspend fun fetchAllMenus() {
        Log.d(TAG, "fetchAllMenus() Recupero dei menu vicini.. ma prima calcolo la posizione (ora DEFAULT)")
        val location = getUserLocation()

        try {
            menusSenzaImmagini = menuRepository.getMenuVicini(_sid.value!!, location.lat, location.lng)
            //Log.d(TAG, "fetchAllMenus() ${menusSenzaImmagini.joinToString("\n")}")

            //Recupero immagine
            val menusWithImage = menusSenzaImmagini.map { menu ->
                MenuWithImage(menu = menu, image = null)
            }

            menusWithImage.map {
                val image = menuRepository.getMenuImage(_sid.value!!, it.menu.mid, it.menu.imageVersion)
                it.image = image
            }
            // Ricavo l'indirizzo da mostrare in HomeScreen, se problemi di sincronizzazione scriverlo più in su
            val address = getAddressFromCoordinates(location.lat, location.lng)
            if (address != "") {
                _positionState.value = _positionState.value.copy(
                    positionUser = Location(
                        lat = location.lat,
                        lng = location.lng,
                        address = address
                    )
                )
            }

            Log.d(TAG, "fetchAllMenus() ${menusWithImage[0]}")
            _menuState.value = _menuState.value.copy(menus = menusWithImage)
        } catch (e: Error) {
            Log.e(TAG, "fetchAllMenus Error: ${e.message}")
        } catch (e: CancellationException) {
            Log.e(TAG, "fetchAllMenus CancellationError: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "fetchAllMenus Exception: ${e.message}")
        }
    }

    suspend fun fetchMenuDetails(mid: Int) {
        //Quando l'avvio init{} parte da MenuDetails, il mid viene recuperato dallo storage
        try {
            val location = getUserLocation()
            val menuDetails = menuRepository.getMenuDetails(_sid.value!!, mid, location.lat, location.lng)
            val image = menuRepository.getMenuImage(_sid.value!!, mid, menuDetails.imageVersion)
            _menuState.value = _menuState.value.copy(
                selectedMenu = MenuDetailsWithImage(
                    menuDetails = menuDetails,
                    image = image
                )
            )
        } catch (e: Error) {
            Log.e(TAG, "fetchMenuDetails Error: ${e.message}")
        } catch (e: CancellationException) {
            Log.e(TAG, "fetchMenuDetails CancellationError: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "fetchMenuDetails Exception: ${e.message}")
        }
    }

    fun formattedTime(minutes: Int): String {
        if (minutes < 60) {
            if (minutes == 0) return "< 1 min"
            return "$minutes min"
        }

        val hours = floor(minutes / 60.0).toInt() //Math.floor(...).toInt()
        val mins = minutes % 60
        if (hours > 1) {
            return "$hours ore e $mins min"
        }
        if (mins == 0) {
            return "$hours ora"
        }

        return "$hours ora e $mins min"
    }


    // ******************************** GESTIONE DATI ORDINE **************************************

    suspend fun canDoOrder() {
        val permissionGranted = _positionState.value.permissionsGranted
        val profileCompleted = userRepository.isRegistered()
        // se il profilo è completato ricava i dati dell'utente per avere _userState.value.user != null e poter confrontare orderStatus
        if (profileCompleted) fetchUserDetails()
        Log.d(TAG, "canDoOrder() Controllo userState=${_userState.value.user}.")
        val hasOrderInProgress = _userState.value.user?.orderStatus == "ON_DELIVERY" //all'avvio diretto in Dettagli dava problemi prima (corretto)

        Log.d(TAG, "canDoOrder() permissionGranted: $permissionGranted, profileCompleted: $profileCompleted, hasOrderInProgress: $hasOrderInProgress")

        val missing = mutableListOf<String>()
        if (!permissionGranted) missing.add("Hai negato l'accesso alla tua posizione")
        if (!profileCompleted) missing.add("Non hai ancora completato il profilo")
        if (hasOrderInProgress) missing.add("Hai un ordine in corso, attendi la consegna")

        _appState.value = _appState.value.copy(avvisi = missing)
    }

    suspend fun buyMenu(mid: Int): Boolean {
        Log.d(TAG, "buyMenu() Acquisto menu $mid...")
        val location = getUserLocation()
        var result = false
        try {
            val order = orderRepository.buyMenu(_sid.value!!, mid, Location(lat = location.lat, lng = location.lng))
            Log.d(TAG, "buyMenu() Order: $order")
            _lastOderState.value = _lastOderState.value.copy(lastOrder = order)
            _userState.value = _userState.value.copy(
                user = _userState.value.user?.copy(lastOrderId = order.oid, orderStatus = order.status)
            )
            result = true
        } catch (e: Error) {
            Log.e(TAG, "buyMenu Error: ${e.message}")
        } catch (e: CancellationException) {
            Log.e(TAG, "buyMenu CancellationError: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "buyMenu Exception: ${e.message}")
        }
        return result
    }

    suspend fun getOrderDetailsWithImage() {
        val isUserRegistered = userRepository.isRegistered()
        Log.d(
            TAG,
            "getOrderDetailsWithImage() Repository isRegistrato=$isUserRegistered e UtenteState isRegistrato=${_userState.value.isUserRegistered}"
        )
        if (!isUserRegistered) return

        try {
            Log.i(
                TAG,
                "Inizio getOrderDetailsWithImage() Sid=${_sid.value} e utente =${_userState.value.user}"
            )
            if (_userState.value.user?.lastOrderId == null) return

            val order =
                orderRepository.getOrderDetails(_sid.value!!, _userState.value.user?.lastOrderId!!)
            val menuDetails = menuRepository.getMenuDetails(
                _sid.value!!,
                order.mid
            ) //ho bisogno solo del nome e immagine, non mi interessa il tempo di consegna preciso
            val imageMenu =
                menuRepository.getMenuImage(_sid.value!!, menuDetails.mid, menuDetails.imageVersion)
            val infoComplete = MenuDetailsWithImage(menuDetails, imageMenu)
            _lastOderState.value = _lastOderState.value.copy(
                lastOrder = order,
                menuLastOrder = infoComplete
            )
            Log.d(
                TAG,
                "getOrderDetailsWithImage() Risultato lastOrder=$order e menuLastOrder=$infoComplete"
            )
        } catch (e: Error) {
            Log.e(TAG, "getOrderDetailsWithImage Error: ${e.message}")
        } catch (e: CancellationException) {
            Log.e(TAG, "getOrderDetailsWithImage CancellationError: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "getOrderDetailsWithImage Exception: ${e.message}")
        }
    }

}