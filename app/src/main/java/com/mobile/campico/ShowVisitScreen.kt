package com.mobile.campico

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.io.encoding.Base64
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

//import android.util.Base64


fun jsonArrayStringToMediaVisitList(jsonString: String): List<MediaVisit> {
    val gson = Gson()
    val mediaVisitsArray = gson.fromJson(jsonString, Array<MediaVisit>::class.java)
    return mediaVisitsArray.toList()
}

@Composable
fun ImagePagerBuilder(
    mediaVisits: List<MediaVisit>,
    networkService: NetworkService
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext
    var code by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }
    var token: String by remember { mutableStateOf("") }
    var email: String by remember { mutableStateOf("") }
    val bitmaps = remember {  mutableStateListOf<Bitmap?>().apply {
        addAll(List(mediaVisits.size) { null }) //
    } }

    val pagerState = rememberPagerState(pageCount = {
        mediaVisits.size
    })

    LaunchedEffect(pagerState.currentPage) {
        Log.d("CAMPICO", "CREATING a page: " + pagerState.pageCount)
        if (bitmaps[pagerState.currentPage] == null) {
            val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
            val preferences = preferencesFlow.first()
            token = preferences[TOKEN] ?: ""
            email = preferences[EMAIL] ?: ""

            scope.launch {
                Log.d("CAMPICO", "GETTING the bitmap...")
                withContext(Dispatchers.IO) {
                    try {
                        // This line pauses until the response is received
                        val result = networkService.getMediaObjectByKey(
                            payload = GetMediaObjectByKeyRequest(
                                token = token,
                                email = email,
                                key = mediaVisits[pagerState.currentPage].s3key,
                            )
                        )
                        code = result.code
                        message = result.message
                        //The error "Exceeded maximum allowed payload size
                        // (6291556 bytes)" occurs because your AWS Lambda
                        // function's request or response payload has exceeded
                        // the hard limit of 6 MB for synchronous invocations.
                        // This is a fixed quota and cannot be increased
                        // by an AWS support request.
                        //Amazon AWS Documentation
                       if (code == 200) {
                            val response = networkService.getBase64Image(message)
                            val base64String =
                                response.string() // Or response.string().replace("data:image/png;base64,", "") if data URI
                            val decodedBytes = java.util.Base64.getDecoder()
                                .decode(base64String) // [Link: Base64.Decoder https://docs.oracle.com/javase/8/docs/api/java/util/Base64.Decoder.html] [1]
                            val decodedBitmap =
                                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                           //val base64String = result.string()
                           //val decodedBytes = java.util.Base64.getDecoder()
                           //    .decode(base64String) // [Link: Base64.Decoder https://docs.oracle.com/javase/8/docs/api/java/util/Base64.Decoder.html] [1]
                           //val decodedBitmap =
                           //    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                           bitmaps[pagerState.currentPage] = decodedBitmap

                        } else {
                            Log.d("CAMPICO", "ERROR MESSAGE " + message)

                        }

                         } catch (e: Exception) {
                        message = "There was an error in the request."
                        Log.d("CAMPICO", "Unexpected exception: $e")
                    }
                }


            }
        } else {
            //Log.d("CAMPICO", "Bitmaps: " + bitmaps.size)

        }
    }
    // 2. Use HorizontalPager to create a swipeable interface
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { pageIndex ->
        Log.d("CAMPICO", "Page index:" + pageIndex)
        Log.d("CAMPICO", "Page index:" + pageIndex)
         bitmaps[pageIndex]?.let {
                Image(
                    bitmap = it.asImageBitmap(), // Convert to Compose's ImageBitmap
                    contentDescription = "Image $pageIndex", // Content description for accessibility
                    contentScale = ContentScale.Fit, // Adjust the image scaling as needed
                    modifier = Modifier.fillMaxSize()
                )
        }
    }
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

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )

    var mediaVisits by remember { mutableStateOf(emptyList<MediaVisit>()) }


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
                // edit the preferences and save email
                visit = jsonArrayStringToVisit(message)
            } else {
                changeMessage(message)
            }

        }
        // get list of media-image-visits
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = networkService.getImagesVisitByVisitUid(
                        payload = GetImagesVisitByVisitUidRequest(
                            token = token,
                            email = email,
                            visitUid = uid,
                            mediaType = 0
                        )
                    )
                    code = result.code
                    message = result.message
                    //Log.d("CAMPICO", message)

                } catch (e: Exception) {
                    message = "There was an error in the request."
                    Log.d("CAMPICO", "Unexpected exception: $e")
                }
            }
            if (code == 200) {
                Log.d("CAMPICO", "DOWNLOADED: " + message)
                // edit the preferences and save email
                mediaVisits = jsonArrayStringToMediaVisitList(message)
                Log.d("CAMPICO", "DOWNLOADED: " + mediaVisits)
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


            Spacer(
                modifier = Modifier.size(16.dp)
            )
            if (mediaVisits.isNotEmpty()) {
                    ImagePagerBuilder(
                        mediaVisits = mediaVisits,
                        networkService = networkService
                    )
                }
            //MediaVisitList(
            //    mediaVisits = mediaVisits,
            //    networkService = networkService,
            //    displayBitmap = displayBitMap
            //)

            //if (bitmapState != null){
            //     AsyncImage(
            //         model = bitmapState, // Pass the Bitmap directly
            //         contentDescription = "Decoded Image"
            //     )
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
                                            email = email,
                                            mediaType = 0
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
            ) { Text("Upload") }
        }


    }
}