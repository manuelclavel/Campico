package com.mobile.campico


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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp





@Composable
fun HomeScreen(
    changeMessage: (String) -> Unit,
    navigateToSearchTrees: () -> Unit,
    navigateToAddTree: () -> Unit
) {


    LaunchedEffect(Unit) {
        //changeMessage("Please, select a flash card.")
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(
            modifier = Modifier.size(16.dp)
        )
        Button(
            modifier = Modifier.semantics { contentDescription = "navigateToSearchTrees" },
            onClick = {
                navigateToSearchTrees()
            })
        {
            Text(
                "Search Trees",
                modifier = Modifier.semantics { contentDescription = "SearchTrees" },
            )
        }
        Button(
            modifier = Modifier.semantics { contentDescription = "navigateToAddTree" },
            onClick = {
                navigateToAddTree()
            }) {
            Text("Add Tree", modifier = Modifier.semantics { contentDescription = "AddTree" },)
        }
    }
    }
