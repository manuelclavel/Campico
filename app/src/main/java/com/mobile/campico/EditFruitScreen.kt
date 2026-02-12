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
import androidx.compose.runtime.remember
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
fun EditFruitScreen(
    uid: Int,
    getFruitByUid: suspend (Int) -> Fruit?,
    changeMessage: (String) -> Unit,
    updateFruit: suspend (String, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var fruit: Fruit? by remember { mutableStateOf(Fruit(
        uid = 0, "",
        uidTree = 0
    )) }
    var idFruit by rememberSaveable() { mutableStateOf("") }

    LaunchedEffect(Unit) {
        fruit = getFruitByUid(uid);
        fruit?.let { idFruit = it.id.orEmpty() }
    }
    if (fruit != null){
        //changeMessage("Please, edit the flashcard.")
        Column() {
            Spacer(
                modifier = Modifier.size(16.dp)
            )
            TextField(
                value = fruit?.uidTree.toString(),
                onValueChange = { idFruit = it },
                modifier = Modifier.semantics { contentDescription = "treeField" },
                label = { Text("tree") },
                readOnly = true
            )
            TextField(
                value = idFruit,
                onValueChange = { idFruit = it },
                modifier = Modifier.semantics { contentDescription = "idField" },
                label = { Text("id") }
            )

            Button(
                modifier = Modifier.semantics { contentDescription = "Save" },
                onClick = {
                    scope.launch {
                        try {
                            updateFruit(
                                fruit?.id.orEmpty(),
                                idFruit
                            )
                            //changeMessage("The flash card has been successfully updated in your database")
                        } catch (e: SQLiteConstraintException) {
                            //changeMessage(context.getString(R.string.add_unsuccessful))
                        } catch (e: Exception) {
                            //changeMessage("Unexpected exception: $e")
                        }
                    }
                })
            {
                Text("Update")
            }
        }
    } else {
        //changeMessage("Flash card not found!!")
    }
}