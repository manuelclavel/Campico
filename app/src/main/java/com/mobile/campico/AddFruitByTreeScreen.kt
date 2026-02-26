package com.mobile.campico

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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

@Composable
fun AddFruitByTreeScreen(
    treeUid: Int,
    changeMessage: (String) -> Unit,
    networkService: NetworkService,
    navigateBack: () -> Unit
) {
    var id by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext

    var code by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }


    var token: String by remember { mutableStateOf("") }
    var email: String by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
        val preferences = preferencesFlow.first()
        token = preferences[TOKEN] ?: ""
        email = preferences[EMAIL] ?: ""
        changeMessage("Please, add a fruit.")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // begin
        Spacer(
            modifier = Modifier.size(8.dp)
        )

        TextField(
            value = id,
            onValueChange = { id = it },
            modifier = Modifier.semantics { contentDescription = "idTextField" },
            label = { Text("id") }
        )
        Button(
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "AddFruit" },
            onClick = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            Log.d("CAMPICO", "request with " + treeUid + " " + id)
                            val result = networkService.addFruit(
                                payload = AddFruitRequest(
                                    token = token,
                                    email = email,
                                    id = id,
                                    treeUid = treeUid,
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
    }

}