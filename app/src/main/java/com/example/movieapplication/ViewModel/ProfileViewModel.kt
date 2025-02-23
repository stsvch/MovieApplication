package com.example.movieapplication.ViewModel

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.example.movieapplication.Model.Genre
import com.example.movieapplication.Model.User
import com.example.movieapplication.Repository.ProfileRepository

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _profile = mutableStateOf(User())
    val profile: State<User> = _profile

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _isEditing = mutableStateOf(false)
    val isEditing: State<Boolean> = _isEditing

    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading

    private val _genresList = mutableStateOf(listOf<Genre>())
    val genresList: State<List<Genre>> = _genresList

    init {
        loadProfile()
        loadGenres()
    }

    fun loadProfile() {
        _isLoading.value = true
        repository.loadUserProfile(
            onSuccess = { userProfile ->
                _profile.value = userProfile
                _isLoading.value = false
                repository.listenToReviewCount(userProfile.id) { count ->
                    _profile.value = _profile.value.copy(reviewCount = count.toString())
                }
            },
            onFailure = {
                _isLoading.value = false
            }
        )
    }

    fun toggleEditing() {
        _isEditing.value = !_isEditing.value
    }

    fun saveProfile(onError: (String) -> Unit) {
        val currentProfile = _profile.value
        val phoneRegex = Regex("^[0-9+\\-() ]+\$")
        if (!phoneRegex.matches(currentProfile.phone)) {
            onError("Incorrect phone format")
            return
        }
        try {
            val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
            sdf.isLenient = false
            val date = sdf.parse(currentProfile.birthday)
            if (date.after(java.util.Date())) {
                onError("The date cannot be in the future")
                return
            }
        } catch (e: Exception) {
            onError("Incorrect date format. Use dd.mm.yyyy")
            return
        }
        repository.updateUserProfile(currentProfile) { success ->
            if (success) {
                _isEditing.value = false
            } else {
                onError("Error saving the profile")
            }
        }
    }

    fun updateAbout(newAbout: String) {
        _profile.value = _profile.value.copy(about = newAbout)
    }

    fun updatePhone(newPhone: String) {
        _profile.value = _profile.value.copy(phone = newPhone)
    }

    fun updateBirthday(newBirthday: String) {
        _profile.value = _profile.value.copy(birthday = newBirthday)
    }

    fun loadGenres() {
        repository.loadGenres(
            onSuccess = { genres ->
                _genresList.value = genres
            },
            onFailure = { }
        )
    }

    fun signOut() {
        repository.signOut { }
    }

    fun deleteAccount(onResult: (Boolean) -> Unit) {
        repository.deleteAccount { success ->
            onResult(success)
        }
    }

    fun updateGender(newGender: String) {
        _profile.value = _profile.value.copy(gender = newGender)
    }

    fun updateFavoriteGenres(newGenres: List<String>) {
        _profile.value = _profile.value.copy(favoriteGenres = newGenres)
    }

    fun uploadPhoto(bitmap: Bitmap) {
        _isUploading.value = true
        repository.uploadPhoto(bitmap) { newPhotoUrl ->
            if (newPhotoUrl.isNotEmpty()) {
                _profile.value = _profile.value.copy(photoUrl = newPhotoUrl)
                repository.updateUserProfile(_profile.value) { success ->
                }
            }
            _isUploading.value = false
        }
    }
}
