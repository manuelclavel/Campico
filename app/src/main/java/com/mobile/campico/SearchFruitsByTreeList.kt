package com.mobile.campico


import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun jsonArrayStringToFruitList(jsonString: String): List<Fruit> {
    val gson = Gson()
    // A common approach is to parse it as an Array and convert to a List
    val fruitsArray = gson.fromJson(jsonString, Array<Fruit>::class.java)
    return fruitsArray.toList()

    // Another method using TypeToken for more complex types, useful in generic functions
    // val listType = object : TypeToken<List<Person>>() {}.type
    // return gson.fromJson(jsonString, listType)
}
@Composable
fun FruitList(
    //navigateToFruitDisplay: (Fruit) -> Unit,
    fruits: List<Fruit>
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        items(
            items = fruits,
            key = { fruit -> fruit.uid}
        ) { fruit ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Color.LightGray)
                    .padding(6.dp)
                    .clickable(onClick = {
                        //navigateToFruitDisplay(fruit)
                    }
                    )
            ) {
                Column(modifier = Modifier.padding(6.dp))
                { Text(fruit.id) }

            }
        }
    }
}
@Composable
fun SearchFruitsByTreeList(
    tree: Tree,
    changeMessage: (String) -> Unit,
    //navigateToFruitDisplay: (Fruit) -> Unit,
    networkService: NetworkService
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext

    var code by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }

    var token:String by remember {mutableStateOf("")}
    var email:String by remember {mutableStateOf("")}

    var fruits by remember { mutableStateOf(emptyList<Fruit>()) }

    LaunchedEffect(tree.uid) {
        val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
        val preferences = preferencesFlow.first()
        token = preferences[TOKEN] ?: ""
        email = preferences[EMAIL] ?: ""
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    Log.d("CAMPICO", "TREE FOR FRUITS" + tree.id)
                    val result = networkService.getFruitsByTreeUid(
                        payload = GetFruitsByTreeUidRequest(
                            token = token,
                            email = email,
                            treeUid = tree.uid
                        )
                    )
                    code = result.code
                    message = result.message
                    Log.d("CAMPICO", message)
                } catch (e: Exception) {
                    message = "There was an error in the request."
                    Log.d("CAMPICO", "Unexpected exception: $e")
                }
            }
            if (code == 200) {
                // edit the preferences and save email
                Log.d("CAMPICO", "Fruits" + message)
                fruits = jsonArrayStringToFruitList(message)
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
        FruitList(
            fruits = fruits,
            //navigateToFruitDisplay = navigateToFruitDisplay
        )
    }
}