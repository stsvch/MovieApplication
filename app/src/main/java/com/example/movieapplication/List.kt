package com.example.movieapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movieapplication.Factory.MovieListViewModelFactory
import com.example.movieapplication.Model.Movie
import com.example.movieapplication.Repository.MovieRepository
import com.example.movieapplication.ViewModel.MovieListViewModel
import com.example.movieapplication.ui.theme.Pink40
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.math.min

@Composable
fun MovieListScreen(
    navController: NavHostController,
    genreFilter: String? = null,
    yearFilter: Int? = null,
    viewModel: MovieListViewModel = viewModel(
        factory = MovieListViewModelFactory(MovieRepository(FirebaseFirestore.getInstance()))
    )
) {
    val movies by viewModel.movies
    val genreMapping by viewModel.genreMapping
    val isLoading by viewModel.isLoading

    LaunchedEffect(genreFilter, yearFilter) {
        viewModel.loadMovies(genreFilter, yearFilter)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        MovieList(
            movies = movies,
            genreMapping = genreMapping,
            onItemClick = { movie ->
                navController.navigate("movie_detail/${movie.id}")
            }
        )
    }
}

@Composable
fun MovieList(
    movies: List<Movie>,
    genreMapping: Map<String, String>,
    onItemClick: (Movie) -> Unit,
    moviesPerPage: Int = 10
) {
    val currentPage = remember { mutableStateOf(1) }
    val totalMovies = movies.size
    val totalPages = if (totalMovies > 0) ((totalMovies - 1) / moviesPerPage) + 1 else 1
    val startIndex = (currentPage.value - 1) * moviesPerPage
    val endIndex = min(startIndex + moviesPerPage, totalMovies)
    val currentMovies = movies.subList(startIndex, endIndex)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(currentMovies) { _, movie ->
                MovieItem(
                    movie = movie,
                    genreMapping = genreMapping,
                    onClick = { onItemClick(movie) }
                )
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
                        imageVector = Icons.Filled.ArrowBack,
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
                        imageVector = Icons.Filled.ArrowForward,
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