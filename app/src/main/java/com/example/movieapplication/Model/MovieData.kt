package com.example.movieapplication.Model

data class MovieData(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val photoLinks: List<String> = emptyList(),
    val year: Int = 0,
    val category: String = "",
    val duration: Int = 0,
    val actors: List<Actor> = emptyList(),
    val directors: List<Director> = emptyList(),
    val genres: List<Genre> = emptyList()
)

data class Actor(
    val id: String = "",
    val name: String = ""
)

data class Director(
    val id: String = "",
    val name: String = ""
)

