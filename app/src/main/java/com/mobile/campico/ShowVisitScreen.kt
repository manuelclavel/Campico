package com.mobile.campico

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.core.graphics.scale
import androidx.datastore.preferences.core.Preferences
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Date
import kotlin.io.encoding.Base64


fun compressImageToLessThan1MB(imageBytes: ByteArray, maxFileSize: Long = 1024000): ByteArray {
        // 1. Decode the original ByteArray into a Bitmap
        var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        // 2. Initialize variables for compression
        var quality = 90 // Start with a decent quality
        val outputStream = ByteArrayOutputStream()

        // 3. Compress the bitmap to the output stream
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        var compressedData = outputStream.toByteArray()

        // 4. Iterate to reduce quality or rescale until the size is under the limit
        while (compressedData.size > maxFileSize) {
            outputStream.reset() // Reset the output stream for a new compression attempt
            quality -= 5 // Decrease quality by 5 for the next iteration

            if (quality < 10) {
                // If quality is too low, rescale the image to fewer pixels
                bitmap = bitmap.scale((bitmap.width * 0.8).toInt(), (bitmap.height * 0.8).toInt())
                quality = 90 // Reset quality for the smaller bitmap
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedData = outputStream.toByteArray()
        }

        // 5. Recycle the bitmap to free memory
        bitmap.recycle()

        return compressedData
    }

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


        var imageUri by remember { mutableStateOf<Uri?>(null) }

        val refreshMediaVisits = fun(updatedMediaVisits: List<MediaVisit>): Unit {
            mediaVisits = updatedMediaVisits.toList()
            Log.d("CAMPICO", "Building the images for " + mediaVisits.size)
        }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
            onResult = { success ->
                if (success) {
                    Log.d("CAMPICO", "successfully capturing image")
                    // Photo captured successfully, imageUri now points to the file
                    // You can initiate the upload here
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            changeMessage("Uploading object in S3...")
                            imageUri?.let { uri ->
                                val byteArray: ByteArray
                                // Use the 'use' extension function to ensure
                                // the InputStream is automatically closed
                                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                    byteArray = inputStream.readBytes()

                                    /* reduce size */
                                    val compressedByteArray = compressImageToLessThan1MB(
                                        imageBytes = byteArray
                                    )
                                    /* end */
                                    val encodedString = Base64.encode(compressedByteArray)
                                    val response = visit?.let {
                                        // generating a unique key
                                        val timestampMillis: Long = System.currentTimeMillis()
                                        val key = "timestamp:$timestampMillis"

                                        // generating a base64 key
                                        val keyAsByteArray = key.toByteArray(Charsets.UTF_8)
                                        val s3key: String = Base64.Default.encode(keyAsByteArray)


                                        networkService.uploadMediaVisitObject(
                                            upload = UploadMediaVisitRequest(
                                                content = encodedString,
                                                token = token,
                                                email = email,
                                                s3key = s3key,
                                                mediaType = 0,
                                                visitUid = it.uid
                                            )
                                        )
                                    }
                                    response?.let { response ->
                                        if (response.code == 200) {
                                            visit?.let { visit ->
                                                scope.launch {
                                                    withContext(Dispatchers.IO) {
                                                        val result =
                                                            networkService.getImagesVisitByVisitUid(
                                                                payload = GetImagesVisitByVisitUidRequest(
                                                                    token = token,
                                                                    email = email,
                                                                    visitUid = visit.uid,
                                                                    mediaType = 0
                                                                )
                                                            )
                                                        val code = result.code
                                                        val message = result.message
                                                        if (code == 200) {
                                                            val updateMediaVisits =
                                                                jsonArrayStringToMediaVisitList(
                                                                    message
                                                                )
                                                            Log.d(
                                                                "CAMPICO",
                                                                "Refreshing the pager with now: " + updateMediaVisits.size
                                                            )
                                                            refreshMediaVisits(updateMediaVisits)
                                                        }
                                                    }
                                                }

                                            }
                                        }
                                    }
                                    changeMessage(response.toString())
                                }
                            }

                        }
                    }
                }
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
                    /* camera */
                    Button(onClick = {
                        val uri = getTmpFileUri(context)
                        imageUri = uri
                        cameraLauncher.launch(uri)
                    }) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Take a picture"
                        )

                    }
                    /* delete */
                    Button(
                        modifier = Modifier.semantics { contentDescription = "Delete" },
                        onClick = {
                            changeMessage("Work in progress")
                            /*
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
                        */

                        })
                    {
                        Icon(Icons.Default.Delete, contentDescription = "DeleteVisit")
                        //Text("Delete")
                    }


                }

                MediaImageHorizontalPager(
                    mediaVisits = mediaVisits,
                    networkService = networkService
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




