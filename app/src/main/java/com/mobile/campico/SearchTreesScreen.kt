package com.mobile.campico

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NumberFruitsByTreeUidCell(uidTree: Int,
                           getTotalFruitsByTreeUid: suspend (Int) -> Int) {
    var cellValue by remember { mutableStateOf("") }

    // Use LaunchedEffect to run the suspend function safely
    LaunchedEffect(Unit) {
        // This runs in a coroutine
        cellValue = getTotalFruitsByTreeUid(uidTree).toString()
    }

    // The UI displays the current state (initially "Loading...", then the fetched data)
    Text(
        text = cellValue, modifier = Modifier.width(50.dp)
    )
}
@Composable
fun TreeList(
    navigateToTreeDisplay: (Tree) -> Unit,
    trees: List<Tree>,
    getTotalFruitsByTreeUid: suspend (Int) -> Int
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp) )
                {
                    Text(text= "Id", modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold)
                    VerticalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp), // Add horizontal padding for spacing
                        thickness = 1.dp,
                        color = Color.DarkGray // Customize the color
                    )
                    Text(text = "# Fruits", modifier = Modifier.width(70.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
        items(
            items = trees,
            key = { tree -> tree.uid }
        ) { tree ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Color.LightGray)
                    .padding(6.dp)
                    .height(IntrinsicSize.Min) // Key modifier for vertical divider height
                    .clickable(onClick = {
                        navigateToTreeDisplay(tree)
                    }
                    )
            ) {
                Row(modifier =
                    Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp) )
                {
                    Text(text= tree.id, modifier = Modifier.width(50.dp))
                    VerticalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp), // Add horizontal padding for spacing
                        thickness = 1.dp,
                        color = Color.DarkGray // Customize the color
                    )
                    NumberFruitsByTreeUidCell(tree.uid, getTotalFruitsByTreeUid)
                }
            }
        }
    }
}
@Composable
fun SearchTreesScreen(
    changeMessage: (String) -> Unit,
    getTrees: suspend () -> List<Tree>,
    getTotalFruitsByTreeUid: suspend (Int) -> Int,
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
            navigateToTreeDisplay = navigateToTreeDisplay,
            getTotalFruitsByTreeUid = getTotalFruitsByTreeUid
        )
    }
}