package com.example.movieapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.movieapplication.ui.theme.MovieApplicationTheme
import com.example.movieapplication.ui.theme.Pink40

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieApplicationTheme {
                Scaffold(modifier = Modifier.padding(0.dp)) { innerPadding ->
                    BottomNav(innerPadding)
                }
            }
        }
    }
}

@Composable
fun BottomNav(innerPadding: androidx.compose.foundation.layout.PaddingValues) {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current.applicationContext
    // выбранный экран (значение – строка маршрута)
    val selected = remember { mutableStateOf(Screens.List.screen) }

    Scaffold(
        bottomBar = {
            BottomAppBar(containerColor = Pink40) {
                IconButton(
                    onClick = {
                        selected.value = Screens.List.screen
                        navController.navigate(Screens.List.screen) {
                            popUpTo(Screens.List.screen) { inclusive = true }
                        }
                    },
                    modifier = Modifier.weight(1f)
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
                    },
                    modifier = Modifier.weight(1f)
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
                    },
                    modifier = Modifier.weight(1f)
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
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(26.dp),
                        tint = if (selected.value == Screens.Profile.screen) Color.White else Color.DarkGray
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screens.List.screen,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screens.List.screen) { List() }
            composable(Screens.Search.screen) { Search() }
            composable(Screens.Favorite.screen) { Favorite() }
            composable(Screens.Profile.screen) { Profile() }
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
