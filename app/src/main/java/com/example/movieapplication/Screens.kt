package com.example.movieapplication

sealed class Screens(val screen: String) {
    data object  List : Screens("list")
    data object  Search : Screens("search")
    data object  Favorite : Screens("favorite")
    data object  Profile : Screens("profile")
}