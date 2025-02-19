package com.example.movieapplication.Repository

import com.example.movieapplication.Model.Review
import com.google.firebase.firestore.FirebaseFirestore

class ReviewsRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun getReviewsForUser(
        userId: String,
        onSuccess: (List<Review>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("reviews")
            .whereEqualTo("user", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val reviews = snapshot.documents.map { doc ->
                    Review(
                        id = doc.id,
                        movie = doc.getString("movie") ?: "",
                        text = doc.getString("text") ?: "",
                        user = doc.getString("user") ?: ""
                    )
                }
                onSuccess(reviews)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun updateReview(review: Review, onComplete: (Boolean) -> Unit) {
        db.collection("reviews").document(review.id)
            .update("text", review.text)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
}
