package com.mobile.campico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@Composable
fun ProfileScreen(
    changeMessage: (String) -> Unit,
    navigateToLogin: () -> Unit,
) {

    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(
            modifier = Modifier.size(8.dp)
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "navigateToLogin" }, onClick = {
                navigateToLogin()
            }) {
            Text(
                "Log in",
                modifier = Modifier.semantics { contentDescription = "Login" }
            )
        }
        /*
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "ExecuteLogout" }, onClick = {

                scope.launch {
                    appContext.dataStore.edit { preferences ->
                        preferences.remove(EMAIL)
                        preferences.remove(TOKEN)
                        changeMessage(preferences[EMAIL] ?: "")
                    }
                }

            }) {
            Text(
                "Log out",
                modifier = Modifier.semantics { contentDescription = "Logout" }
            )
        }
        
         */
    }
}