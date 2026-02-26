package com.mobile.campico


import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun convertDateToStringDate(date: Date?): String {
    // 1. Convert the java.util.Date to an Instant (a point on the time-line in UTC)
    val instant = date?.toInstant()
    // 2. Apply a time zone to the Instant to get a ZonedDateTime
    // Using the systemDefault() zone is common, but you could use any specific ZoneId
    val zonedDateTime = instant?.atZone(ZoneId.systemDefault())

    // 3. Extract the LocalDate from the ZonedDateTime
    val localDate = zonedDateTime?.toLocalDate()
    // Define the desired format pattern
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    // Format the date object into a string
    return localDate?.format(formatter).orEmpty()
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun convertDateStringToDate(dateString: String): Date {
    // define a DateTimeFormatter object that matches the pattern and pass it to the parse() method.
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val localDate = LocalDate.parse(dateString, formatter)
    val localDateTime = localDate.atStartOfDay()
    // Associate with the system's default time zone
    val zonedDateTime = localDateTime.atZone(ZoneId.systemDefault())
    // Convert to an Instant
    val instant = zonedDateTime.toInstant()
    // Convert Instant to java.util.Date
    return Date.from(instant)
}

@Composable
fun AddVisitScreen(
    changeMessage: (String) -> Unit,
    navigateBack : () -> Unit,
    networkService: NetworkService
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext

    var code by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }


    var token: String by remember { mutableStateOf("") }
    var email: String by remember { mutableStateOf("") }


    var selectedDateText by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(Unit) {
        val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
        val preferences = preferencesFlow.first()
        token = preferences[TOKEN] ?: ""
        email = preferences[EMAIL] ?: ""
        changeMessage("Please, add a visit.")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(
            modifier = Modifier.size(8.dp)
        )
        OutlinedTextField(
            value = selectedDateText,
            onValueChange = { /* Prevent direct editing */ },
            readOnly = true, // Prevents keyboard input
            label = { Text("Select Date") },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Add" },
            onClick = {

                scope.launch {
                    //The withContext function is your primary tool for seamlessly moving between Dispatchers.IO,
                    //Dispatchers.Default,
                    //and Dispatchers.Main within a single coroutine, ensuring background tasks don't freeze the UI.

                    //Start on Main (Implicitly): Composable functions generally run on the Main thread.
                    //Switch to IO: Use withContext(Dispatchers.IO) { ... } to perform heavy lifting (database, network)
                    // without blocking the UI.
                    //Switch back to Main: After the withContext(Dispatchers.IO) block finishes,
                    // the coroutine automatically resumes on the original Main dispatcher
                    // where you can update your UI state and trigger recomposition.

                    withContext(Dispatchers.IO) {
                        try {
                            val result = networkService.addVisit(
                                payload = AddVisitRequest(
                                    token = token,
                                    email = email,
                                    date = selectedDateText
                                )
                            )
                            code = result.code
                            message = result.message
                            changeMessage(message)
                            Log.d("CAMPICO", message)

                            //Prefer ApplicationContext: When you need a Context for operations that do not interact with the UI
                            //(e.g., file operations, database access, accessing resources like strings or drawables),
                            // use the application context.
                            //The application context lives for the lifetime of your app and is safe to use on any thread.
                        } catch (e: Exception) {
                            message = "There was an error in the request."
                            Log.d("CAMPICO", "Unexpected exception: $e")
                        }
                    }
                    if (code == 200) {
                        // edit the preferences and save email
                        changeMessage(message)
                        navigateBack()
                    } else {
                        changeMessage(message)
                    }

                }
            })
        {
            Text("Add")
        }
        // begin
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Convert the selected milliseconds to a readable date format
                            val selectedDateMillis = datePickerState.selectedDateMillis
                            if (selectedDateMillis != null) {
                                selectedDateText =
                                    convertMillisToDate(selectedDateMillis) // Helper function needed
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePicker = false }
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        // end

    }

}