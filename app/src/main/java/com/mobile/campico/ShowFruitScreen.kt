package com.mobile.campico

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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

fun jsonArrayStringToFruit(jsonString: String): Fruit {
    val gson = Gson()
    // A common approach is to parse it as an Array and convert to a List
    val fruitsArray = gson.fromJson(jsonString, Array<Fruit>::class.java)
    Log.d("CAMPICO", fruitsArray.toString())
    return fruitsArray[0]
}

@Composable
fun ShowFruitScreen(
    uid: Int,
    changeMessage: (String) -> Unit,
    networkService: NetworkService
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext

    var code by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }

    var token: String by remember { mutableStateOf("") }
    var email: String by remember { mutableStateOf("") }

    var fruit: Fruit? by remember { mutableStateOf(Fruit(
        uid = 0, "",
        uidTree = 0
    )) }

    LaunchedEffect(Unit) {
        val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
        val preferences = preferencesFlow.first()
        token = preferences[TOKEN] ?: ""
        email = preferences[EMAIL] ?: ""
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = networkService.getFruitByUid(
                        payload = GetFruitByUidRequest(
                            token = token,
                            email = email,
                            uid = uid
                        )
                    )
                    code = result.code
                    message = result.message
                } catch (e: Exception) {
                    message = "There was an error in the request."
                    Log.d("CAMPICO", "Unexpected exception: $e")
                }
            }
            if (code == 200) {
                fruit = jsonArrayStringToFruit(message)
                fruit?.let {
                    Log.d("CAMPICO", "FRUIT TO DISPLAY" + it.uid)
                }

            } else {
                changeMessage(message)
            }
        }
    }
    if (fruit == null) {
        changeMessage("Tree not found")
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(
                modifier = Modifier.size(8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    readOnly = true,
                    value = fruit?.id.orEmpty(),
                    onValueChange = { },
                    modifier = Modifier
                        .weight(1f) // Takes up remaining space
                        .padding(end = 8.dp)
                        .semantics { contentDescription = "idField" },
                    label = { Text("id") }
                )
            }
        }
        //}


        /*
        Button(
            modifier = Modifier.semantics { contentDescription = "Edit" },
            onClick = {
                navigateToEditTree(tree)
            })
        {
            Text("Edit")
        }

         */
        /*
        Button(
            modifier = Modifier.semantics { contentDescription = "AddFruit" },
            onClick = {
                navigateToAddFruitByTree(tree)
            }
        )

        {
            Text("Add Fruit")
        }

         */
        /*
        Button(
            modifier = Modifier.semantics { contentDescription = "SearchFruits" },
            onClick = {
                navigateToSearchFruitsByTree(tree)
            }
        )

        {
            Text("Search Fruits")
        }

         */
    }
}
