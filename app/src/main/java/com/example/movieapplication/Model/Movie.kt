package com.example.movieapplication.Model

data class Movie(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val photoLinks: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
    val year: Int = 0
)