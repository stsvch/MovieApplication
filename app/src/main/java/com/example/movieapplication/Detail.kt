package com.example.movieapplication

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movieapplication.Model.MovieData
import com.example.movieapplication.Model.Movie
import com.example.movieapplication.ui.theme.Pink40
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movieapplication.Factory.MovieDetailViewModelFactory
import com.example.movieapplication.Model.Review
import com.example.movieapplication.ViewModel.MovieDetailViewModel

@Composable
fun MovieDetailScreen(movieId: String) {
    val viewModel: MovieDetailViewModel = viewModel(
        factory = MovieDetailViewModelFactory(movieId)
    )

    val movie by viewModel.movie
    val fullMovie by viewModel.fullMovie
    val isLoading by viewModel.isLoading
    val isFavorite by viewModel.isFavorite
    val averageRating by viewModel.averageRating
    val reviews by viewModel.reviews
    val showReviewInput by viewModel.showReviewInput
    val reviewText by viewModel.reviewText

    val context = LocalContext.current

    if (isLoading || movie == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val pagerState = rememberPagerState(pageCount = { movie!!.photoLinks.size })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) { page ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(movie!!.photoLinks[page])
                        .crossfade(true)
                        .build(),
                    contentDescription = "Movie Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Pink40
                    )
                }
                InteractiveRatingBar(
                    currentRating = averageRating,
                    onRatingChanged = { newRating ->
                        viewModel.updateRating(newRating)
                    }
                )
            }
        }
        item {
            Text(text = movie!!.title, fontSize = 28.sp, color = Pink40)
        }
        item {
            Text(text = movie!!.description, fontSize = 16.sp)
        }
        // Дополнительная информация из fullMovie (MovieData)
        if (fullMovie != null) {
            item {
                Text(text = "Category: ${fullMovie?.category}", fontSize = 16.sp)
            }
            item {
                Text(text = "Directors: ${fullMovie?.directors?.joinToString(", ") { it.name }}", fontSize = 16.sp)
            }
            item {
                Text(text = "Actors: ${fullMovie?.actors?.joinToString(", ") { it.name }}", fontSize = 16.sp)
            }
            item {
                Text(text = "Genres: ${fullMovie?.genres?.joinToString(", ") { it.name }}", fontSize = 16.sp)
            }
            item {
                Text(text = "Duration: ${fullMovie?.duration} min", fontSize = 16.sp)
            }
        }
        item {
            Button(
                onClick = { viewModel.showReviewInput(true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Leave a review")
            }
        }
        if (showReviewInput) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { viewModel.setReviewText(it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Your review") }
                    )
                    IconButton(
                        onClick = { viewModel.submitReview() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Review",
                            tint = Pink40
                        )
                    }
                }
            }
        }
        item {
            Text(
                text = "Reviews",
                fontSize = 20.sp,
                color = Pink40,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        items(reviews) { review ->
            LaunchedEffect(review.user) {
                viewModel.loadUserData(review.user)
            }
            val userData = viewModel.userCache[review.user]
            ReviewItem(review = review, userData = userData)
        }
    }
}

@Composable
fun InteractiveRatingBar(
    currentRating: Float,
    onRatingChanged: (Float) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var newRating by remember { mutableStateOf(currentRating) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Rate this movie") },
            text = {
                Column {
                    Text("Select your rating:")
                    Slider(
                        value = newRating,
                        onValueChange = { newRating = it },
                        valueRange = 0f..5f,
                        steps = 9
                    )
                    Text("Rating: ${"%.1f".format(newRating)}")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onRatingChanged(newRating)
                    showDialog = false
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Row(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { showDialog = true }
            )
        }
    ) {
        val maxStars = 5
        val fullStars = currentRating.toInt()
        val halfStar = (currentRating - fullStars) >= 0.5f
        val emptyStars = maxStars - fullStars - if (halfStar) 1 else 0
        repeat(fullStars) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Full Star",
                tint = Color.Yellow,
                modifier = Modifier.size(24.dp)
            )
        }
        if (halfStar) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Half Star",
                tint = Color.Yellow,
                modifier = Modifier.size(24.dp)
            )
        }
        repeat(emptyStars) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Empty Star",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
