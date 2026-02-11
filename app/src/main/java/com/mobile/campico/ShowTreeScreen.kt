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
fun ShowTreeScreen(
    uid: Int,
    getTreeByUid: suspend (Int) -> Tree?,
    deleteTree: suspend (String) -> Unit,
    changeMessage: (String) -> Unit,
    navigateBack: () -> Unit,
    navigateToEditTree: (Tree?) -> Unit,
    navigateToSearchFruitsByTree: (Tree?) -> Unit
) {
    val scope = rememberCoroutineScope()
    var tree: Tree? by remember { mutableStateOf(Tree(uid = 0, "")) }

    LaunchedEffect(Unit) {
        tree = getTreeByUid(uid);
        //changeMessage("Please, select an option.")
    }
    if (tree == null) {
        //changeMessage("Flash card not found")
    } else {

        Column() {
            Spacer(
                modifier = Modifier.size(16.dp)
            )
            TextField(
                readOnly = true,
                value = tree?.id.orEmpty(),
                onValueChange = { },
                modifier = Modifier.semantics { contentDescription = "idField" },
                label = { Text("id") }
            )

            Button(
                modifier = Modifier.semantics { contentDescription = "Edit" },
                onClick = {
                    navigateToEditTree(tree)
                })
            {
                Text("Edit")
            }
            Button(
                modifier = Modifier.semantics { contentDescription = "Delete" },
                onClick = {
                    scope.launch {
                        try {
                            deleteTree(
                                tree?.id.orEmpty())
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
            Button(
                modifier = Modifier.semantics { contentDescription = "SearchFruits" },
                onClick = {
                    navigateToSearchFruitsByTree(tree)
                }
            )

            {
                Text("Search Fruits")
            }
        }
    }
}