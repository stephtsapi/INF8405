package com.example.polymaps.auth

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthViewModel(application: Application, private val authManager: AuthManager) : AndroidViewModel(application) {
//    val isLoggingIn = MutableLiveData<Boolean>()

    private val auth = FirebaseAuth.getInstance()

    fun signIn(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        authManager.signIn(email, password, onSuccess, onFailure)
    }

    fun signUp(email: String, password: String, onSuccess: (Any?) -> Unit, onFailure: (String) -> Unit) {
        authManager.signUp(email, password, onSuccess, onFailure)
    }

    fun uploadProfilePhoto(user: FirebaseUser, uri: Uri, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        authManager.uploadProfileImage(user, uri, onSuccess, onFailure)
    }

    fun signOut() {
        authManager.signOut()
    }

    fun getCurrentUser() = authManager.getCurrentUser()

//    private fun getImageBytes(uri: Uri): ByteArray {
//        val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
//        val bufferSize = 1024
//        val buffer = ByteArray(bufferSize)
//        val outputStream = ByteArrayOutputStream()
//        var length: Int
//        while (inputStream?.read(buffer).also { length = it ?: 0 } != -1) {
//            outputStream.write(buffer, 0, length)
//        }
//        return outputStream.toByteArray()
//    }
}
