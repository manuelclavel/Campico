package com.mobile.campico


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(
    changeMessage: (String) -> Unit,
    navigateToSearchVisits: () -> Unit,
    navigateToSearchTrees: () -> Unit,
    navigateToAddTree: () -> Unit,
    navigateToLogin: () -> Unit,
    networkService: NetworkService,
    navigateToQRCodeScanner: () -> Unit,
    //navigateToUploadPhoto: () -> Unit
) {

    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {

        //Then, use the DataStore.data property to expose the appropriate stored value using a Flow.

        //In coroutines, a flow is a type that can emit multiple values sequentially,
        //as opposed to suspend functions that return only a single value.
        //For example, you can use a flow to receive live updates from a database.

        //Flows are built on top of coroutines and can provide multiple values.
        //A flow is conceptually a stream of data that can be computed asynchronously.
        //The emitted values must be of the same type. For example, a Flow<Int>
        //is a flow that emits integer values.

        //In Kotlin with Jetpack DataStore, the Flow<Preferences> returned by dataStore.data
        // emits every time any single preference within the DataStore file changes.
        //The flow emits the entire Preferences object, containing all current key-value pairs, with each change.

        //In Kotlin Flow, the first() terminal operator is used to collect only the initial value emitted
        //by a flow and then automatically cancel the flow's execution.
        //This is particularly useful in Jetpack Compose and other Android development scenarios
        //where you only need a single, immediate result from a potentially long-running data stream.

        val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
        val preferences = preferencesFlow.first()
        changeMessage(preferences[EMAIL] ?: "")

    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // begin
        Spacer(
            modifier = Modifier.size(8.dp)
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "navigateToSearchVisits" },
            onClick = {
                navigateToSearchVisits()
            })
        {
            Text(
                "Visits",
                modifier = Modifier
                    .semantics { contentDescription = "SearchVisits" },
            )
        }

        /*
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "UploadPhoto" },
            onClick = {
               navigateToUploadPhoto()
            }) {
            Text(
                "Upload Photo",
                modifier = Modifier.semantics { contentDescription = "UploadPhotoButton" }
            )
        }
*/

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "navigateToSearchTrees" },
            onClick = {
                navigateToSearchTrees()
            })
        {
            Text(
                "Trees",
                modifier = Modifier
                    .semantics { contentDescription = "SearchTrees" },
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "navigateToAddTree" },
            onClick = {
                navigateToAddTree()
            }) {
            Text("Add Tree", modifier = Modifier.semantics { contentDescription = "AddTree" })
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "navigateToQRCodeScanner" },
            onClick = {
                navigateToQRCodeScanner()
            }) {
            Text("Scan QRCode", modifier = Modifier.semantics { contentDescription = "ScanQRCode" })
        }


        Button(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "backingUpDb" }, onClick = {

                exportRoomDatabase(
                    appContext, scope, "CampicoDatabase",
                    networkService = networkService,
                    changeMessage = changeMessage
                )

            }) {
            Text(
                "Backup data",
                modifier = Modifier.semantics { contentDescription = "BackUpDb" }
            )
        }


    }
}

