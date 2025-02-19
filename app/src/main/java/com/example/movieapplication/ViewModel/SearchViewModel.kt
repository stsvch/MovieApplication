package com.example.movieapplication.ViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.movieapplication.Model.Movie
import com.example.movieapplication.Repository.MovieRepository
import com.google.firebase.firestore.FirebaseFirestore

class SearchViewModel(
    private val repository: MovieRepository = MovieRepository(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _movies = mutableStateOf<List<Movie>>(emptyList())
    val movies get() = _movies.value

    private val _genres = mutableStateOf<Map<String, String>>(emptyMap())
    val genres get() = _genres.value

    var searchQuery by mutableStateOf("")
        private set

    var selectedGenres by mutableStateOf(setOf<String>())
        private set

    val filteredMovies: List<Movie>
        get() {
            if (searchQuery.isBlank() && selectedGenres.isEmpty()) return movies
            return movies.filter { movie ->
                val matchesTitle = if (searchQuery.isNotBlank()) {
                    movie.title.contains(searchQuery, ignoreCase = true)
                } else true
                val matchesGenre = if (selectedGenres.isNotEmpty()) {
                    movie.genres.any { genreId -> selectedGenres.contains(genreId) }
                } else true
                matchesTitle && matchesGenre
            }
        }

    init {
        loadMovies()
        loadGenres()
    }

    fun loadMovies() {
        repository.loadMovies(
            genreFilter = null,
            yearFilter = null,
            onSuccess = { movieList ->
                _movies.value = movieList
            },
            onFailure = { exception ->

            }
        )
    }

    fun loadGenres() {
        if (_genres.value.isNotEmpty()) return

        FirebaseFirestore.getInstance().collection("genres").get()
            .addOnSuccessListener { querySnapshot ->
                val mapping = querySnapshot.documents.associate { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    id to name
                }
                _genres.value = mapping
            }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun toggleGenre(genreId: String) {
        selectedGenres = if (selectedGenres.contains(genreId)) {
            selectedGenres - genreId
        } else {
            selectedGenres + genreId
        }
    }
}