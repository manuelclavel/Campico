package com.mobile.campico

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AddTreeScreen(
    changeMessage: (String) -> Unit,
    insertTree: suspend (Tree) -> Unit
) {
    var id by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    //val context = LocalContext.current

    LaunchedEffect(Unit) {
        changeMessage("Please, add a flash card.")
    }

    Column() {
        Spacer(
            modifier = Modifier.size(16.dp)
        )
        TextField(
            value = id,
            onValueChange = { id = it },
            modifier = Modifier.semantics { contentDescription = "enTextField" },
            label = { Text("id") }
        )
        Button(
            modifier = Modifier.semantics { contentDescription = "Add" },
            onClick = {
                scope.launch {
                    try {
                        insertTree(
                            Tree(
                                uid = 0,
                                id = id
                            )
                        )
                        id = ""
                        //changeMessage(context.getString(R.string.add_successful))
                    } catch (e: SQLiteConstraintException) {
                        //changeMessage(context.getString(R.string.add_unsuccessful))
                    } catch (e: Exception) {
                        changeMessage("Unexpected exception")
                    }
                }
            })
        {
            Text("Add")
        }
    }

}