package com.mobile.campico

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MediaImageHorizontalPager(
    mediaVisits: List<MediaVisit>,
    networkService: NetworkService
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    var code by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }
    var token: String by remember { mutableStateOf("") }
    var email: String by remember { mutableStateOf("") }
    //val mediaVisits = remember { mutableStateListOf<MediaVisit>()  }
    //val bitmaps =
    //    remember { mutableStateListOf<Bitmap?>().apply { addAll(List(mediaVisits) { null }) } }
    val bitmaps = remember {
        mutableStateListOf<Bitmap?>()
    }
    val pagerState = rememberPagerState(pageCount = { bitmaps.size })

    // 1. We need to use mediaVisits.size to trigger again LaunchedEffect.
    // Otherwise, the LaunchedEffect's body will not be executed --for example,
    // if we use Unit instead.
    LaunchedEffect(mediaVisits.size) {
        // 1. We need to clean up the bitmap from the previous composition, before adding
        // the new bitmaps
        // 2. Using add ---later on--- instead of simply reassigning the new bitmaps to
        // a pre-compute bitmaps array is key for forcing the horizontal pager to
        // recompose (but other options may be available)
        bitmaps.clear()
        Log.d("CAMPICO", "Building the images for " + mediaVisits.size)
        mediaVisits.forEachIndexed { index, mediaVisit ->
            val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
            val preferences = preferencesFlow.first()
            token = preferences[TOKEN] ?: ""
            email = preferences[EMAIL] ?: ""
            // if a for-loop's body uses the launch function within a suitable CoroutineScope,
            // the different executions of the body can run concurrently
            // (or in parallel, depending on the dispatcher).
            // Multi-threaded Dispatcher (Dispatchers.Default or Dispatchers.IO):
            // If the coroutines are launched into a scope with a multi-threaded dispatcher,
            // they will be scheduled on a shared thread pool and can run in parallel on different threads.
            // This is the most common scenario for achieving true parallelism with CPU-intensive
            // or I/O-bound tasks.

            launch {
                Log.d("CAMPICO", "GETTING the bitmap for " + mediaVisits[index].s3key)
                withContext(Dispatchers.IO) {
                    try {
                        // 1. This line pauses until the response is received
                        val result = networkService.getMediaObjectByKey(
                            payload = GetMediaObjectByKeyRequest(
                                token = token,
                                email = email,
                                key = mediaVisits[index].s3key,
                            )
                        )
                        code = result.code
                        message = result.message
                        if (code == 200) {
                            val response = networkService.getBase64Image(message)
                            val base64String =
                                response.string() // Or response.string().replace("data:image/png;base64,", "") if data URI
                            val decodedBytes = java.util.Base64.getDecoder()
                                .decode(base64String) // [Link: Base64.Decoder https://docs.oracle.com/javase/8/docs/api/java/util/Base64.Decoder.html] [1]
                            val decodedBitmap =
                                BitmapFactory.decodeByteArray(
                                    decodedBytes,
                                    0,
                                    decodedBytes.size
                                )
                            bitmaps.add(decodedBitmap)
                            Log.d("CAMPICO", "GOT BITMAP" + mediaVisits[index].s3key)
                        } else {
                            Log.d("CAMPICO", "ERROR MESSAGE $message")
                        }

                    } catch (e: Exception) {
                        message = "There was an error in the request."
                        Log.d("CAMPICO", "Unexpected exception: $e")
                    }
                }
            }
        }
        // The coroutineScope function ---in this case the LaunchedEffect---
        // will suspend until all launched children are complete
        // This means, in particular, until all images have been downloaded
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { pageIndex ->
        Log.d("CAMPICO", "Page index: $pageIndex")
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