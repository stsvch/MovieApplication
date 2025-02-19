package com.example.movieapplication

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movieapplication.Model.Review
import com.example.movieapplication.ViewModel.ReviewsViewModel

@Composable
fun UserReviewsScreen() {
    val viewModel: ReviewsViewModel = viewModel()
    val reviews by viewModel.reviews
    val context = LocalContext.current

    var editingReview by remember { mutableStateOf<Review?>(null) }
    var editingText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        if (reviews.isEmpty()) {
            item {
                Text(
                    text = "There are no review",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(reviews) { review ->
                ReviewItem(
                    review = review,
                    userData = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            editingReview = review
                            editingText = review.text
                        }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (editingReview != null) {
        AlertDialog(
            onDismissRequest = { editingReview = null },
            title = { Text(text = "Edit") },
            text = {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = { editingText = it },
                    label = { Text("Review") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val updatedReview = editingReview!!.copy(text = editingText)
                    viewModel.updateReview(updatedReview) { success ->
                        if (success) {
                            Toast.makeText(context, "Review has been updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Update error", Toast.LENGTH_SHORT).show()
                        }
                        editingReview = null
                        viewModel.loadReviews()
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { editingReview = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
