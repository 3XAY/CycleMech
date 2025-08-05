package com.xyzian.cyclemech

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class BikePart(
    val id: Int,
    val name: String,
    val brand: String,
    val model: String,
    val miles: Int,
    val startMiles: Int,
    val endMiles: Int,
    val dateInstalled: Int,
    val endDate: Int,
    val price: Double,
    val notes: String
)

val bikeParts = listOf(
    BikePart(1, "Chain", "Shimano", "CN-HG54", 15, 0, 100,0, 0, 25.00, "10 Speed, Amazon"),
    BikePart(2, "Rear Derailleur", "Microshift", "Advent X", 15, 0, 100, 0, 0, 63.00, "With clutch, 365 Cycles"),
    BikePart(3, "Chainring", "Deckas", "N/A", 15, 0, 100,  0, 0, 10.00, "38T 104BCD, Amazon"),
    BikePart(4, "Cassette", "Microshift", "Advent X E-Series", 15, 0, 100,  0, 0, 47.00, "Amazon"),
    BikePart(5, "Tubes", "Goodyear", "N/A", 15, 0, 100,  0, 0, 12.00, "Walmart"),
    BikePart(6, "Tires", "Continental", "Cross King", 15, 0, 100,  0, 0, 63.00, "26x2.2, Al's (swapped in bike shop)"),
    BikePart(7, "Brake pads", "CNC", "N/A", 15, 0, 100,  0, 0, 5.00, "Part of a whole set ($27), Amazon"),
    BikePart(8, "Shifter", "Shimano", "SL-M6000", 15, 0, 100,  0, 0, 28.00, "Amazon")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartsScreen(navController: NavController){
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Parts List")},
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
            ) } }
    )
}
    ){
        paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bikeParts) { part ->
                BikePartItem(part = part)
            }
        }
    }}

@Composable
fun BikePartItem(part: BikePart) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(text = part.name)
            Text(text = "Miles: ${part.miles}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PartsScreenPreview(){
    val navController = rememberNavController()
    PartsScreen(navController = navController)
}