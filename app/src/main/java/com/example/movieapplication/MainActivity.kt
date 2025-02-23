package com.example.movieapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.movieapplication.ui.theme.MovieApplicationTheme
import com.example.movieapplication.ui.theme.Pink40
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

data class Moviedb(
    val actors: List<String> = emptyList(),
    val category: String = "",
    val title: String = "",
    val description: String = "",
    val directors: List<String> = emptyList(),
    val duration : Int = 0,
    val photoLinks: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
    val year: Int = 0
)

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener

    fun fillFirestoreCollections() {
        val db = FirebaseFirestore.getInstance()
       val movie1 = Moviedb(
            actors = listOf("71", "72", "73", "74", "75", "76", "77", "78", "79", "80"),
            category = "movie",
            description = "1942, Great Britain. They are the best of the best. Inveterate adventurers and first-class specialists who are used to acting alone. But when the fate of the entire world is at stake, they must team up in a top-secret combat unit and embark on a daring mission against the Nazis. Now their business is war, and they will conduct it in a completely ungentlemanly way.",
            directors = listOf("8"),
            duration = 119,
            genres = listOf("5", "7", "10", "11"),
            photoLinks = listOf(
                "https://i.ibb.co/HLn4CmJ2/2025-02-11-00-01-24.png",
            ),
            title = "The Ministry of Ungentlemanly Warfare",
            year = 2024
        )

        db.collection("movies").document("16").set(movie1)
        val movie2 = Moviedb(
            actors = listOf("71", "72", "73", "74", "75", "76", "77", "78", "79", "80"),
            category = "movie",
            description = "1942, Great Britain. They are the best of the best. Inveterate adventurers and first-class specialists who are used to acting alone. But when the fate of the entire world is at stake, they must team up in a top-secret combat unit and embark on a daring mission against the Nazis. Now their business is war, and they will conduct it in a completely ungentlemanly way.",
            directors = listOf("8"),
            duration = 119,
            genres = listOf("5", "7", "10", "11"),
            photoLinks = listOf(
                "https://i.ibb.co/HLn4CmJ2/2025-02-11-00-01-24.png",
            ),
            title = "The Ministry of Ungentlemanly Warfare",
            year = 2024
        )

        db.collection("movies").document("16").set(movie2)
        val movie = Moviedb(
            actors = listOf("71", "72", "73", "74", "75", "76", "77", "78", "79", "80"),
            category = "movie",
            description = "1942, Great Britain. They are the best of the best. Inveterate adventurers and first-class specialists who are used to acting alone. But when the fate of the entire world is at stake, they must team up in a top-secret combat unit and embark on a daring mission against the Nazis. Now their business is war, and they will conduct it in a completely ungentlemanly way.",
            directors = listOf("8"),
            duration = 119,
            genres = listOf("5", "7", "10", "11"),
            photoLinks = listOf(
                "https://i.ibb.co/HLn4CmJ2/2025-02-11-00-01-24.png",
            ),
            title = "The Ministry of Ungentlemanly Warfare",
            year = 2024
        )

        db.collection("movies").document("16").set(movie)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        lifecycleScope.launch(Dispatchers.IO) {
            PhotoCacheManager.preloadAllPhotos()
        }
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        firestore.firestoreSettings = settings

        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        /*
        lifecycleScope.launch(Dispatchers.IO) {
            fillFirestoreCollections()
        }
*/
        enableEdgeToEdge()
        setContent {
            MovieApplicationTheme {
                Scaffold(modifier = Modifier.padding(0.dp)) { innerPadding ->
                    BottomNav(innerPadding)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authListener)
    }
}



@Composable
fun BottomNav(innerPadding: androidx.compose.foundation.layout.PaddingValues) {
    val navController: NavHostController = rememberNavController()
    val selected = remember { mutableStateOf(Screens.List.screen) }

    Scaffold(
        bottomBar = {
            BottomAppBar(containerColor = Pink40) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            selected.value = Screens.List.screen
                            navController.navigate(Screens.List.screen) {
                                popUpTo(Screens.List.screen) { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "List",
                            modifier = Modifier.size(26.dp),
                            tint = if (selected.value == Screens.List.screen) Color.White else Color.DarkGray
                        )
                    }
                    IconButton(
                        onClick = {
                            selected.value = Screens.Search.screen
                            navController.navigate(Screens.Search.screen) {
                                popUpTo(Screens.List.screen)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(26.dp),
                            tint = if (selected.value == Screens.Search.screen) Color.White else Color.DarkGray
                        )
                    }
                    IconButton(
                        onClick = {
                            selected.value = Screens.Favorite.screen
                            navController.navigate(Screens.Favorite.screen) {
                                popUpTo(Screens.List.screen)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            modifier = Modifier.size(26.dp),
                            tint = if (selected.value == Screens.Favorite.screen) Color.White else Color.DarkGray
                        )
                    }
                    IconButton(
                        onClick = {
                            selected.value = Screens.Profile.screen
                            navController.navigate(Screens.Profile.screen) {
                                popUpTo(Screens.List.screen)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(26.dp),
                            tint = if (selected.value == Screens.Profile.screen) Color.White else Color.DarkGray
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screens.List.screen,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screens.List.screen) {
                MovieListScreen(navController = navController)
            }
            composable(Screens.Search.screen) {
                Search(navController = navController)
            }
            composable(Screens.Favorite.screen) { Favorite(navController = navController) }
            composable(Screens.Profile.screen) { Profile(navController = navController) }
            composable(Screens.MovieDetail.screen) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                MovieDetailScreen(movieId = movieId)
            }
            composable(Screens.UserReviews.screen) {
                UserReviewsScreen()
            }
        }
    }
}

@Preview
@Composable
fun PreviewBottomNav() {
    MovieApplicationTheme {
        BottomNav(innerPadding = androidx.compose.foundation.layout.PaddingValues())
    }
}
