package com.xyzian.cyclemech

import android.view.RoundedCorner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    var inputMiles by remember {mutableStateOf("")} //For the text input

    LaunchedEffect(inputMiles){} //This makes sure the TextField always shows the current value of inputMiles

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log your miles") },
                navigationIcon = { //The back button "pops" the screen off of the navigation stack, bringing it back to the previous screen
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                            )
                    }
                }
            )
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
            Button( //Does the same as removing miles
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                   val currentInput = inputMiles.toFloatOrNull() ?: 0.0F
                   inputMiles = (currentInput + 0.5F).toString()
                }
            ) {
                Text(text = "+", fontSize = 100.sp)
            }
            TextField( //Allows people to type their mile changes in instead of having to click
                value = inputMiles,
                onValueChange = {newValue ->
                    inputMiles = newValue
                },
                label = {Text("Miles", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .padding(top = 32.dp)
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 30.sp, textAlign = TextAlign.Center)
            )
            Button(
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val currentInput = inputMiles.toFloatOrNull() ?: 0.0F
                    inputMiles = (currentInput - 0.5F).toString()
                }
            ) {
                Text(text = "-", fontSize = 100.sp)
              }
            Button( //Save button
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 128.dp),
                onClick = {
                    val milesToAdd = inputMiles.toFloatOrNull() ?: 0.0F
                    miles += milesToAdd

                    if (miles < 0.0F) {
                        miles = 0.0F
                    }

                    prefsManager.setMiles(miles)
                    val currentParts = partsRepository.loadParts()
                    val updatedParts = currentParts.map { part ->
                        part.copy(
                            miles = miles - part.startMiles
                        )
                    }
                    partsRepository.saveParts(updatedParts)
                    navController.popBackStack()
                }
            ) {
                Text(text = "Save", fontSize = 32.sp)
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