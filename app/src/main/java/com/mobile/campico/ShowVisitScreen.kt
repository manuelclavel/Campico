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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.io.encoding.Base64


fun jsonArrayStringToVisit(jsonString: String): Visit {
    val gson = Gson()
    // A common approach is to parse it as an Array and convert to a List
    val visitsArray = gson.fromJson(jsonString, Array<Visit>::class.java)
    Log.d("CAMPICO", visitsArray.toString())
    return visitsArray.toList().first()
}

@Composable
fun ShowVisitScreen(
    uid: Int,
    changeMessage: (String) -> Unit,
    navigateBack: () -> Unit,
    networkService: NetworkService,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext

    var code by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }

    var token: String by remember { mutableStateOf("") }
    var email: String by remember { mutableStateOf("") }

    var visit: Visit? by remember { mutableStateOf(Visit(uid = 0, Date())) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )


    LaunchedEffect(Unit) {
        val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
        val preferences = preferencesFlow.first()
        token = preferences[TOKEN] ?: ""
        email = preferences[EMAIL] ?: ""
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = networkService.getVisitByUid(
                        payload = GetVisitByUidRequest(
                            token = token,
                            email = email,
                            uid = uid
                        )
                    )
                    code = result.code
                    message = result.message


                    //Prefer ApplicationContext: When you need a Context for operations that do not interact with the UI
                    //(e.g., file operations, database access, accessing resources like strings or drawables),
                    // use the application context.
                    //The application context lives for the lifetime of your app and is safe to use on any thread.
                } catch (e: Exception) {
                    message = "There was an error in the request."
                    Log.d("CAMPICO", "Unexpected exception: $e")
                }
            }
            if (code == 200) {
                // edit the preferences and save email
                visit = jsonArrayStringToVisit(message)
            } else {
                changeMessage(message)
            }

        }

        //tree = getTreeByUid(uid);
        //changeMessage("Please, select an option.")
    }
    if (visit == null) {
        changeMessage("Visit not found")
    } else {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(
                modifier = Modifier.size(16.dp)
            )
            TextField(
                readOnly = true,
                value = convertDateToStringDate(visit?.date),
                onValueChange = { },
                modifier = Modifier.semantics { contentDescription = "dateField" },
                label = { Text("date") }
            )

            //Button(
            //    modifier = Modifier.semantics { contentDescription = "Edit" },
            //    onClick = {
            //navigateToEditTree(tree)
            //    })
            // {
            //     Text("Edit")
            // }
            Button(
                modifier = Modifier.semantics { contentDescription = "Delete" },
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            try {
                                val result = networkService.deleteVisit(
                                    payload = DeleteVisitRequest(
                                        token = token,
                                        email = email,
                                        uid = uid
                                    )
                                )
                                code = result.code
                                message = result.message
                                changeMessage(message)
                                Log.d("CAMPICO", message)

                                //Prefer ApplicationContext: When you need a Context for operations that do not interact with the UI
                                //(e.g., file operations, database access, accessing resources like strings or drawables),
                                // use the application context.
                                //The application context lives for the lifetime of your app and is safe to use on any thread.
                            } catch (e: Exception) {
                                message = "There was an error in the request."
                                Log.d("CAMPICO", "Unexpected exception: $e")
                            }
                        }
                        if (code == 200) {
                            // edit the preferences and save email
                            changeMessage(message)
                            navigateBack()
                        } else {
                            changeMessage(message)
                        }

                    }
                })
            {
                Text("Delete")
            }
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
                                val response = visit?.uid?.let {
                                    // generating a unique key
                                    val timestampMillis: Long = System.currentTimeMillis()
                                    val key = "timestamp:$timestampMillis"

                                    // generating a base64 key
                                    val byteArray = key.toByteArray(Charsets.UTF_8)
                                    val s3key: String = Base64.Default.encode(byteArray)

                                    // uploading
                                    networkService.uploadMediaVisitObject(
                                        upload = UploadMediaVisitRequest(
                                            visitUid = it,
                                            s3key = s3key,
                                            content = encodedString,
                                            token = token,
                                            email = email
                                        )
                                    )
                                }
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
}