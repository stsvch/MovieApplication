package com.example.movieapplication.ViewModel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.example.movieapplication.Model.Movie
import com.example.movieapplication.Model.MovieData
import com.example.movieapplication.Model.Review
import com.example.movieapplication.Model.UserData
import com.example.movieapplication.Model.Actor
import com.example.movieapplication.Model.Director
import com.example.movieapplication.Model.Genre
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class MovieDetailViewModel(
    private val movieId: String,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    companion object {
        private val movieDetailsCache = mutableMapOf<String, Movie>()
        private val fullMovieDetailsCache = mutableMapOf<String, MovieData>()
    }

    private val _movie = mutableStateOf<Movie?>(null)
    val movie: State<Movie?> = _movie

    private val _fullMovie = mutableStateOf<MovieData?>(null)
    val fullMovie: State<MovieData?> = _fullMovie

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _isFavorite = mutableStateOf(false)
    val isFavorite: State<Boolean> = _isFavorite

    private val _averageRating = mutableStateOf(0f)
    val averageRating: State<Float> = _averageRating

    private val _reviews = mutableStateOf<List<Review>>(emptyList())
    val reviews: State<List<Review>> = _reviews

    private val _showReviewInput = mutableStateOf(false)
    val showReviewInput: State<Boolean> = _showReviewInput

    private val _reviewText = mutableStateOf("")
    val reviewText: State<String> = _reviewText

    private val _userCache = mutableStateMapOf<String, com.example.movieapplication.Model.UserData>()
    val userCache: Map<String, com.example.movieapplication.Model.UserData> get() = _userCache

    init {
        loadMovieDetails()
        loadFullMovieDetails()
        loadScores()
        loadReviews()
        checkIfFavorite()
    }

    private fun loadMovieDetails() {
        movieDetailsCache[movieId]?.let { cachedMovie ->
            _movie.value = cachedMovie
            _isLoading.value = false
            return
        }
        db.collection("movies").document(movieId).get(Source.DEFAULT)
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val movieData = Movie(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        photoLinks = doc.get("photoLinks") as? List<String> ?: emptyList(),
                        genres = doc.get("genres") as? List<String> ?: emptyList(),
                        year = doc.getLong("year")?.toInt() ?: 0
                    )
                    movieDetailsCache[movieId] = movieData
                    _movie.value = movieData
                }
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    private fun loadFullMovieDetails() {
        fullMovieDetailsCache[movieId]?.let { cachedFull ->
            _fullMovie.value = cachedFull
            return
        }

        db.collection("movies").document(movieId).get(Source.DEFAULT)
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val title = doc.getString("title") ?: ""
                    val description = doc.getString("description") ?: ""
                    val photoLinks = doc.get("photoLinks") as? List<String> ?: emptyList()
                    val year = doc.getLong("year")?.toInt() ?: 0
                    val category = doc.getString("category") ?: ""
                    val duration = doc.getLong("duration")?.toInt() ?: 0
                    // Получаем ID списков для актёров, режиссёров и жанров
                    val actorIds = doc.get("actors") as? List<String> ?: emptyList()
                    val directorIds = doc.get("directors") as? List<String> ?: emptyList()
                    val genreIds = doc.get("genres") as? List<String> ?: emptyList()

                    // Последовательно загружаем данные для актёров, режиссёров и жанров
                    loadActors(actorIds) { actorsList ->
                        loadDirectors(directorIds) { directorsList ->
                            loadGenres(genreIds) { genresList ->
                                val fullData = MovieData(
                                    id = doc.id,
                                    title = title,
                                    description = description,
                                    photoLinks = photoLinks,
                                    year = year,
                                    category = category,
                                    duration = duration,
                                    actors = actorsList,
                                    directors = directorsList,
                                    genres = genresList
                                )
                                fullMovieDetailsCache[movieId] = fullData
                                _fullMovie.value = fullData
                            }
                        }
                    }
                }
            }
    }

    private fun loadActors(actorIds: List<String>, callback: (List<Actor>) -> Unit) {
        if (actorIds.isEmpty()) {
            callback(emptyList())
            return
        }
        val actorsList = mutableListOf<Actor>()
        var count = 0
        for (id in actorIds) {
            db.collection("actors").document(id).get(Source.DEFAULT)
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        val name = doc.getString("name") ?: ""
                        actorsList.add(Actor(id = id, name = name))
                    }
                    count++
                    if (count == actorIds.size) callback(actorsList)
                }
                .addOnFailureListener {
                    count++
                    if (count == actorIds.size) callback(actorsList)
                }
        }
    }

    private fun loadDirectors(directorIds: List<String>, callback: (List<Director>) -> Unit) {
        if (directorIds.isEmpty()) {
            callback(emptyList())
            return
        }
        val directorsList = mutableListOf<Director>()
        var count = 0
        for (id in directorIds) {
            db.collection("directors").document(id).get(Source.DEFAULT)
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        val name = doc.getString("name") ?: ""
                        directorsList.add(Director(id = id, name = name))
                    }
                    count++
                    if (count == directorIds.size) callback(directorsList)
                }
                .addOnFailureListener {
                    count++
                    if (count == directorIds.size) callback(directorsList)
                }
        }
    }

    private fun loadGenres(genreIds: List<String>, callback: (List<Genre>) -> Unit) {
        if (genreIds.isEmpty()) {
            callback(emptyList())
            return
        }
        val genresList = mutableListOf<Genre>()
        var count = 0
        for (id in genreIds) {
            db.collection("genres").document(id).get(Source.DEFAULT)
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        val name = doc.getString("name") ?: ""
                        genresList.add(Genre(id = id, name = name))
                    }
                    count++
                    if (count == genreIds.size) callback(genresList)
                }
                .addOnFailureListener {
                    count++
                    if (count == genreIds.size) callback(genresList)
                }
        }
    }

    private fun loadScores() {
        db.collection("scores")
            .whereEqualTo("movie", movieId)
            .get()
            .addOnSuccessListener { query ->
                val scores = query.documents.mapNotNull { it.getDouble("score") }
                _averageRating.value = if (scores.isNotEmpty()) scores.average().toFloat() else 0f
            }
    }

    private fun loadReviews() {
        db.collection("reviews")
            .whereEqualTo("movie", movieId)
            .get()
            .addOnSuccessListener { query ->
                _reviews.value = query.documents.map { doc ->
                    // Если понадобится, можно добавить id отзыва
                    com.example.movieapplication.Model.Review(
                        user = doc.getString("user") ?: "",
                        text = doc.getString("text") ?: ""
                    )
                }
            }
    }

    private fun checkIfFavorite() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("favorites")
            .whereEqualTo("user", uid)
            .whereEqualTo("movie", movieId)
            .get()
            .addOnSuccessListener { query ->
                _isFavorite.value = query.documents.isNotEmpty()
            }
    }

    fun toggleFavorite() {
        val uid = auth.currentUser?.uid ?: return
        _isFavorite.value = !_isFavorite.value
        val favRef = db.collection("favorites")
        if (_isFavorite.value) {
            favRef.add(mapOf("user" to uid, "movie" to movieId))
        } else {
            favRef.whereEqualTo("user", uid)
                .whereEqualTo("movie", movieId)
                .get()
                .addOnSuccessListener { query ->
                    query.documents.forEach { doc ->
                        favRef.document(doc.id).delete()
                    }
                }
        }
    }

    fun updateRating(newRating: Float) {
        val uid = auth.currentUser?.uid ?: return
        val scoresRef = db.collection("scores")
        scoresRef
            .whereEqualTo("user", uid)
            .whereEqualTo("movie", movieId)
            .get()
            .addOnSuccessListener { query ->
                if (query.documents.isEmpty()) {
                    scoresRef.add(mapOf("user" to uid, "movie" to movieId, "score" to newRating))
                } else {
                    query.documents.forEach { doc ->
                        scoresRef.document(doc.id).update("score", newRating)
                    }
                }
                scoresRef
                    .whereEqualTo("movie", movieId)
                    .get()
                    .addOnSuccessListener { query2 ->
                        val scores = query2.documents.mapNotNull { it.getDouble("score") }
                        _averageRating.value = if (scores.isNotEmpty()) scores.average().toFloat() else 0f
                    }
            }
    }

    fun setReviewText(text: String) {
        _reviewText.value = text
    }

    fun showReviewInput(show: Boolean) {
        _showReviewInput.value = show
    }

    fun submitReview() {
        val uid = auth.currentUser?.uid ?: return
        val reviewData = mapOf(
            "movie" to movieId,
            "user" to uid,
            "text" to _reviewText.value
        )
        db.collection("reviews")
            .add(reviewData)
            .addOnSuccessListener {
                _reviews.value = _reviews.value + com.example.movieapplication.Model.Review(
                    user = uid,
                    text = _reviewText.value
                )
                _reviewText.value = ""
                _showReviewInput.value = false
            }
    }

    fun loadUserData(userId: String) {
        if (_userCache.containsKey(userId)) return

        db.collection("users").document(userId).get(Source.DEFAULT)
            .addOnSuccessListener { doc ->
                val name = if (doc.exists()) doc.getString("name") ?: "User" else "User"
                db.collection("userInfo").document(userId).get(Source.DEFAULT)
                    .addOnSuccessListener { doc2 ->
                        val photoUrl = if (doc2.exists()) doc2.getString("photoUrl") ?: "" else ""
                        _userCache[userId] = com.example.movieapplication.Model.UserData(name, photoUrl)
                    }
            }
    }
}
