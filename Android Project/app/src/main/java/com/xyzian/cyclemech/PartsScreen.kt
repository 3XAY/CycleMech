package com.xyzian.cyclemech

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.TimeZone

//Data class just makes an object with the data that needs to be stored
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

    //Gets the total miles and uses that to update the individual parts' miles
    val totalMiles = prefsManager.getMiles()
    val updatedParts = bikeParts.map{ part ->
        part.copy(miles = totalMiles - part.startMiles)
    }

    //These variables control the visibility of dialogs (like adding a part or a worn out part warning)
    var showAddPartDialog by remember {mutableStateOf(false)}
    var showNotificationDialog by remember {mutableStateOf(false)}

    //Filters the parts list and saves the worn out ones so an alert can be shown
    val wornParts = updatedParts.filter {it.miles >= it.endMiles}

    //LaunchedEffect is something that runs when the screen is loaded and will re-ren if wornParts changes (that's what key1 does)
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
        //This is a button that floats above the screen's content, which makes it so you can have the screen filled with parts and still add parts
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

        //Checks if the parts list is empty, if so, display a message that tells the user to add their parts
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

            //If the list does have items, use LazyColumn (which only renders the items that are on screen) to display the parts
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                //items is a method that loops through the list and makes a composable for each part
                items(updatedParts) { part ->
                    BikePartItem(part = part, navController = navController)
                }
            }
        }

        //This runs when the add button is clicked, and it runs the method to display the actual dialog with the correct parameters
        if(showAddPartDialog){
            AddPartDialog(
                onDismiss = {showAddPartDialog = false},
                onPartAdded = {newPart ->

                    //After the new bike part is created and returned from the dialog, add it to the list, save the list, and close the dialog
                    bikeParts.add(newPart)
                    partsRepository.saveParts(bikeParts)
                    showAddPartDialog = false
                }
            )
        }
    }

    //Alerts users if a part is worn out
    if(showNotificationDialog){
        AlertDialog(
            onDismissRequest = {showNotificationDialog = false},
            title = {Text(text="Mile limit reached!")},
            text = {
                Column{
                    Text("These parts have reached their mile limit:")
                    wornParts.forEach{ part ->
                        Text("* ${part.name} (Limit: ${part.endMiles}, Current: ${part.miles})") //TODO: Replace the "*" with something else that is a dot that looks a bit better
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

//This is the method to actually create a new part (and the dialog)
@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartDialog(
    onDismiss: () -> Unit,
    onPartAdded: (BikePart) -> Unit
){
    val context: Context = LocalContext.current
    val prefsManager = remember {SharedPreferencesManager(context)}

    //Variables for the text fields in the dialog
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
                OutlinedTextField( //Creates a text field with an outline
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
                onClick = { //When the confirm button is clicked, create a new part with the information entered and the total miles variable
                    val newPart = BikePart(
                        id = Random.nextInt(), //Generate a random ID for the part
                        name = partName,
                        brand = partBrand,
                        model = partModel,
                        miles = 0.0F,
                        startMiles = prefsManager.getMiles(),//Sets the baseline to count the miles tracked
                        endMiles = partEndMiles.toFloatOrNull() ?: 0.0F,
                        dateInstalled = partDateInstalled,
                        price = partPrice.toDoubleOrNull() ?: 0.0,
                        notes = partNotes
                    )
                    onPartAdded(newPart) //Returns the newly created part to be added to the list and saved in storage
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

    //Date picker dialog enables the user to easily pick the installation date with a useful menu
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


//Defines how each part should be displayed
@Composable
fun BikePartItem(part: BikePart, navController: NavController) {
    val progress = if(part.endMiles > 0) part.miles / part.endMiles else 0f //Calculates the progress of the part's lifespan between 0 and 1
    val progressColor = when{
        progress >= 1F -> Color(0xFFFF4F30) //100% used = red
        progress >= 0.9F -> Color(0xFFFF98000) //90%-100% used = orange
        progress >= 0.8F -> Color(0xFFFFDD540) //80%-90% used = yellow
        else -> Color(0xFF8FFA61) //Below 80% used = green
    }

    //The card is a visual container where basic part details (name, miles, model, etc.) are displayed as well as the progress bar
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable{ //This modifier makes it so when you click on the card (the part), it takes you to the details screen
                    navController.navigate("part_details/${part.id}")
                },
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Column{
                    Text(text = part.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "${part.brand} ${part.model}", style = MaterialTheme.typography.bodySmall)
                }
                Text(text = "Miles: ${part.miles}", style = MaterialTheme.typography.bodyMedium)
            }
            Box( //This box is the container that stores / creates the progress bar
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50.dp))
            ) {
                Box( //This is the background or empty part of the progress bar
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
                )
                Box( //This is the foreground or filled part of the progress bar
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.coerceIn(0F, 1F)) //This changes the width of the progress bar or how much of it is filled
                        .background(progressColor)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartDetailsScreen(navController: NavController, partID: Int) { //This screen shows ALL of the information for the given part
    val context = LocalContext.current
    val partsRepository = remember { PartsRepository(context) }
    val prefsManager = remember {SharedPreferencesManager(context)}
    val totalMiles = prefsManager.getMiles()

    var part by remember{ //Loads the part in from storage and then ensures the mile counter is accurate by recalculating it
        mutableStateOf(partsRepository.loadParts().find {it.id == partID}?.let{
            it.copy(miles = totalMiles - it.startMiles)
        })
    }

    //Variables to control the visibility of their respective dialogs
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showResetConfirmation by remember {mutableStateOf(false)}


    //Gets the list of parts from storage, finds the active part, removes it from the list, and then updates the data in storage to reflect the removal of the part
    fun deletePart(part: BikePart) {
        val currentParts = partsRepository.loadParts().toMutableList()
        currentParts.remove(part)
        partsRepository.saveParts(currentParts)
        navController.popBackStack() //This will go back to the parts overview screen because you can't display details for a part that no longer exists
    }

    //Resets the part's miles back to 0
    //This involves changing the start miles to the current total miles of the bike and mile counter of the part back to 0
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


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(part?.name ?: "Part Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    part?.let { safePart ->
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
        part?.let { safePart -> //part?.let checks if the part actually exists and then runs the following code, which displays the details of the part
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
        } ?: run { //If the part IS somehow null, display a message telling the user that the part doesn't exist
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

    //Shows the delete confirmation dialog
    if (showDeleteConfirmation) {
        part?.let { safePart ->
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmation = false
                },
                title = {
                    Text(text = "Confirm Part Deletion")
                },
                text = {
                    Text(text = "Are you sure you want to delete ${safePart.name}? \nThis can not be undone.")
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

    //Shows the edit part dialog
    if (showEditDialog) {
        part?.let { safePart ->
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

    //Shows the reset confirmation dialog
    if(showResetConfirmation){
        part?.let { safePart ->
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
fun EditPartDialog( //Basically does the same thing as the add part dialog but applies to a specific part and already has the old information prefilled in the input field
    //This calculates the progress of the part's life as a float from 0F to 1F.
    //This calculates the progress of the part's life as a float from 0F to 1F.
    //This calculates the progress of the part's life as a float from 0F to 1F.
    //This calculates the progress of the part's life as a float from 0F to 1F.
    //This calculates the progress of the part's life as a float from 0F to 1F.
    //This calculates the progress of the part's life as a float from 0F to 1F.
    //This calculates the progress of the part's life as a float from 0F to 1F.
    //This calculates the progress of the part's life as a float from 0F to 1F.
    //
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