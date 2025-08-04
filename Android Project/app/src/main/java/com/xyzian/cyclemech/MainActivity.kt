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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xyzian.cyclemech.ui.theme.CycleMechTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CycleMechTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home_screen") {
                    composable("home_screen") {
                        HomeScreen(navController = navController)
                    }
                    composable("miles_screen") {
                        MilesScreen(navController = navController)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CycleMech") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Navigate to settings screen */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings Icon"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = { /* TODO: Navigate to All Parts screen */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Parts List")
                    }
                    Button(
                        onClick = { navController.navigate("miles_screen") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Miles")
                    }
                }
            }
        }
    ) { paddingValues ->
        Text(
            text = "Welcome to CycleMech",
            modifier = Modifier.padding(paddingValues)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController)
}