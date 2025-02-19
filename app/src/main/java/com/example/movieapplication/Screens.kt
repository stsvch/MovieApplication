package com.example.movieapplication

sealed class Screens(val screen: String) {
    data object  List : Screens("list")
    data object  Search : Screens("search")
    data object  Favorite : Screens("favorite")
    data object  Profile : Screens("profile")
    object MovieDetail : Screens("movie_detail/{movieId}") {
        fun createRoute(movieId: String) = "movie_detail/$movieId"
    }
    data object UserReviews : Screens("user_reviews")
}