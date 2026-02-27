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
import androidx.compose.material3.VerticalDivider
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
    navigateToFruitDisplay: (Fruit) -> Unit,
    fruits: List<Fruit>
) {
    LazyColumn(
        modifier = Modifier.padding(8.dp)
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
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Text(text= "Id", modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold)
                    // Spacer that takes up all remaining space
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
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
                        navigateToFruitDisplay(fruit)
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
fun SearchFruitsByTreeUidList(
    treeUid: Int,
    changeMessage: (String) -> Unit,
    navigateToFruitDisplay: (Fruit) -> Unit,
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

    // this is key for recomposition. if the key of LaunchedEffect does
    // not change, then it will not execute its body again.


    LaunchedEffect(treeUid) {
        val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
        val preferences = preferencesFlow.first()
        token = preferences[TOKEN] ?: ""
        email = preferences[EMAIL] ?: ""
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    Log.d("CAMPICO", "TREE FOR FRUITS: $treeUid")
                    val result = networkService.getFruitsByTreeUid(
                        payload = GetFruitsByTreeUidRequest(
                            token = token,
                            email = email,
                            treeUid = treeUid
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


    //Column(
    //    horizontalAlignment = Alignment.CenterHorizontally,
    //    verticalArrangement = Arrangement.spacedBy(8.dp),
    //) {
        FruitList(
            fruits = fruits,
            navigateToFruitDisplay = navigateToFruitDisplay
        )
    //}
}