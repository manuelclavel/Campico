package com.mobile.campico

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePager(images: List<String>) {
    // 1. Remember the Pager State
    val pagerState = rememberPagerState(pageCount = { images.size })

    Column(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Fills the remaining vertical space
        ) { pageIndex ->
            // 2. Load and display the image for the current page
            val imageUrl = images[pageIndex]
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .size(Size.ORIGINAL) // Optional: optimize image loading
                    .build()
            )

            Image(
                painter = painter,
                contentDescription = "Image $pageIndex",
                contentScale = ContentScale.Crop, // Adjust scale as needed
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )

            // Optional: Show a loading indicator
            if (painter.state is coil.compose.AsyncImagePainter.State.Loading) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// Optional: Add a page indicator (dots, etc., as described in the [Android Developers