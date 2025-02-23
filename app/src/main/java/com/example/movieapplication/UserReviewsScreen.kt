package com.example.movieapplication

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movieapplication.Model.Review
import com.example.movieapplication.ViewModel.ReviewsViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete

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
                    text = "There are no reviews",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(reviews) { review ->
                LaunchedEffect(review.user) {
                    viewModel.loadUserData(review.user)
                }
                val userData = viewModel.userCache[review.user]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    ReviewItem(
                        review = review,
                        userData = userData,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                editingReview = review
                                editingText = review.text
                            }
                    )
                    IconButton(
                        onClick = {
                            viewModel.deleteReview(review) { success ->
                                if (success) {
                                    Toast.makeText(context, "Review deleted", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Delete error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Review"
                        )
                    }
                }
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
