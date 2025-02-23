package com.example.movieapplication.Repository

import android.graphics.Bitmap
import android.util.Log
import com.example.movieapplication.Model.Genre
import com.example.movieapplication.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.example.movieapplication.bitmapToBase64
import com.example.movieapplication.uploadImageToImgBB

class ProfileRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private var cachedUserProfile: User? = null

    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
    }

    fun listenToReviewCount(userId: String, onCountUpdate: (Int) -> Unit) {
        db.collection("reviews")
            .whereEqualTo("user", userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    onCountUpdate(snapshot.size())
                }
            }
    }

    fun loadUserProfile(
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: run {
            onFailure(Exception("User not authenticated"))
            return
        }
        cachedUserProfile?.let {
            onSuccess(it)
            return
        }
        db.collection("users").document(userId).get(Source.DEFAULT)
            .addOnSuccessListener { userDoc ->
                val name = userDoc.getString("name") ?: ""
                val email = userDoc.getString("email") ?: ""
                db.collection("userInfo").document(userId).get(Source.DEFAULT)
                    .addOnSuccessListener { infoDoc ->
                        val about = infoDoc.getString("about") ?: ""
                        val phone = infoDoc.getString("phone") ?: ""
                        val birthday = infoDoc.getString("birthday") ?: ""
                        val gender = infoDoc.getString("gender") ?: ""
                        val favoriteGenres = infoDoc.get("favoriteGenres") as? List<String> ?: emptyList()
                        val reviewCount = infoDoc.getString("ReviewCount") ?: "0"
                        val regDate = infoDoc.getString("regDate") ?: ""
                        val photoUrl = infoDoc.getString("photoUrl") ?: ""
                        val profile = User(
                            id = userId,
                            name = name,
                            email = email,
                            about = about,
                            phone = phone,
                            birthday = birthday,
                            gender = gender,
                            favoriteGenres = favoriteGenres,
                            reviewCount = reviewCount,
                            regDate = regDate,
                            photoUrl = photoUrl
                        )
                        cachedUserProfile = profile
                        onSuccess(profile)
                    }
                    .addOnFailureListener { e -> onFailure(e) }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun updateUserProfile(profile: User, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: run {
            onComplete(false)
            return
        }
        val data = hashMapOf(
            "about" to profile.about,
            "phone" to profile.phone,
            "birthday" to profile.birthday,
            "gender" to profile.gender,
            "favoriteGenres" to profile.favoriteGenres,
            "ReviewCount" to profile.reviewCount,
            "regDate" to profile.regDate,
            "photoUrl" to profile.photoUrl
        )
        db.collection("userInfo").document(userId).set(data, SetOptions.merge())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cachedUserProfile = profile
                }
                onComplete(task.isSuccessful)
            }
    }

    fun loadGenres(
        onSuccess: (List<Genre>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("genres").get(Source.DEFAULT)
            .addOnSuccessListener { snapshot ->
                val genres = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name")
                    if (name != null) Genre(id = doc.id, name = name) else null
                }
                onSuccess(genres)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun signOut(onComplete: () -> Unit) {
        try {
            auth.signOut()
            if (auth.currentUser == null) {
                Log.d("Auth", "User signed out successfully.")
            } else {
                Log.d("Auth", "Sign out did not clear the user.")
            }
        } catch (e: Exception) {
            Log.e("Auth", "Error signing out", e)
        }
        onComplete()
    }


    fun deleteAccount(onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: run {
            onComplete(false)
            return
        }
        val batch = db.batch()
        val userDocRef = db.collection("users").document(userId)
        val userInfoDocRef = db.collection("userInfo").document(userId)
        batch.delete(userDocRef)
        batch.delete(userInfoDocRef)
        db.collection("reviews")
            .whereEqualTo("user", userId)
            .get(Source.DEFAULT)
            .addOnSuccessListener { reviewsSnapshot ->
                reviewsSnapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                db.collection("scores")
                    .whereEqualTo("user", userId)
                    .get(Source.DEFAULT)
                    .addOnSuccessListener { scoresSnapshot ->
                        scoresSnapshot.documents.forEach { doc ->
                            batch.delete(doc.reference)
                        }
                        batch.commit().addOnCompleteListener { batchTask ->
                            if (batchTask.isSuccessful) {
                                auth.currentUser?.delete()?.addOnCompleteListener { authTask ->
                                    if (authTask.isSuccessful) {
                                        auth.signOut()
                                        onComplete(true)
                                    } else {
                                        onComplete(false)
                                    }
                                }
                            } else {
                                onComplete(false)
                            }
                        }
                    }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun uploadPhoto(bitmap: Bitmap, onComplete: (String) -> Unit) {
        val base64Image = bitmapToBase64(bitmap)
        uploadImageToImgBB(
            imageBase64 = base64Image,
            apiKey = "50245cd843c0c88936aeb23dda809ff0",
            onSuccess = { url ->
                onComplete(url ?: "")
            },
            onError = {
                onComplete("")
            }
        )
    }
}
