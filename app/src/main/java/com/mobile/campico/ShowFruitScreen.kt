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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch




@Composable
fun ShowFruitScreen(
    uid: Int,
    getFruitByUid: suspend (Int) -> Fruit?,
    deleteFruit: suspend (String) -> Unit,
    changeMessage: (String) -> Unit,
    navigateBack: () -> Unit,
    navigateToEditFruit: (Fruit?) -> Unit
) {
    val scope = rememberCoroutineScope()
    var fruit: Fruit? by remember { mutableStateOf(Fruit(
        uid = 0, "",
        uidTree = 0
    )) }

    LaunchedEffect(Unit) {
        fruit = getFruitByUid(uid);
        //changeMessage("Please, select an option.")
    }
    if (fruit == null) {
        //changeMessage("Flash card not found")
    } else {

        Column() {
            Spacer(
                modifier = Modifier.size(16.dp)
            )
            TextField(
                readOnly = true,
                value = fruit?.id.orEmpty(),
                onValueChange = { },
                modifier = Modifier.semantics { contentDescription = "idField" },
                label = { Text("id") }
            )
            //TextField(
            //    readOnly = true,
            //    value = fruit?.uidTree.toString(),
            //    onValueChange = { },
            //    modifier = Modifier.semantics { contentDescription = "treeField" },
            //    label = { Text("tree") }
            //)

            Button(
                modifier = Modifier.semantics { contentDescription = "Edit" },
                onClick = {
                    navigateToEditFruit(fruit)
                })
            {
                Text("Edit")
            }
            Button(
                modifier = Modifier.semantics { contentDescription = "Delete" },
                onClick = {
                    scope.launch {
                        try {
                            deleteFruit(
                                fruit?.id.orEmpty()
                            )
                            navigateBack()
                            //changeMessage("The flash card has been deleted from  your database")
                        } catch (e: SQLiteConstraintException) {
                            //changeMessage("Unexpected exception: $e")
                        } catch (e: Exception) {
                            //changeMessage("Unexpected exception: $e")
                        }
                    }
                })
            {
                Text("Delete")
            }
        }
    }
}