package com.example.movieapplication.Repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.movieapplication.Model.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class ReviewsRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val reviewsCache = mutableMapOf<String, List<Review>>()
    fun getReviewsForUser(
        userId: String,
        onSuccess: (List<Review>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        reviewsCache[userId]?.let { cachedReviews ->
            onSuccess(cachedReviews)
            return
        }

        db.collection("reviews")
            .whereEqualTo("user", userId)
            .get(Source.DEFAULT)
            .addOnSuccessListener { snapshot ->
                val reviews = snapshot.documents.map { doc ->
                    Review(
                        id = doc.id,
                        movie = doc.getString("movie") ?: "",
                        text = doc.getString("text") ?: "",
                        user = doc.getString("user") ?: ""
                    )
                }
                reviewsCache[userId] = reviews
                onSuccess(reviews)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun addReview(review: Review, onComplete: (Boolean) -> Unit) {
        db.collection("reviews")
            .add(review)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun updateReview(review: Review, onComplete: (Boolean) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null || currentUserId != review.user) {
            onComplete(false)
            return
        }
        db.collection("reviews").document(review.id)
            .update("text", review.text)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun deleteReview(review: Review, onComplete: (Boolean) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null || currentUserId != review.user) {
            onComplete(false)
            return
        }
        db.collection("reviews").document(review.id)
            .delete()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
}
