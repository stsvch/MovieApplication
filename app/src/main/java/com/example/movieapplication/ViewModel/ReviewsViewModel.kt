package com.example.movieapplication.ViewModel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.example.movieapplication.Model.Review
import com.example.movieapplication.Model.UserData
import com.example.movieapplication.Repository.ReviewsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class ReviewsViewModel(
    private val repository: ReviewsRepository = ReviewsRepository()
) : ViewModel() {

    private val _reviews = mutableStateOf<List<Review>>(emptyList())
    val reviews: State<List<Review>> = _reviews

    val userCache = mutableStateMapOf<String, UserData>()

    init {
        loadReviews()
    }

    fun loadReviews() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            repository.getReviewsForUser(userId,
                onSuccess = { reviewsList ->
                    _reviews.value = reviewsList
                },
                onFailure = {
                }
            )
        }
    }

    fun loadUserData(userId: String) {
        if (userCache.containsKey(userId)) return

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get(Source.DEFAULT)
            .addOnSuccessListener { userDoc ->
                val name = userDoc.getString("name") ?: "User"
                db.collection("userInfo").document(userId).get(Source.DEFAULT)
                    .addOnSuccessListener { infoDoc ->
                        val photoUrl = infoDoc.getString("photoUrl") ?: ""
                        userCache[userId] = UserData(name = name, photoUrl = photoUrl)
                    }
            }
    }

    fun updateReview(review: Review, onComplete: (Boolean) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == review.user) {
            repository.updateReview(review) { success ->
                if (success) {
                    _reviews.value = _reviews.value.map { if (it.id == review.id) review else it }
                }
                onComplete(success)
            }
        } else {
            onComplete(false)
        }
    }

    fun deleteReview(review: Review, onComplete: (Boolean) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == review.user) {
            repository.deleteReview(review) { success ->
                if (success) {
                    _reviews.value = _reviews.value.filter { it.id != review.id }
                }
                onComplete(success)
            }
        } else {
            onComplete(false)
        }
    }
}
