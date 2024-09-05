package com.example.healthcatalyst1

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthcatalyst1.ui.theme.HealthCatalyst1Theme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        val serviceIntent = Intent(this, DocumentFetchService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        val serviceIntent1 = Intent(this, DocumentFetchService1::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent1)
        } else {
            startService(serviceIntent1)
        }
       // documentFetcher = DocumentFetcher(this)
       // documentFetcher1 = DocumentFetcher1(this)
        setContent {
            HealthCatalyst1Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp()
                }
            }
        }
    }
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
                Text("This app needs notification permission to work properly. Please grant the permissions when asked.")
            }
        )
    }

    Navigation()
}
@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "splash_screen") {
        composable("splash_screen") {
            SplashScreen(navController = navController)
        }
        composable("login_screen") {
            LoginScreen(navController = navController)
        }
        composable("main_screen") {
            MainScreen(navController = navController)
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
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
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
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("                Health Cat") },
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
    }
}

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

            Spacer(modifier = Modifier.height(18.dp))

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
    }
}


