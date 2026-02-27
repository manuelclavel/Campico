package com.mobile.campico

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

fun jsonArrayStringToMediaVisitList(jsonString: String): List<MediaVisit> {
    val gson = Gson()
    val mediaVisitsArray = gson.fromJson(jsonString, Array<MediaVisit>::class.java)
    return mediaVisitsArray.toList()
}




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
        getRefreshImagePagerVisit: () -> Boolean,
    ) {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val appContext = context.applicationContext

        var code by remember { mutableIntStateOf(0) }
        var message by remember { mutableStateOf("") }

        var token: String by remember { mutableStateOf("") }
        var email: String by remember { mutableStateOf("") }

        var visit: Visit? by remember { mutableStateOf(Visit(uid = 0, Date())) }
        var mediaVisits by remember { mutableStateOf(emptyList<MediaVisit>()) }



        val refreshMediaVisits = fun(updatedMediaVisits: List<MediaVisit>): Unit {
            mediaVisits = updatedMediaVisits.toList()
            Log.d("CAMPICO", "Building the images for " + mediaVisits.size)
        }

        LaunchedEffect(getRefreshImagePagerVisit()) {
            changeMessage("Loading images...")
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
                    } catch (e: Exception) {
                        message = "There was an error in the request."
                        Log.d("CAMPICO", "Unexpected exception: $e")
                    }
                }
                if (code == 200) {
                   visit = jsonArrayStringToVisit(message)
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val result = networkService.getImagesVisitByVisitUid(
                                payload = GetImagesVisitByVisitUidRequest(
                                    token = token,
                                    email = email,
                                    visitUid = uid,
                                    mediaType = 0
                                )
                            )
                            val code = result.code
                            val message = result.message
                            if (code == 200) {
                                Log.d("CAMPICO", "Getting the list of media ")
                                val updateMediaVisits = jsonArrayStringToMediaVisitList(message)
                                Log.d("CAMPICO", "Total of media " + updateMediaVisits.size)
                                Log.d(
                                    "CAMPICO",
                                    "Refreshing the pager with now: " + updateMediaVisits.size
                                )
                                if (updateMediaVisits.isNotEmpty()) {
                                    refreshMediaVisits(updateMediaVisits)
                                }
                            }
                        }
                    }

                } else {
                    changeMessage(message)
                }

            }
        }

        if (visit == null) {
            changeMessage("Visit not found")
        } else {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Spacer(
                    modifier = Modifier.size(8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        readOnly = true,
                        value = convertDateToStringDate(visit?.date),
                        onValueChange = { },
                        modifier = Modifier
                            .weight(1f) // Takes up remaining space
                            .padding(end = 8.dp)
                            .semantics { contentDescription = "dateField" },
                        label = { Text("date") }
                    )

                }

                MediaImageHorizontalPager(
                    mediaVisits = mediaVisits,
                    networkService = networkService,
                    changeMessage = changeMessage
                )
            }
        }
    }
                /*
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

            Button(onClick = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        //val byteArrayOutputStream = ByteArrayOutputStream()
                        changeMessage("Uploading object in S3...")
                        imageUri?.let { uri ->
                            val byteArray: ByteArray
                            // Use the 'use' extension function to ensure
                            // the InputStream is automatically closed
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                byteArray = inputStream.readBytes()
                                /* reduce size */
                                val compressedByteArray = compressImageToLessThan1MB(
                                    imageBytes = byteArray)
                                /* end */
                                val encodedString = Base64.encode(compressedByteArray)
                                val response = visit?.uid?.let {
                                    // generating a unique key
                                    val timestampMillis: Long = System.currentTimeMillis()
                                    val key = "timestamp:$timestampMillis"

                                    // generating a base64 key
                                    val keyAsByteArray = key.toByteArray(Charsets.UTF_8)
                                    val s3key: String = Base64.Default.encode(keyAsByteArray)

                                    // uploading
                                    networkService.uploadMediaVisitObject(
                                        upload = UploadMediaVisitRequest(
                                            visitUid = it,
                                            s3key = s3key,
                                            content = encodedString,
                                            token = token,
                                            email = email,
                                            mediaType = 0
                                        )
                                    )
                                }
                                changeMessage(response.toString())
                            }
                        }


                    }
                }
            }
            ) { Text("Upload") }

    */




