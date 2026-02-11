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
import androidx.compose.ui.unit.dp

@Composable
fun TreeList(
    navigateToTreeDisplay: (Tree) -> Unit,
    trees: List<Tree>
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        items(
            items = trees,
            key = { tree -> tree.uid}
        ) { tree ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Color.LightGray)
                    .padding(6.dp)
                    .clickable(onClick = {
                        navigateToTreeDisplay(tree)
                    }
                    )
            ) {
                Column(modifier = Modifier.padding(6.dp))
                { Text(tree.id) }

            }
        }
    }
}
@Composable
fun SearchTreesScreen(
    changeMessage: (String) -> Unit,
    getTrees: suspend () -> List<Tree>,
    navigateToTreeDisplay: (Tree) -> Unit
) {

    var trees by remember { mutableStateOf(emptyList<Tree>()) }

    LaunchedEffect(Unit) {
        //changeMessage("Please, select a flash card.")
        trees = getTrees()
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(
            modifier = Modifier.size(16.dp)
        )
        TreeList(
            trees = trees,
            navigateToTreeDisplay = navigateToTreeDisplay
        )
    }
}