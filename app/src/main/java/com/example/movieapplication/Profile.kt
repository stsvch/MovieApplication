package com.example.movieapplication

import android.graphics.BitmapFactory
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movieapplication.ui.theme.Pink40
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun Profile() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var isEditing by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var about by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }

    var gender by remember { mutableStateOf("") }
    var favoriteGenre by remember { mutableStateOf("") }
    var reviewCount by remember { mutableStateOf("0") }
    var regDate by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }

    val darkPink = Color(0xFFAD1457)

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = loadBitmapFromUri(context, it)
            bitmap?.let { bmp ->
                isUploading = true
                val base64Image = bitmapToBase64(bmp)

                uploadImageToImgBB(base64Image, "50245cd843c0c88936aeb23dda809ff0",
                    onSuccess = { newUrl ->
                        photoUrl = newUrl
                        if (userId != null) {
                            db.collection("userInfo").document(userId).update("photoUrl", newUrl)
                        }
                        isUploading = false
                    },
                    onError = { errorMsg ->
                        isUploading = false
                    }
                )
            }
        }
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        name = document.getString("name") ?: ""
                        email = document.getString("email") ?: ""
                    }
                }
            db.collection("userInfo").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        about = document.getString("about") ?: ""
                        phone = document.getString("phone") ?: ""
                        birthday = document.getString("birthday") ?: ""
                        gender = document.getString("gender") ?: ""
                        favoriteGenre = document.getString("favoriteGenre") ?: ""
                        reviewCount = document.getString("ReviewCount") ?: "0"
                        regDate = document.getString("regDate") ?: ""
                        photoUrl = document.getString("photoUrl") ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            isLoading = false
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
                    .data(if (photoUrl.isNotEmpty()) photoUrl else null)
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

                    val data = hashMapOf(
                        "about" to about,
                        "phone" to phone,
                        "birthday" to birthday,
                        "gender" to gender,
                        "favoriteGenre" to favoriteGenre,
                        "ReviewCount" to reviewCount,
                        "regDate" to regDate,
                        "photoUrl" to photoUrl
                    )
                    if (userId != null) {
                        db.collection("userInfo").document(userId).update(data as Map<String, Any>)
                    }
                }
                isEditing = !isEditing
            }) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (isEditing) "Save" else "Edit",
                    tint = Pink40
                )
            }
        }

        ProfileField(label = "Name", value = name, onValueChange = { }, isEditable = false)
        ProfileField(label = "Email", value = email, onValueChange = { }, isEditable = false)
        ProfileField(label = "About", value = about, onValueChange = { about = it }, isEditable = isEditing)
        ProfileField(label = "Phone", value = phone, onValueChange = { phone = it }, isEditable = isEditing)
        ProfileField(label = "Birthday", value = birthday, onValueChange = { birthday = it }, isEditable = isEditing)

        Text(text = "Gender", fontSize = 14.sp, color = Color.Gray)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "M",
                fontSize = 16.sp,
                modifier = Modifier
                    .clickable { gender = "M" }
                    .background(if (gender == "M") Pink40 else Color.Transparent)
                    .padding(8.dp),
                color = if (gender == "M") Color.White else Color.Black
            )

            Text(
                text = "F",
                fontSize = 16.sp,
                modifier = Modifier
                    .clickable { gender = "F" }
                    .background(if (gender == "F") Pink40 else Color.Transparent)
                    .padding(8.dp),
                color = if (gender == "F") Color.White else Color.Black
            )
        }

        ProfileField(label = "Favorite Genre", value = favoriteGenre, onValueChange = { favoriteGenre = it }, isEditable = isEditing)
        ProfileField(label = "Registration Date", value = regDate, onValueChange = { }, isEditable = false)

        Text(
            text = "Reviews: $reviewCount",
            fontSize = 16.sp,
            color = Pink40,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                //
                //
                //
            }
        )

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                //
                //
                //
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = darkPink)
        ) {
            Text(text = "Sign Out", color = Color.White)
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
