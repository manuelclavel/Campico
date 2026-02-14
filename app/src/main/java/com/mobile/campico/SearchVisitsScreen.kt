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
fun VisitList(
    navigateToVisitDisplay: (Visit) -> Unit,
    visits: List<Visit>
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
                    Text(text= "Date", modifier = Modifier.width(150.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
        items(
            items = visits,
            key = { tree -> tree.uid }
        ) { tree ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Color.LightGray)
                    .padding(6.dp)
                    .height(IntrinsicSize.Min) // Key modifier for vertical divider height
                    .clickable(onClick = {
                        navigateToVisitDisplay(tree)
                    }
                    )
            ) {
                Row(modifier =
                    Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp) )
                {
                    Text(text= convertDateToStringDate(tree.date),
                        modifier = Modifier.width(150.dp))
                }
            }
        }
    }
}
@Composable
fun SearchVisitsScreen(
    changeMessage: (String) -> Unit,
    getVisits: suspend () -> List<Visit>,
    navigateToVisitDisplay: (Visit) -> Unit
) {

    var visits by remember { mutableStateOf(emptyList<Visit>()) }

    LaunchedEffect(Unit) {
        //changeMessage("Please, select a flash card.")
        visits = getVisits()
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(
            modifier = Modifier.size(16.dp)
        )
        VisitList(
            visits = visits,
            navigateToVisitDisplay = navigateToVisitDisplay
        )
    }
}