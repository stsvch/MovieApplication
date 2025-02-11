package com.example.movieapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movieapplication.ui.theme.Pink40
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen(
                onRegisterSuccess = {
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                    // Переход на MainActivity после успешной регистрации и сохранения данных
                    startActivity(Intent(this, MainActivity::class.java))
                    finish() // Закрываем RegisterActivity
                },
                onRegisterFail = { message ->
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onRegisterFail: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }

    val regDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val defaultPhotoUrl = "https://ibb.co/q3b835dC"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Sign Up", fontSize = 32.sp, color = Pink40)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (name.text.isNotEmpty() && email.text.isNotEmpty() && password.text.isNotEmpty()) {
                        auth.createUserWithEmailAndPassword(email.text, password.text)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        val userData = hashMapOf(
                                            "name" to name.text,
                                            "email" to email.text
                                        )
                                        db.collection("users").document(userId)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                val userInfoData = hashMapOf(
                                                    "about" to "",
                                                    "phone" to "",
                                                    "birthday" to "",
                                                    "gender" to "",
                                                    "favoriteGenre" to "",
                                                    "reviewCount" to "0",
                                                    "regDate" to regDate,
                                                    "photoUrl" to defaultPhotoUrl
                                                )
                                                db.collection("userInfo").document(userId)
                                                    .set(userInfoData)
                                                    .addOnSuccessListener {
                                                        onRegisterSuccess()
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        onRegisterFail(exception.message ?: "Error creating userInfo")
                                                    }
                                            }
                                            .addOnFailureListener { exception ->
                                                onRegisterFail(exception.message ?: "Error creating users entry")
                                            }
                                    } else {
                                        onRegisterFail("User ID is null")
                                    }
                                } else {
                                    onRegisterFail(task.exception?.message ?: "Registration failed")
                                }
                            }
                    } else {
                        onRegisterFail("All fields must be filled")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Pink40),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Sign Up", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Preview
@Composable
fun PreviewRegisterScreen() {
    RegisterScreen(
        onRegisterSuccess = {},
        onRegisterFail = {}
    )
}
