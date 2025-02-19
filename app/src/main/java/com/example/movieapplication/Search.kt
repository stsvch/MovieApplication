package com.example.movieapplication

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movieapplication.ui.theme.Pink40
import androidx.navigation.NavHostController
import com.example.movieapplication.ViewModel.SearchViewModel

@Composable
fun Search(navController: NavHostController) {
    val viewModel: SearchViewModel = viewModel()
    val searchQuery by remember { derivedStateOf { viewModel.searchQuery } }
    val genres by remember { derivedStateOf { viewModel.genres } }
    val selectedGenres by remember { derivedStateOf { viewModel.selectedGenres } }
    val movies = viewModel.filteredMovies

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Search by title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            genres.forEach { (genreId, genreName) ->
                val isSelected = selectedGenres.contains(genreId)
                Button(
                    onClick = { viewModel.toggleGenre(genreId) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Pink40 else Color.LightGray
                    )
                ) {
                    Text(
                        text = genreName,
                        color = if (isSelected) Color.White else Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (movies.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No movies found", fontSize = 20.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(movies) { movie ->
                    MovieItem(
                        movie = movie,
                        genreMapping = genres,
                        onClick = { navController.navigate("movie_detail/${movie.id}") }
                    )
                }
            }
        }
    }
}