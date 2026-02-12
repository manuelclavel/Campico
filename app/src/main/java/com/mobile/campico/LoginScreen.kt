package com.mobile.campico

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    changeMessage: (String) -> Unit,
    networkService: NetworkService,
    navigateToToken: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    //val context = LocalContext.current
    //val appContext = context.applicationContext
    var email by remember { mutableStateOf("") }
    var code by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        changeMessage("Please, introduce your email.")
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "emailTextField" },
            label = { Text("email") }
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Enter" },
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
                            val result = networkService.generateToken(
                                email = UserCredential(email)
                            )
                            code = result.code
                            message = result.message

                            //token = result.token

                            //Prefer ApplicationContext: When you need a Context for operations that do not interact with the UI
                            //(e.g., file operations, database access, accessing resources like strings or drawables),
                            // use the application context.
                            //The application context lives for the lifetime of your app and is safe to use on any thread.

                            //Log.d("FLASHCARD", result.toString())
                        } catch (e: Exception) {
                            message = "There was an error in the token request."
                            Log.d("FLASHCARD", "Unexpected exception: $e")
                        }
                    }
                    if (code == 200) {
                        // edit the preferences and save email
                        navigateToToken(email)
                    } else {
                        changeMessage(message)
                    }

                }
            })
        {
            Text("Enter")
        }
    }
}