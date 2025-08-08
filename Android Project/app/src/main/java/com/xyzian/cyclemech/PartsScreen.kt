package com.xyzian.cyclemech

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.TimeZone

data class BikePart(
    val id: Int,
    val name: String,
    val brand: String,
    val model: String,
    val miles: Float,
    val startMiles: Float,
    val endMiles: Float,
    val dateInstalled: String,
    val price: Double,
    val notes: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartsScreen(navController: NavController){
    val context = LocalContext.current
    val partsRepository = remember {PartsRepository(context)}
    val prefsManager = remember {SharedPreferencesManager(context)}

    val bikeParts = remember{
        mutableStateListOf<BikePart>().apply{
            addAll(partsRepository.loadParts())
        }
    }

    val totalMiles = prefsManager.getMiles()
    val updatedParts = bikeParts.map{ part ->
        part.copy(miles = totalMiles - part.startMiles)
    }
    var showAddPartDialog by remember {mutableStateOf(false)}
    var showNotificationDialog by remember {mutableStateOf(false)}

    val wornParts = updatedParts.filter {it.miles >= it.endMiles}

    LaunchedEffect(key1 = wornParts){
        if(wornParts.isNotEmpty()){
            showNotificationDialog = true
        }
    }

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
},
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddPartDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Part")
            }
        }
    ){
        paddingValues ->
        if(updatedParts.isEmpty()){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(text = "Add your parts here!")
            }
        }
        else{
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(updatedParts) { part ->
                    BikePartItem(part = part, navController = navController)
                }
            }
        }
        if(showAddPartDialog){
            AddPartDialog(
                onDismiss = {showAddPartDialog = false},
                onPartAdded = {newPart ->
                    bikeParts.add(newPart)
                    partsRepository.saveParts(bikeParts)
                    showAddPartDialog = false
                }
            )
        }
    }
    if(showNotificationDialog){
        AlertDialog(
            onDismissRequest = {showNotificationDialog = false},
            title = {Text(text="Mile limit reached!")},
            text = {
                Column{
                    Text("These parts have reached their mile limit:")
                    wornParts.forEach{ part ->
                        Text("* ${part.name} (Limit: ${part.endMiles}, Current: ${part.miles})")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {showNotificationDialog = false}){
                    Text("Ok")
                }
            }
        )
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartDialog(
    onDismiss: () -> Unit,
    onPartAdded: (BikePart) -> Unit
){
    val context: Context = LocalContext.current
    val prefsManager = remember {SharedPreferencesManager(context)}

    var partName by remember {mutableStateOf("")}
    var partBrand by remember {mutableStateOf("")}
    var partModel by remember {mutableStateOf("")}
    var partEndMiles by remember {mutableStateOf("")}
    var partDateInstalled by remember {mutableStateOf("")}
    var partPrice by remember {mutableStateOf("")}
    var partNotes by remember {mutableStateOf("")}

    var showDatePicker by remember {mutableStateOf(false)}


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add new part")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = partName,
                    onValueChange = { partName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = partBrand,
                    onValueChange = { partBrand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = partModel,
                    onValueChange = { partModel = it },
                    label = { Text("Model") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = partEndMiles,
                    onValueChange = { partEndMiles = it },
                    label = { Text("Mile limit") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = partDateInstalled,
                    onValueChange = {partDateInstalled = it},
                    label = {Text("Date Installed (Click Icon)")},
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {showDatePicker = true}){
                            Icon(Icons.Default.DateRange, contentDescription = "Select date")
                        }
                    }
                )
                OutlinedTextField(
                    value = partPrice,
                    onValueChange = {partPrice = it},
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = partNotes,
                    onValueChange = {partNotes = it},
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newPart = BikePart(
                        id = Random.nextInt(),
                        name = partName,
                        brand = partBrand,
                        model = partModel,
                        miles = 0.0F,
                        startMiles = prefsManager.getMiles(),
                        endMiles = partEndMiles.toFloatOrNull() ?: 0.0F,
                        dateInstalled = partDateInstalled,
                        price = partPrice.toDoubleOrNull() ?: 0.0,
                        notes = partNotes
                    )
                    onPartAdded(newPart)
                }
            ){
                Text(text = "Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )

    if(showDatePicker){
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = remember {derivedStateOf {datePickerState.selectedDateMillis != null}}

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        val selectedDateMillis = datePickerState.selectedDateMillis
                        if(selectedDateMillis != null){
                            val calendar= Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            calendar.timeInMillis = selectedDateMillis

                            val localDate = LocalDate.of(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH) +1,
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                            val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US)
                            partDateInstalled = localDate.format(formatter)
                        }
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("Ok")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikePartItem(part: BikePart, navController: NavController) {
    val cardColor = when{
        part.miles >= part.endMiles -> Color(0xFFEF9A9A)
        part.miles >= part.endMiles * 0.8F -> Color(0xFFFFCC80)

        else -> Color(0xFFC8E6C9)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable{
                    navController.navigate("part_details/${part.id}")
                },
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(text = part.name, color = Color.Black)
            Text(text = "Miles: ${part.miles}", color = Color.Black)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartDetailsScreen(navController: NavController, partID: Int) {
    val context = LocalContext.current
    val partsRepository = remember { PartsRepository(context) }
    var prefsManager = remember {SharedPreferencesManager(context)}
    val totalMiles = prefsManager.getMiles()

    var part by remember{
        mutableStateOf(partsRepository.loadParts().find {it.id == partID}?.let{
            it.copy(miles = totalMiles - it.startMiles)
        })
    }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showResetConfirmation by remember {mutableStateOf(false)}


    fun deletePart(part: BikePart) {
        val currentParts = partsRepository.loadParts().toMutableList()
        currentParts.remove(part)
        partsRepository.saveParts(currentParts)
        navController.popBackStack()
    }

    fun resetPartMiles(){
        val currentPart = part ?: return
        val currentParts = partsRepository.loadParts().toMutableList()
        val index = currentParts.indexOfFirst {it.id == currentPart.id}
        if(index != -1){
            val updatedPart = currentParts[index].copy(
                startMiles = totalMiles,
                miles = 0.0F
            )
            currentParts[index] = updatedPart
            partsRepository.saveParts(currentParts)
            part = updatedPart.copy(miles = totalMiles - updatedPart.startMiles)
        }
    }

    val currentPart = part

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentPart?.name ?: "Part Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    currentPart?.let { safePart ->
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit part"
                            )
                        }
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete part"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        currentPart?.let { safePart ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Name: ${safePart.name}")
                Text(text = "Brand: ${safePart.brand}")
                Text(text = "Model: ${safePart.model}")
                Text(text = "Miles: ${safePart.miles}")
                Text(text = "Mile limit: ${safePart.endMiles}")
                Text(text = "Miles remaining: ${safePart.endMiles - safePart.miles}")
                Text(text = "Date Installed: ${safePart.dateInstalled}")
                Text(text = "Price: ${safePart.price}")
                Text(text = "Notes: ${safePart.notes}")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {showResetConfirmation = true},
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("Reset miles")
                }
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Part not found")
            }
        }

    }
    if (showDeleteConfirmation) {
        currentPart?.let { safePart ->
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmation = false
                },
                title = {
                    Text(text = "Confirm Part Deletion")
                },
                text = {
                    Text(text = "Are you sure you want to delete ${safePart?.name}? \nThis can not be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            deletePart(safePart)
                            showDeleteConfirmation = false
                        }
                    ) {
                        Text(text = "Confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (showEditDialog) {
        currentPart?.let { safePart ->
            EditPartDialog(
                part = safePart,
                onDismiss = { showEditDialog = false },
                onPartEdited = { editedPart ->
                    val currentParts = partsRepository.loadParts().toMutableList()
                    val index = currentParts.indexOfFirst { it.id == editedPart.id }
                    if (index != -1) {
                        currentParts[index] = editedPart
                    }
                    partsRepository.saveParts(currentParts)
                    showEditDialog = false
                    part = editedPart
                }
            )
        }
    }

    if(showResetConfirmation){
        currentPart?.let { safePart ->
            AlertDialog(
                onDismissRequest = {showResetConfirmation = false},
                title = {Text("Confirm Reset")},
                text = {Text("Are you sure you want to reset the miles for ${safePart.name}?")},
                confirmButton = {
                    TextButton(
                        onClick = {
                            resetPartMiles()
                            showResetConfirmation = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {showResetConfirmation = false}){
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPartDialog(
    part: BikePart,
    onDismiss: () -> Unit,
    onPartEdited: (BikePart) -> Unit
){

    var partName by remember {mutableStateOf(part.name)}
    var partBrand by remember {mutableStateOf(part.brand)}
    var partModel by remember {mutableStateOf(part.model)}
    var partEndMiles by remember {mutableStateOf(part.endMiles.toString())}
    var partDateInstalled by remember {mutableStateOf(part.dateInstalled)}
    var partPrice by remember {mutableStateOf(part.price.toString())}
    var partNotes by remember {mutableStateOf(part.notes)}

    var showDatePicker by remember {mutableStateOf(false)}

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text(text = "Edit ${part.name}")},
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)){
                OutlinedTextField(value = partName, onValueChange = {partName = it}, label = {Text("Name")}, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = partBrand, onValueChange = {partBrand = it}, label = {Text("Brand")}, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = partModel, onValueChange = {partModel = it}, label = {Text("Model")}, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = partEndMiles, onValueChange = {partEndMiles = it}, label = {Text("Mile limit")}, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = partDateInstalled,
                    onValueChange = {},
                    label = {Text("Date Installed")},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {showDatePicker = true}){
                            Icon(Icons.Default.DateRange, contentDescription = "Select date")
                        }
                    }
                )
                OutlinedTextField(value = partPrice, onValueChange = {partPrice = it}, label = {Text("Price")}, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = partNotes, onValueChange = {partNotes = it}, label = {Text("Notes")}, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val editedPart = part.copy(
                        name = partName,
                        brand = partBrand,
                        model = partModel,
                        endMiles = partEndMiles.toFloatOrNull() ?: 0.0F,
                        dateInstalled = partDateInstalled,
                        price = partPrice.toDoubleOrNull() ?: 0.0,
                        notes = partNotes
                    )
                    onPartEdited(editedPart)
                }
            ) {Text(text = "Save")}
        },
        dismissButton = {
            Button(onClick = onDismiss) {Text(text = "Cancel")}
        }
    )

    if(showDatePicker){
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = remember {derivedStateOf {datePickerState.selectedDateMillis != null}}

        DatePickerDialog(
            onDismissRequest = {showDatePicker = false},
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        val selectedDateMillis = datePickerState.selectedDateMillis
                        if(selectedDateMillis != null){
                            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            calendar.timeInMillis = selectedDateMillis
                            val localDate = LocalDate.of(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                            val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US)
                            partDateInstalled = localDate.format(formatter)
                        }
                    },
                    enabled = confirmEnabled.value
                ) {Text("Ok")}
            },
            dismissButton = {
                TextButton(onClick = {showDatePicker = false}) {Text("Cancel")}
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PartsScreenPreview(){
    val navController = rememberNavController()
    PartsScreen(navController = navController)
}