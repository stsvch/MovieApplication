package com.example.movieapplication.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.example.movieapplication.Model.Movie
import com.example.movieapplication.Repository.MovieRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesViewModel(
    private val repository: MovieRepository = MovieRepository(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _favoriteMovies = mutableStateOf<List<Movie>>(emptyList())
    val favoriteMovies: State<List<Movie>> = _favoriteMovies

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadFavoriteMovies()
    }

    fun loadFavoriteMovies() {
        _isLoading.value = true
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _favoriteMovies.value = emptyList()
            _isLoading.value = false
            return
        }

        db.collection("favorites")
            .whereEqualTo("user", uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val movieIds = querySnapshot.documents.mapNotNull { doc ->
                    doc.getString("movie")
                }

                if (movieIds.isEmpty()) {
                    _favoriteMovies.value = emptyList()
                    _isLoading.value = false
                } else {
                    val moviesList = mutableListOf<Movie>()
                    var loadedCount = 0
                    movieIds.forEach { movieId ->
                        repository.getMovieById(movieId,
                            onSuccess = { movie ->
                                if (movie != null) {
                                    moviesList.add(movie)
                                }
                                loadedCount++
                                if (loadedCount == movieIds.size) {
                                    _favoriteMovies.value = moviesList
                                    _isLoading.value = false
                                }
                            },
                            onFailure = { e ->
                                loadedCount++
                                if (loadedCount == movieIds.size) {
                                    _favoriteMovies.value = moviesList
                                    _isLoading.value = false
                                }
                            }
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                _favoriteMovies.value = emptyList()
                _isLoading.value = false
            }
    }
}