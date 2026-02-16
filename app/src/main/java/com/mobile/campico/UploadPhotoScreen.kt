package com.mobile.campico

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.io.encoding.Base64


@Composable
fun UploadPhotoScreen(networkService: NetworkService, changeMessage: (String) -> Unit) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext

    var token:String by remember {mutableStateOf("")}
    var email:String by remember {mutableStateOf("")}


    LaunchedEffect(Unit) {
        val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
        val preferences = preferencesFlow.first()
        token = preferences[TOKEN] ?: ""
        email = preferences[EMAIL] ?: ""

    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Button to launch the photo picker
        Button(onClick = {
            // Launch the picker for a single image
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) {
            Text(text = "Pick Image from Gallery")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the selected image
        imageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            )
        }
        // Upload the selected image
        //changeMessage("Backing up flashcard database in S3...")

        Button(onClick= {
            scope.launch {
                withContext(Dispatchers.IO) {
                    //val byteArrayOutputStream = ByteArrayOutputStream()
                    changeMessage("Uploading object in S3...")
                    imageUri?.let { uri ->
                        val byteArray: ByteArray
                        // Use the 'use' extension function to ensure
                        // the InputStream is automatically closed
                        context.contentResolver.openInputStream(uri)?.use {
                                inputStream ->
                            byteArray = inputStream.readBytes()
                            val encodedString = Base64.encode(byteArray)
                            val response = networkService.uploadObject(
                                upload = UploadObjectRequest(
                                    key = "test",
                                    content = encodedString,
                                    token = token,
                                    email = email
                                )
                            )
                            changeMessage(response.toString())
                        }
                    }


                    //val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
                    //val current = LocalDateTime.now().format(formatter)
                    //val key = "campico_$current.db"


                    //changeMessage(response.message)
                    // Log.d("FLASHCARD", response.message)


                    //} catch (e: Exception) {
                    //    changeMessage("Unexpected exception: Database cannot back up in S3")
                    //    Log.d("FLASHCARD", "EXCEPTION: $e")

                }
            }
        }
        ){Text("Upload")}
    }
    }
