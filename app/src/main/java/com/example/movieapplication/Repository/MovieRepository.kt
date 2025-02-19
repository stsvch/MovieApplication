package com.example.movieapplication.Repository

import com.example.movieapplication.Model.Movie
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MovieRepository(private val db: FirebaseFirestore) {

    fun loadMovies(
        genreFilter: String? = null,
        yearFilter: Int? = null,
        onSuccess: (List<Movie>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        var query: Query = db.collection("movies")
        genreFilter?.let {
            query = query.whereArrayContains("genres", it)
        }
        yearFilter?.let {
            query = query.whereEqualTo("year", it)
        }
        query.get()
            .addOnSuccessListener { querySnapshot ->
                val movieList = querySnapshot.documents.map { doc ->
                    Movie(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        photoLinks = doc.get("photoLinks") as? List<String> ?: emptyList(),
                        genres = doc.get("genres") as? List<String> ?: emptyList(),
                        year = doc.getLong("year")?.toInt() ?: 0
                    )
                }
                onSuccess(movieList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getMovieById(
        movieId: String,
        onSuccess: (Movie?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("movies").document(movieId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val movie = Movie(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        photoLinks = doc.get("photoLinks") as? List<String> ?: emptyList(),
                        genres = doc.get("genres") as? List<String> ?: emptyList(),
                        year = doc.getLong("year")?.toInt() ?: 0
                    )
                    onSuccess(movie)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}