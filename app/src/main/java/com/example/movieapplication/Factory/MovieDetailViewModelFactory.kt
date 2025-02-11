package com.example.movieapplication.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.movieapplication.ViewModel.MovieDetailViewModel

class MovieDetailViewModelFactory(private val movieId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovieDetailViewModel(movieId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}