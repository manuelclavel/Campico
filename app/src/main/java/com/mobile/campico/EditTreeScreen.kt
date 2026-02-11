package com.mobile.campico

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
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
fun EditTreeScreen(
    uid: Int,
    getTreeByUid: suspend (Int) -> Tree?,
    changeMessage: (String) -> Unit,
    updateTree: suspend (String, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var tree: Tree? by remember { mutableStateOf(Tree(uid = 0, "")) }
    var idTree by rememberSaveable() { mutableStateOf("") }

    LaunchedEffect(Unit) {
        tree = getTreeByUid(uid);
        tree?.let { idTree = it.id.orEmpty() }
    }
    if (tree != null){
        //changeMessage("Please, edit the flashcard.")
        Column() {
            Spacer(
                modifier = Modifier.size(16.dp)
            )
            TextField(
                value = idTree,
                onValueChange = { idTree = it },
                modifier = Modifier.semantics { contentDescription = "idField" },
                label = { Text("id") }
            )

            Button(
                modifier = Modifier.semantics { contentDescription = "Save" },
                onClick = {
                    scope.launch {
                        try {
                            updateTree(
                                tree?.id.orEmpty(),
                                idTree
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