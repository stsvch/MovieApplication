package com.example.movieapplication

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movieapplication.ViewModel.ProfileViewModel
import com.example.movieapplication.ui.theme.Pink40
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Profile(navController: NavHostController) {
    val viewModel: ProfileViewModel = viewModel()
    val profile by viewModel.profile
    val isLoading by viewModel.isLoading
    val isEditing by viewModel.isEditing
    val isUploading by viewModel.isUploading
    val genresList by viewModel.genresList
    val context = LocalContext.current

    var selectedGenreIds by remember { mutableStateOf(profile.favoriteGenres) }

    val imagePickerLauncher = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = loadBitmapFromUri(context, it)
            bitmap?.let { bmp ->
                viewModel.uploadPhoto(bmp)
            }
        }
    }

    if (isLoading || isUploading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(if (profile.photoUrl.isNotEmpty()) profile.photoUrl else "https://via.placeholder.com/120")
                    .crossfade(true)
                    .build(),
                contentDescription = "User Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable { imagePickerLauncher.launch("image/*") }
            )
            IconButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .background(Pink40, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Change Photo",
                    tint = Color.White
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "User Profile", fontSize = 24.sp, color = Pink40)
            IconButton(onClick = {
                if (isEditing) {
                    viewModel.updateFavoriteGenres(selectedGenreIds)
                    viewModel.saveProfile { errorMsg ->
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    viewModel.toggleEditing()
                }
            }) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (isEditing) "Save" else "Edit",
                    tint = Pink40
                )
            }
        }

        ProfileField(label = "Name", value = profile.name, onValueChange = { }, isEditable = false)
        ProfileField(label = "Email", value = profile.email, onValueChange = { }, isEditable = false)

        ProfileField(label = "About", value = profile.about, onValueChange = { viewModel.updateAbout(it) }, isEditable = isEditing)
        ProfileField(label = "Phone", value = profile.phone, onValueChange = { newPhone ->
            viewModel.updatePhone(newPhone)
        }, isEditable = isEditing)

        ProfileField(label = "Birthday", value = profile.birthday, onValueChange = { newBirthday ->
            viewModel.updateBirthday(newBirthday)
        }, isEditable = isEditing)

        Text(text = "Gender", fontSize = 14.sp, color = Color.Gray)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val maleModifier = if (isEditing) Modifier.clickable { viewModel.updateGender("M") } else Modifier
            Text(
                text = "M",
                fontSize = 16.sp,
                modifier = maleModifier
                    .background(if (profile.gender == "M") Color.Blue else Color.Transparent)
                    .padding(8.dp),
                color = if (profile.gender == "M") Color.White else Color.Black
            )
            val femaleModifier = if (isEditing) Modifier.clickable { viewModel.updateGender("F") } else Modifier
            Text(
                text = "F",
                fontSize = 16.sp,
                modifier = femaleModifier
                    .background(if (profile.gender == "F") Pink40 else Color.Transparent)
                    .padding(8.dp),
                color = if (profile.gender == "F") Color.White else Color.Black
            )
        }

        if (isEditing) {
            var showGenreDialog by remember { mutableStateOf(false) }

            LaunchedEffect(genresList) {
                println("Genres list updated: ${genresList.size} items")
                genresList.forEach { println("Genre: ${it.id} - ${it.name}") }
            }

            Column {
                Text("Select favorite genres:", color = Color.Gray)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genresList.forEach { genre ->
                        val isSelected = selectedGenreIds.contains(genre.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedGenreIds = if (isSelected) {
                                    selectedGenreIds - genre.id
                                } else {
                                    selectedGenreIds + genre.id
                                }
                            },
                            label = { Text(genre.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Pink40,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        } else {
            val favoriteGenresNames = genresList
                .filter { genre -> profile.favoriteGenres.contains(genre.id) }
                .joinToString(", ") { it.name }

            ProfileField(
                label = "Favorite Genres",
                value = favoriteGenresNames,
                onValueChange = { },
                isEditable = false
            )
        }

        ProfileField(label = "Registration Date", value = profile.regDate, onValueChange = { }, isEditable = false)

        Text(
            text = "Reviews: ${profile.reviewCount}",
            fontSize = 16.sp,
            color = Pink40,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                navController.navigate(Screens.UserReviews.screen)
            }
        )

        Button(
            onClick = {
                viewModel.signOut()
                context.startActivity(Intent(context, LoginActivity::class.java))
                (context as? Activity)?.finish()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAD1457))
        ) {
            Text(text = "Sign Out", color = Color.White)
        }

        Button(
            onClick = {
                viewModel.deleteAccount { success ->
                    if (success) {
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        (context as? Activity)?.finish()
                    } else {
                        Toast.makeText(context, "Account deletion error. Please reauthenticate.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(text = "Delete account", color = Color.White)
        }

    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditable: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        if (isEditable) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = value,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0))
                    .padding(8.dp)
            )
        }
    }
}