package com.mobile.campico

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date


fun jsonArrayStringToVisit(jsonString: String): Visit {
    val gson = Gson()
    // A common approach is to parse it as an Array and convert to a List
    val visitsArray = gson.fromJson(jsonString, Array<Visit>::class.java)
    Log.d("CAMPICO", visitsArray.toString())
    return visitsArray.toList().first()
}

@Composable
fun ShowVisitScreen(
    uid: Int,
    changeMessage: (String) -> Unit,
    navigateBack: () -> Unit,
    networkService: NetworkService,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext

    var code by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }

    var token:String by remember {mutableStateOf("")}
    var email:String by remember {mutableStateOf("")}

    var visit: Visit? by remember { mutableStateOf(Visit(uid = 0, Date())) }

    LaunchedEffect(Unit) {
        val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
        val preferences = preferencesFlow.first()
        token = preferences[TOKEN] ?: ""
        email = preferences[EMAIL] ?: ""
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = networkService.getVisitByUid(
                        payload = GetVisitByUidRequest(
                            token = token,
                            email = email,
                            uid = uid
                        )
                    )
                    code = result.code
                    message = result.message


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
                visit = jsonArrayStringToVisit(message)
            } else {
                changeMessage(message)
            }

        }

        //tree = getTreeByUid(uid);
        //changeMessage("Please, select an option.")
    }
    if (visit == null) {
        changeMessage("Visit not found")
    } else {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),) {
            Spacer(
                modifier = Modifier.size(16.dp)
            )
            TextField(
                readOnly = true,
                value = convertDateToStringDate(visit?.date),
                onValueChange = { },
                modifier = Modifier.semantics { contentDescription = "dateField" },
                label = { Text("date") }
            )

            //Button(
            //    modifier = Modifier.semantics { contentDescription = "Edit" },
            //    onClick = {
                    //navigateToEditTree(tree)
            //    })
           // {
           //     Text("Edit")
           // }
           // Button(
           //     modifier = Modifier.semantics { contentDescription = "Delete" },
           //     onClick = {
           //         scope.launch {
           //             try {
           //                 deleteTree(
           //                     tree?.id.orEmpty())
           //                 navigateBack()
           //                 //changeMessage("The flash card has been deleted from  your database")
           //             } catch (e: SQLiteConstraintException) {
           //                 //changeMessage("Unexpected exception: $e")
           //             } catch (e: Exception) {
           //                 //changeMessage("Unexpected exception: $e")
           //             }
           //         }
           //     })
           // {
           //     Text("Delete")
           // }

        }
    }
}