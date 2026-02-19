package com.mobile.campico

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun jsonArrayStringToVisitList(jsonString: String): List<Visit> {
    val gson = Gson()
    // A common approach is to parse it as an Array and convert to a List
    val visitsArray = gson.fromJson(jsonString, Array<Visit>::class.java)
    return visitsArray.toList()

    // Another method using TypeToken for more complex types, useful in generic functions
    // val listType = object : TypeToken<List<Person>>() {}.type
    // return gson.fromJson(jsonString, listType)
}
@Composable
fun VisitList(
    navigateToVisitDisplay: (Visit) -> Unit,
    visits: List<Visit>
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(6.dp)
                    .height(IntrinsicSize.Min) // Key modifier for vertical divider height

            ) {
                Row(modifier =
                    Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp) )
                {
                    Text(text= "Date", modifier = Modifier.width(150.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
        items(
            items = visits,
            key = { visit -> visit.uid }
        ) { visit ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Color.LightGray)
                    .padding(6.dp)
                    .height(IntrinsicSize.Min) // Key modifier for vertical divider height
                    .clickable(onClick = {
                        navigateToVisitDisplay(visit)
                    }
                    )
            ) {
                Row(modifier =
                    Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp) )
                {
                    Text(text= convertDateToStringDate(visit.date),
                        modifier = Modifier.width(150.dp))
                }
            }
        }
    }
}
@Composable
fun SearchVisitsScreen(
    changeMessage: (String) -> Unit,
    navigateToVisitDisplay: (Visit) -> Unit,
    networkService: NetworkService
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext

    var code by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }

    var token:String by remember {mutableStateOf("")}
    var email:String by remember {mutableStateOf("")}

    var visits by remember { mutableStateOf(emptyList<Visit>()) }

    LaunchedEffect(Unit) {
        val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
        val preferences = preferencesFlow.first()
        token = preferences[TOKEN] ?: ""
        email = preferences[EMAIL] ?: ""
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
                    val result = networkService.getVisits(
                        payload = GetVisitsRequest(
                            token = token,
                            email = email
                        )
                    )
                    code = result.code
                    message = result.message
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
                visits = jsonArrayStringToVisitList(message)
            } else {
                changeMessage(message)
            }

        }
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(
            modifier = Modifier.size(16.dp)
        )
        VisitList(
            visits = visits,
            navigateToVisitDisplay = navigateToVisitDisplay
        )
    }
}