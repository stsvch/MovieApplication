package com.example.movieapplication.Repository

import android.graphics.Bitmap
import com.example.movieapplication.Model.Genre
import com.example.movieapplication.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.example.movieapplication.bitmapToBase64
import com.example.movieapplication.uploadImageToImgBB

class ProfileRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
    }

    fun loadUserProfile(
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onFailure(Exception("User not authenticated"))
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val name = userDoc.getString("name") ?: ""
                val email = userDoc.getString("email") ?: ""
                db.collection("userInfo").document(userId).get()
                    .addOnSuccessListener { infoDoc ->
                        val about = infoDoc.getString("about") ?: ""
                        val phone = infoDoc.getString("phone") ?: ""
                        val birthday = infoDoc.getString("birthday") ?: ""
                        val gender = infoDoc.getString("gender") ?: ""
                        val favoriteGenres = infoDoc.get("favoriteGenres") as? List<String> ?: emptyList<String>()
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
                        onSuccess(profile)
                    }
                    .addOnFailureListener { e -> onFailure(e) }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun updateUserProfile(profile: User, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
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
        db.collection("userInfo").document(userId).update(data as Map<String, Any>)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun loadGenres(
        onSuccess: (List<Genre>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("genres").get()
            .addOnSuccessListener { snapshot ->
                val genres = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name")
                    if (name != null) {
                        Genre(id = doc.id, name = name)
                    } else {
                        null
                    }
                }
                onSuccess(genres)
            }
            .addOnFailureListener { e -> onFailure(e) }
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

    fun signOut(onComplete: () -> Unit) {
        auth.signOut()
        onComplete()
    }

    fun deleteAccount(onComplete: (Boolean) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid
        if (userId == null) {
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
            .get()
            .addOnSuccessListener { reviewsSnapshot ->
                reviewsSnapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                db.collection("scores")
                    .whereEqualTo("user", userId)
                    .get()
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
                    .addOnFailureListener {
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                onComplete(false)
            }
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