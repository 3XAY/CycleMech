package com.xyzian.cyclemech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xyzian.cyclemech.ui.theme.CycleMechTheme

class MainActivity : ComponentActivity() {
    //This method is called on when the app starts up
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { //Everything in here defines the UI
            CycleMechTheme { //This function applies the app's theme to everything within it such as colors and fonts
                val navController = rememberNavController() //This is what allows you to use the system back button and go back to the correct page, it remembers your navigation states
                NavHost(navController = navController, startDestination = "home_screen") { //This is the screen manager (kinda like window manager in Linux) that shows the correct screen (composable)
                    composable("home_screen") {
                        HomeScreen(navController = navController)
                    }
                    composable("miles_screen") {
                        MilesScreen(navController = navController)
                    }
                    composable("parts_screen"){
                        PartsScreen(navController = navController)
                    }
                    composable(
                        "part_details/{partID}", //The {partID} is a parameter which allows you to go to each part's specific details screen
                        arguments = listOf(navArgument("partID") {type = NavType.IntType})
                    ) { backStackEntry ->
                        backStackEntry.arguments?.getInt("partID")?.let {partID -> //Grabs the part ID and displays it if it exists
                            PartDetailsScreen(navController, partID)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) //Enables experimental features
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val prefsManager = remember {SharedPreferencesManager(context)} //Remember makes it so that instances of the data managers aren't recreated on every recomposition
    var miles by remember { mutableStateOf(prefsManager.getMiles()) } //mutableStateOf makes it so that when the value changes, it recomposes the screen

    Scaffold( //A basic pre-made layout that gives you a structure with a top and bottom bar
        topBar = {
            TopAppBar( //The top bar contains things like the back button or a settings icon (In this case, the settings icon)
                title = { Text("") },
                /*navigationIcon = { //Allows you to have an icon on the left hand side of the screen
                    IconButton(onClick = { /* TODO: Navigate to settings screen */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings Icon"
                        )
                    }
                }*/
            )
        },
        bottomBar = {
            BottomAppBar(tonalElevation = 0.dp) { //A bar at the bottom of the screen (where the buttons to switch between the parts and mile screen goes), tonal elevation makes it so the colors match
                Row( //Arranges the items (in this case buttons) horizontally
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("parts_screen") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(65.dp)
                    ) {
                        Text("Parts List", fontSize = 32.sp)
                    }
                    /*Button(
                        onClick = { navController.navigate("miles_screen") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Miles")
                    }*/
                }
            }
        }
    ) { paddingValues -> //Used to ensure that the content doesn't overlap the top/bottom bars, contains the main content of the screen (in between the bars)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text(
                text = "Miles: $miles",
                fontSize = 54.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(
                onClick = { navController.navigate("miles_screen") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(60.dp)
                    .offset(y = 25.dp)
            ) {
                Text("Add Miles", fontSize = 32.sp, textAlign = TextAlign.Center)
            }
        }
    }
}


@Preview(showBackground = true) //This annotation enables you to see the preview without having to run the app
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController)
}