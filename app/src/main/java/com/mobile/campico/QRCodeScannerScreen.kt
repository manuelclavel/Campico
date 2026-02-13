package com.mobile.campico

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.shouldShowRationale
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.Manifest
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi

fun successTreeScan(text: String): Boolean {
    val prefix = "tree:"
    var check = false
    if (text.startsWith(prefix)) {
        check = true
    }
    return check
}

fun successFruitScan(text: String): Boolean {
    val prefix = "fruit:"
    var check = false
    if (text.startsWith(prefix)) {
        check = true
    }
    return check
}

fun prettyPrintTreeScan(text: String): String {
    val prefix = "tree:"
    var prettyPrint = ""
    if (text.startsWith(prefix)) {
        prettyPrint = text.substringAfter(prefix)
    }
    return prettyPrint
}

fun prettyPrintFruitScan(text: String): String {
    val prefix = "fruit:"
    var prettyPrint = ""
    if (text.startsWith(prefix)) {
        prettyPrint = text.substringAfter(prefix)
    }
    return prettyPrint
}

@kotlin.OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRCodeScannerScreen(
    navigateToTreeDisplayById: (String) -> Unit,
    navigateToFruitDisplayById: (String) -> Unit
) {
    // State to hold the scanned barcode value, saved across recompositions
    var barcode by remember { mutableStateOf<String?>("No Code Scanned") }
    //var permissionGranted by remember { mutableStateOf<Boolean>(false) }
    // State to manage the camera permission
    val permissionState = rememberPermissionState(
        Manifest.permission.CAMERA // Permission being requested
    )

    // State to track whether to show the rationale dialog for the permission
    var oncancel by remember(permissionState.status.shouldShowRationale) {
        mutableStateOf(permissionState.status.shouldShowRationale)
    }


    // Check if a barcode has been scanned
    val myPermissionState = Manifest.permission.CAMERA
    if (permissionState.status.isGranted) {
        ScanCode(onQrCodeDetected = {
            // barcode = it // Update the barcode state with the scanned value
            if (successTreeScan(it)) {
                navigateToTreeDisplayById(prettyPrintTreeScan(it))
                //barcode = "No Code Scanned"
            } else if (successFruitScan(it)){
                navigateToFruitDisplayById(prettyPrintFruitScan(it))
            }
            else {
                barcode = it
            }
        })
    } else {
        if (barcode != null) {
            // Box to hold the UI elements and center them on the screen
            Box(
                modifier = Modifier
                    .fillMaxSize() // Make the Box take up the entire screen
                    .background(Color.LightGray) // Optional: Add a background color for visibility
            ) {
                // Column to arrange UI elements vertically and center them
                Column(
                    modifier = Modifier
                        .align(Alignment.Center) // Center the Column within the Box
                        .padding(16.dp), // Optional: Add padding
                    horizontalAlignment = Alignment.CenterHorizontally, // Center children horizontally
                    verticalArrangement = Arrangement.Center // Center children vertically
                ) {
                    //if (!permissionGranted) {
                    // Show rationale dialog if permission is denied and rationale can be shown
                    if (oncancel) {
                        ShowRationaleDialog(
                            onDismiss = { oncancel = false }, // Callback when dialog is dismissed
                            onConfirm = { permissionState.launchPermissionRequest() }, // Callback to request permission
                            body = permissionState.permission // Permission being requested
                        )
                    }

                    // Determine the text to show based on the permission state
                    val textToShow = if (permissionState.status.shouldShowRationale) {
                        // If the user has denied the permission but the rationale can be shown,
                        // explain why the app requires this permission
                        "The Camera permission is important for this app. Please grant the permission."
                    } else if (!permissionState.status.isGranted) {
                        // If it's the first time the user lands on this feature, or the user
                        // doesn't want to be asked again for this permission, explain that the
                        // permission is required
                        "Camera permission required for this feature to be available. " +
                                "Please grant the permission"
                    } else {
                        // If permission is granted, show the scanned barcode or a default message
                        //changePermission(true)
                        //Log.d("", permissionGranted)

                        barcode ?: "No Scanned"
                    }
                    // Display the determined text
                    Text(textToShow)

                    // Show a button to scan a QR or barcode if permission is granted
                    if (permissionState.status.isGranted) {
                        //Button(onClick = { barcode = null }) {
                        //    Text("Scan QR or Barcode")
                        //}
                        Log.d("CAMPICO", "Unexpected state")
                    } else {
                        // Show a button to request camera permission if not granted
                        Button(onClick = { permissionState.launchPermissionRequest() }) {
                            Text("Request permission")
                        }
                    }
                }
            }
        } else {
            // If no barcode has been scanned, show the QR/barcode scanner
            ScanCode(onQrCodeDetected = {
                // barcode = it // Update the barcode state with the scanned value
                if (successTreeScan(it)) {
                    navigateToTreeDisplayById(prettyPrintTreeScan(it))
                } else if (successFruitScan(it)){
                    navigateToFruitDisplayById(prettyPrintFruitScan(it))
                }
                else {
                    barcode = it
                }
            })
        }
    }
}