package com.example.movieapplication.ViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.example.movieapplication.Model.Movie
import com.example.movieapplication.Repository.MovieRepository
import com.google.firebase.firestore.FirebaseFirestore

class MovieListViewModel(
    private val repository: MovieRepository = MovieRepository(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _movies = mutableStateOf<List<Movie>>(emptyList())
    val movies: State<List<Movie>> = _movies

    private val _genreMapping = mutableStateOf<Map<String, String>>(emptyMap())
    val genreMapping: State<Map<String, String>> = _genreMapping

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadGenres()
        loadMovies()
    }

    fun loadMovies(genreFilter: String? = null, yearFilter: Int? = null) {
        _isLoading.value = true
        repository.loadMovies(genreFilter, yearFilter,
            onSuccess = { movieList ->
                _movies.value = movieList
                _isLoading.value = false
            },
            onFailure = { exception ->
                _isLoading.value = false
            }
        )
    }

    private fun loadGenres() {
        FirebaseFirestore.getInstance().collection("genres").get()
            .addOnSuccessListener { querySnapshot ->
                val mapping = querySnapshot.documents.associate { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    id to name
                }
                _genreMapping.value = mapping
            }
    }
}