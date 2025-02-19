package com.example.movieapplication.ViewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.movieapplication.Model.Movie
import com.example.movieapplication.Model.Review
import com.example.movieapplication.Model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MovieDetailViewModel(
    private val movieId: String,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _movie = mutableStateOf<Movie?>(null)
    val movie: State<Movie?> = _movie

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

    private val _userCache = mutableStateMapOf<String, UserData>()
    val userCache: Map<String, UserData> get() = _userCache

    init {
        loadMovieDetails()
        loadScores()
        loadReviews()
        checkIfFavorite()
    }

    private fun loadMovieDetails() {
        db.collection("movies").document(movieId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    _movie.value = Movie(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        photoLinks = doc.get("photoLinks") as? List<String> ?: emptyList(),
                        genres = doc.get("genres") as? List<String> ?: emptyList(),
                        year = doc.getLong("year")?.toInt() ?: 0
                    )
                }
                _isLoading.value = false
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
                    Review(
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
                _reviews.value = _reviews.value + Review(
                    user = uid,
                    text = _reviewText.value
                )
                _reviewText.value = ""
                _showReviewInput.value = false
            }
    }

    fun loadUserData(userId: String) {
        if (_userCache.containsKey(userId)) return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                val name = if (doc.exists()) doc.getString("name") ?: "User" else "User"
                db.collection("userInfo").document(userId).get()
                    .addOnSuccessListener { doc2 ->
                        val photoUrl = if (doc2.exists()) doc2.getString("photoUrl") ?: "" else ""
                        _userCache[userId] = UserData(name, photoUrl)
                    }
            }
    }
}