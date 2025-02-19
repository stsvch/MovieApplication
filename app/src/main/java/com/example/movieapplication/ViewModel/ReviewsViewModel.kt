package com.example.movieapplication.ViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.example.movieapplication.Model.Review
import com.example.movieapplication.Repository.ReviewsRepository
import com.google.firebase.auth.FirebaseAuth

class ReviewsViewModel(
    private val repository: ReviewsRepository = ReviewsRepository()
) : ViewModel() {

    private val _reviews = mutableStateOf<List<Review>>(emptyList())
    val reviews: State<List<Review>> = _reviews

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

    fun updateReview(review: Review, onComplete: (Boolean) -> Unit) {
        repository.updateReview(review, onComplete)
    }
}
