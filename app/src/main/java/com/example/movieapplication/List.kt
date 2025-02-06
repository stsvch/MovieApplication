package com.example.movieapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movieapplication.ui.theme.Pink40
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.min

data class Movie(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val photoLinks: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
    val year: Int = 0
)


@Composable
fun List() {
    val db = FirebaseFirestore.getInstance()
    val movies = remember { mutableStateOf(listOf<Movie>()) }
    val genreMapping = remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val isLoading = remember { mutableStateOf(true) }
    val currentPage = remember { mutableStateOf(1) }
    val moviesPerPage = 10

    LaunchedEffect(Unit) {
        db.collection("movies").get()
            .addOnSuccessListener { querySnapshot ->
                val movieList = querySnapshot.documents.map { doc ->
                    Movie(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        photoLinks = doc.get("photoLinks") as? List<String> ?: emptyList(),
                        genres = doc.get("genres") as? List<String> ?: emptyList(),
                        year = doc.getLong("year")?.toInt() ?: 0
                    )
                }
                movies.value = movieList
                isLoading.value = false
            }
            .addOnFailureListener {
                isLoading.value = false
            }
    }

    LaunchedEffect(Unit) {
        db.collection("genres").get()
            .addOnSuccessListener { querySnapshot ->
                val mapping = querySnapshot.documents.associate { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    id to name
                }
                genreMapping.value = mapping
            }
    }

    if (isLoading.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val totalMovies = movies.value.size
    val totalPages = if (totalMovies > 0) ((totalMovies - 1) / moviesPerPage) + 1 else 1
    val startIndex = (currentPage.value - 1) * moviesPerPage
    val endIndex = min(startIndex + moviesPerPage, totalMovies)
    val currentMovies = movies.value.subList(startIndex, endIndex)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(currentMovies) { index, movie ->
                MovieItem(movie = movie, genreMapping = genreMapping.value, onClick = {
                    //
                    //
                    //
                })
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPage.value > 1) {
                IconButton(onClick = { currentPage.value-- }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous Page",
                        tint = Pink40
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
            Text(text = "Page ${currentPage.value} of $totalPages", fontSize = 16.sp)
            if (currentPage.value < totalPages) {
                IconButton(onClick = { currentPage.value++ }) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next Page",
                        tint = Pink40
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    }
}

@Composable
fun MovieItem(movie: Movie, genreMapping: Map<String, String>, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (movie.photoLinks.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(movie.photoLinks.first())
                            .crossfade(true)
                            .build(),
                        contentDescription = "Movie Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.LightGray)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = movie.title, fontSize = 20.sp, color = Pink40)
                    Spacer(modifier = Modifier.height(4.dp))
                    val genreNames = movie.genres.mapNotNull { genreId ->
                        genreMapping[genreId]
                    }
                    Text(text = genreNames.joinToString(", "), fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val shortDescription = if (movie.description.length > 256)
                movie.description.substring(0, 256) + "..."
            else movie.description
            Text(text = shortDescription, fontSize = 14.sp)
        }
    }
}

