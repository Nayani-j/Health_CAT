package com.example.health_cat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.example.health_cat.ui.theme.Health_CatTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.Scaffold as Scaffold1

class MainActivity : ComponentActivity() {


    lateinit var geofencingStateLiveData: MutableLiveData<Boolean>
    val GEOFENCE_LOCATION_REQUEST_CODE = 1001


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleQuantityDecrease(this)
        FirebaseApp.initializeApp(this)
        val serviceIntent = Intent(this, DocumentFetchService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }




        geofencingStateLiveData = MutableLiveData<Boolean>()

        requestGeofencePermission()

        setContent {
            Health_CatTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(id = R.color.background))
                ) {
                    MyApp()
                }
            }
        }
    }

    private fun updateGeofencingState(isActive: Boolean) {
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
            .putBoolean("geofencing_active", isActive)
            .apply()
        geofencingStateLiveData.value = isActive
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestGeofencePermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        ActivityCompat.requestPermissions(this, permissions, GEOFENCE_LOCATION_REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GEOFENCE_LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with geofencing
                Toast.makeText(this, "Geofencing permission granted", Toast.LENGTH_SHORT).show()
                val serviceIntent = Intent(this, LocationService::class.java)
                serviceIntent.action = LocationService.ACTION_START
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }

                updateGeofencingState(true)
            } else {
                // Permission denied handling...
                updateGeofencingState(false)
            }
        }
    }

    /* private fun startLocationService() {
         Intent(applicationContext, LocationService::class.java).apply {
             action = LocationService.ACTION_START
             startService(this)
         }
     }*/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startLocationService() {

    }


    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun MyApp() {

        var showDialog by remember { mutableStateOf(true) }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("OK")
                    }
                },
                title = {
                    Text(text = "Welcome")
                },
                text = {
                    Text("This app needs 'location permission all the time' and 'notification permission' to work properly. Please grant the permissions when asked.")
                }
            )
        }

        Navigation()
    }





    @Composable
    fun Navigation() {
        val locationViewModel: LocationViewModel = viewModel()
        var la by remember { mutableStateOf(0.0) }
        var lt by remember { mutableStateOf(0.0) }
        val context = LocalContext.current
        val locationUtils = LocationUtils(context)
        val navController = rememberNavController()


        NavHost(navController, startDestination = "splash_screen") {
            composable("splash_screen") {
                SplashScreen(navController = navController)
            }
            composable("register_screen") {
                RegisterScreen(navController = navController)
            }
            composable("login_screen") {
                LoginScreen(navController = navController)
            }
            composable("main_screen") {
                MainScreen(navController = navController)
            }
            /*     composable("main_screen") {
                // Show side menu on the main screen

                    MainScreen(navController, drawerState, scope)

            }*/
            composable("mm") {
                mm(navController = navController)
            }
            composable("add_reminder_screen") {

                AddReminderScreen(
                    locationUtils,
                    locationViewModel,
                    navController,
                    context,
                    locationViewModel.address.value.firstOrNull()?.formatted_address
                        ?: "No Address", la, lt
                )

            }

            composable("add_reminder_screen_1") {

                Trip(
                    locationUtils,
                    locationViewModel,
                    navController,
                    context,
                    locationViewModel.address.value.firstOrNull()?.formatted_address
                        ?: "No Address", la, lt
                )

            }

            dialog("locationscreen") {
                locationViewModel.location.value?.let { it1 ->
                    LocationSelectionScreen(
                        location = it1,
                        onLocationSelected = { locationdata ->
                            locationViewModel.fetchAddress("${locationdata.latitude},${locationdata.longitude}")
                            la = locationdata.latitude
                            lt = locationdata.longitude

                            navController.popBackStack()
                        })
                }
            }
        }
    }




        @Composable
        fun SplashScreen(navController: NavController) {
            LaunchedEffect(key1 = true) {
                delay(10000L)
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    navController.navigate("main_screen") {
                        popUpTo("splash_screen") { inclusive = true }
                    }
                } else {
                    navController.navigate("login_screen") {
                        popUpTo("splash_screen") { inclusive = true }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.saa),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        @Composable
        fun LoginScreen(navController: NavController) {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var loginError by remember { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .background(color = colorResource(id = R.color.background))
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        textStyle = TextStyle(color = Color.Black),
                        label = {
                            Text(
                                "email ",
                                color = colorResource(id = R.color.black)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        textStyle = TextStyle(color = Color.Black),
                        label = {
                            Text(
                                "password ",
                                color = colorResource(id = R.color.black)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (loginError.isNotEmpty()) {
                        Text(loginError)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Button(
                        onClick = {

                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        navController.navigate("main_screen")
                                    } else {
                                        loginError = task.exception?.message ?: "Login failed"
                                    }
                                }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Login")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { navController.navigate("register_screen") },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Register")
                    }
                }
            }


        @Composable
        fun RegisterScreen(navController: NavController) {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var confirmPassword by remember { mutableStateOf("") }
            var registerError by remember { mutableStateOf("") }
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.fg),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.matchParentSize()
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .background(color = colorResource(id = R.color.background))
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        textStyle = TextStyle(color = Color.Black),
                        label = {
                            Text(
                                "email ",
                                color = colorResource(id = R.color.black)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        textStyle = TextStyle(color = Color.Black),
                        label = {
                            Text(
                                "password ",
                                color = colorResource(id = R.color.black)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        textStyle = TextStyle(color = Color.Black),
                        label = {
                            Text(
                                "confirmPassword ",
                                color = colorResource(id = R.color.black)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (registerError.isNotEmpty()) {
                        Text(registerError)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Button(
                        onClick = {
                            if (password == confirmPassword) {
                                FirebaseAuth.getInstance()
                                    .createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val user = hashMapOf(
                                                "email" to email
                                            )
                                            FirebaseFirestore.getInstance().collection("users")
                                                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                                .set(user)
                                                .addOnSuccessListener {
                                                    navController.navigate("login_screen")
                                                }
                                                .addOnFailureListener { e ->
                                                    registerError =
                                                        e.message ?: "Registration failed"
                                                }
                                        } else {
                                            registerError =
                                                task.exception?.message ?: "Registration failed"
                                        }
                                    }
                            } else {
                                registerError = "Passwords do not match"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Register")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { navController.navigate("login_screen") },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Back to Login")
                    }
                }
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(

        navController: NavHostController,
    ) {

        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(250.dp) // Set the width of the drawer content
                ) {
                    DrawerContent(navController)
                }
            }
        ) {
            Scaffold1(
                topBar = {
                    TopAppBar(
                        title = { Text("                      Health Cat") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF8FCEF2), // Background color of the top bar
                                titleContentColor = Color.Black,    // Title text color
                                actionIconContentColor = Color.White // Navigation icon color
                            )

                    )
                }
            ) { paddingValues ->
                // Main Screen Content
                Box(modifier=Modifier.fillMaxSize()){
                    Image(
                        painter=painterResource(id = R.drawable.ll),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier=Modifier.matchParentSize()
                    )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {





                }
            }
        }
    }}

    @Composable
    fun DrawerContent(navController: NavHostController) {
        Box(modifier=Modifier.fillMaxSize().background(Color.White)){

            Image(
                painter=painterResource(id = R.drawable.fg),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier=Modifier.matchParentSize()
            )
        Column(
            modifier = Modifier
                .fillMaxSize(),
                /*.background(colorResource(id = R.color.background)),*/
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("mm") },modifier = Modifier
                .width(200.dp)) {
                Text("Add medicine tracker")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate("add_reminder_screen") },modifier = Modifier
                .width(200.dp)) {
                Text("Add Reminders")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { navController.navigate("add_reminder_screen_1") },modifier = Modifier
                .width(200.dp)) {
                Text("Add trackers for trips")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val firestore = FirebaseFirestore.getInstance()
                    val db = FirebaseFirestore.getInstance()
                    // val userId = FirebaseAuth.getInstance().currentUser?.uid
                    firestore.document("locations/vb10VoOOfI5asimNHeBL")
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val location = document.getString("location")

                                val message = "It's an emergency"
                                val locationData = hashMapOf(
                                    "place" to location,
                                    "notification" to message,
                                    "timestamp" to FieldValue.serverTimestamp(),

                                    )
                                db.collection("emergency").document("xY4VKjrZsqbdkvUVvhcx")
                                    .set(locationData, SetOptions.merge())
                                    .addOnSuccessListener {
                                        // Successfully updated location
                                    }
                                    .addOnFailureListener {
                                        // Failed to update location
                                    }}}
                    navController.navigate("main_screen") },
                modifier = Modifier.width(200.dp))

             {
                Text("Emergency")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val db = FirebaseFirestore.getInstance()
                    // val userId = FirebaseAuth.getInstance().currentUser?.uid

                    val location = "none"

                    val message = "No more emergency"
                    val locationData = hashMapOf(
                        "place" to location,
                        "notification" to message,
                        "timestamp" to FieldValue.serverTimestamp(),

                        )
                    db.collection("emergency").document("xY4VKjrZsqbdkvUVvhcx")
                        .set(locationData, SetOptions.merge())
                        .addOnSuccessListener {
                            // Successfully updated location
                        }
                        .addOnFailureListener {
                            // Failed to update location
                        }
                    navController.navigate("main_screen") },
                modifier = Modifier.width(200.dp))

             {
                Text("No emergency")
            }


            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login_screen") {
                    popUpTo("main_screen") { inclusive = true }
                }
            },modifier = Modifier
                .width(200.dp)) {
                Text("Logout")
            }
        }
    }}


    @Composable
    fun AddReminderScreen(
        locationUtils: LocationUtils,
        viewModel: LocationViewModel,
        navController: NavController,
        context: Context,
        address: String,
        latitude: Double,
        longitude: Double
    ) {
        var reminderTitle by remember { mutableStateOf("") }
        var reminderLocation by remember { mutableStateOf(address) }
        var geofenceRadius by remember { mutableStateOf("100") } // Default radius in meters
        var errorMessage by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(color = colorResource(id = R.color.background))
                .fillMaxWidth(),
        ) {
            val requestPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
                onResult = { permissions ->
                    if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                        && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    ) {
                        // I HAVE ACCESS to location

                        locationUtils.requestLocationUpdates(viewModel = viewModel)
                    } else {
                        val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                            context as MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) || ActivityCompat.shouldShowRequestPermissionRationale(
                            context as MainActivity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) || ActivityCompat.shouldShowRequestPermissionRationale(
                            context as MainActivity,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )

                        if (rationaleRequired) {
                            Toast.makeText(
                                context,
                                "Location Permission is required for this feature to work",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        } else {
                            Toast.makeText(
                                context,
                                "Location Permission is required. Please enable it in the Android Settings",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = reminderTitle,
                    onValueChange = { reminderTitle = it },
                    textStyle = TextStyle(color = Color.Black),
                    label = {
                        Text(
                            "Enter Reminder Title",
                            color = colorResource(id = R.color.black)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    if (locationUtils.hasLocationPermission(context)) {
                        locationUtils.requestLocationUpdates(viewModel)
                        navController.navigate("locationscreen") {
                            this.launchSingleTop
                        }
                    } else {
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }

                }) {
                    Text("address")
                }
                Spacer(modifier = Modifier.height(16.dp))
                reminderLocation = address

                OutlinedTextField(
                    value = geofenceRadius,
                    onValueChange = { geofenceRadius = it },
                    textStyle = TextStyle(color = Color.Black),
                    label = {
                        Text(
                            "Geofence Radius (meters)",
                            color = colorResource(id = R.color.black)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Button(
                    onClick = {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            val reminder = hashMapOf(
                                "title" to reminderTitle,
                                "address" to reminderLocation,
                                "userId" to userId,
                                "latitude" to latitude,//viewModel.location.value?.latitude,
                                "longitude" to longitude, //viewModel.location.value?.longitude,


                                ("radius" to geofenceRadius.toFloatOrNull()) as Pair<Any, Any>
                            )

                            FirebaseFirestore.getInstance().collection("reminders")
                                .add(reminder)
                                .addOnSuccessListener { documentReference ->
                                    val geofencingClient = GeofencingClient(context)
                                    geofencingClient.addGeofence(
                                        documentReference.id,
                                        latitude,    //viewModel.location.value?.latitude ?: 0.0,
                                        longitude,   //viewModel.location.value?.longitude ?: 0.0,

                                        geofenceRadius.toFloatOrNull() ?: 100f
                                    )
                                    navController.navigate("main_screen") {
                                        popUpTo("add_reminder_screen") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    errorMessage = "Error: ${e.message}"
                                }
                        } else {
                            errorMessage = "User not authenticated"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)

                ) {
                    Text("Save Reminder and Add Geofence")
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }


    @Composable
    fun Trip(
        locationUtils: LocationUtils,
        viewModel: LocationViewModel,
        navController: NavController,
        context: Context,
        address: String,
        latitude: Double,
        longitude: Double
    ) {
        var reminderTitle by remember { mutableStateOf("") }
        var reminderLocation by remember { mutableStateOf(address) }
        var geofenceRadius by remember { mutableStateOf("100") } // Default radius in meters
        var errorMessage by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(color = colorResource(id = R.color.background))
                .fillMaxWidth(),
        ) {
            val requestPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
                onResult = { permissions ->
                    if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                        && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    ) {
                        // I HAVE ACCESS to location

                        locationUtils.requestLocationUpdates(viewModel = viewModel)
                    } else {
                        val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                            context as MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) || ActivityCompat.shouldShowRequestPermissionRationale(
                            context as MainActivity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) || ActivityCompat.shouldShowRequestPermissionRationale(
                            context as MainActivity,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )

                        if (rationaleRequired) {
                            Toast.makeText(
                                context,
                                "Location Permission is required for this feature to work",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        } else {
                            Toast.makeText(
                                context,
                                "Location Permission is required. Please enable it in the Android Settings",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = reminderTitle,
                    onValueChange = { reminderTitle = it },
                    textStyle = TextStyle(color = Color.Black),
                    label = {
                        Text(
                            "Enter Reminder Title",
                            color = colorResource(id = R.color.black)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    if (locationUtils.hasLocationPermission(context)) {
                        locationUtils.requestLocationUpdates(viewModel)
                        navController.navigate("locationscreen") {
                            this.launchSingleTop
                        }
                    } else {
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }

                }) {
                    Text("address")
                }
                Spacer(modifier = Modifier.height(16.dp))
                reminderLocation = address

                OutlinedTextField(
                    value = geofenceRadius,
                    onValueChange = { geofenceRadius = it },
                    textStyle = TextStyle(color = Color.Black),
                    label = {
                        Text(
                            "Geofence Radius (meters)",
                            color = colorResource(id = R.color.black)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Button(
                    onClick = {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            val reminder = hashMapOf(
                                "title" to reminderTitle,
                                "address" to reminderLocation,
                                "userId" to userId,
                                "latitude" to latitude,//viewModel.location.value?.latitude,
                                "longitude" to longitude, //viewModel.location.value?.longitude,


                                ("radius" to geofenceRadius.toFloatOrNull()) as Pair<Any, Any>
                            )

                            FirebaseFirestore.getInstance().collection("reminders")
                                .add(reminder)
                                .addOnSuccessListener { documentReference ->
                                    val geofencingClient = GeofencingClient(context)
                                    geofencingClient.addGeofence(
                                        documentReference.id,
                                        latitude,
                                        longitude,

                                        geofenceRadius.toFloatOrNull() ?: 100f
                                    )
                                    navController.navigate("main_screen") {
                                        popUpTo("add_reminder_screen_1") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    errorMessage = "Error: ${e.message}"
                                }
                        } else {
                            errorMessage = "User not authenticated"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)

                ) {
                    Text("Save Reminder and Add Geofence")
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
    @Composable
    fun mm(navController: NavController)
    {
        var medicine by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("0") }
        var errorMessage by remember { mutableStateOf("") }
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(color = colorResource(id = R.color.background))
                .fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = medicine,
                onValueChange = { medicine = it },
                textStyle = TextStyle(color = Color.Black),
                label = {
                    Text(
                        "Enter Name Of The Medicine",
                        color = colorResource(id = R.color.black)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                textStyle = TextStyle(color = Color.Black),
                label = {
                    Text(
                        "Enter The Quantity",
                        color = colorResource(id = R.color.black)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        val medicine = hashMapOf(
                            "mm" to medicine,

                            "userId" to userId,
                            //viewModel.location.value?.longitude,


                            ("mq" to quantity.toFloatOrNull()) as Pair<Any, Any>
                        )

                        FirebaseFirestore.getInstance().collection("medicines")
                            .add(medicine)
                            .addOnSuccessListener { documentReference ->

                                navController.navigate("main_screen") {
                                    popUpTo("mm") { inclusive = true }
                                }
                            }
                            .addOnFailureListener { e ->
                                errorMessage = "Error: ${e.message}"
                            }
                    } else {
                        errorMessage = "User not authenticated"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)

            ) {
                Text("Save")
            }

        }
    }
}
