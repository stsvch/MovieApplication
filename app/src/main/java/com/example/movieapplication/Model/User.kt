package com.example.movieapplication.Model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val about: String = "",
    val phone: String = "",
    val birthday: String = "",
    val gender: String = "",
    val favoriteGenres: List<String> = emptyList(),
    val reviewCount: String = "0",
    val regDate: String = "",
    val photoUrl: String = ""
)
