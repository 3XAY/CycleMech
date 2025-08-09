package com.xyzian.cyclemech

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilesScreen(navController: NavController){
    //Gives Android context which allows you to init the data managers (saved miles + parts)
    val context = LocalContext.current
    val prefsManager = remember {SharedPreferencesManager(context)} //Remember makes it so that instances of the data managers aren't recreated on every recomposition
    val partsRepository = remember {PartsRepository(context)}
    var miles by remember {mutableStateOf(prefsManager.getMiles())} //mutableStateOf makes it so that when the value changes, it recomposes the screen

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log your miles") },
                navigationIcon = { //The back button "pops" the screen off of the navigation stack, bringing it back to the previous screen
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
            )}
           })
        }
    ) {
        paddingValues ->
        Column( //Arranges items vertically
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Miles: $miles",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        if(miles > 0.0F){
                            miles-=0.5F
                            prefsManager.setMiles(miles) //Updated the miles and saved it to SharedPreferences (storage)

                            val currentParts = partsRepository.loadParts() // Updates and saves the individual parts' miles
                            val updatedParts = currentParts.map{ part ->
                                part.copy(
                                    miles = if(miles - part.startMiles >= 0.0F) miles - part.startMiles else 0.0F
                                )
                            }
                            partsRepository.saveParts(updatedParts)
                        }
                        else{
                            miles = 0.0F
                            prefsManager.setMiles(miles)
                        }
                    }
                ) {
                    Text(text = "-")
                }
                Button( //Does the same as removing miles
                    onClick = {
                        miles+=0.5F
                        prefsManager.setMiles(miles)
                        val currentParts = partsRepository.loadParts()
                        val updatedParts = currentParts.map{ part ->
                            part.copy(miles = miles - part.startMiles)
                        }
                        partsRepository.saveParts(updatedParts)
                    }
                ) {
                    Text(text = "+")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MilesScreenPreview(){
    val navController = rememberNavController()
    MilesScreen(navController = navController)
}