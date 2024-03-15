package com.example.polymaps.viewmodels


import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class SignUpViewModel(application: Application) :  AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun signUp(email: String, password: String) {
        if (email.isEmpty()) {
            _error.value = "Email cannot be empty"
            return
        }
        if (password.isEmpty()) {
            _error.value = "Password cannot be empty"
            return
        }

        // Perform sign up using FirebaseAuth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _user.postValue(auth.currentUser)
                } else {
                    _error.postValue(task.exception?.message)
                }
            }
    }

//    fun uploadProfileImage(user: FirebaseUser, selectedImageUri: Uri?) {
//        selectedImageUri?.let { uri ->
//            val storageRef = storageRef.child("profile_pictures/${user.uid}")
//            val uploadTask = storageRef.putBytes(getImageBytes(uri))
//
//            uploadTask.addOnSuccessListener {
//                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
//                    val profileUpdates = UserProfileChangeRequest.Builder()
//                        .setPhotoUri(downloadUri)
//                        .build()
//
//                    user.updateProfile(profileUpdates).addOnSuccessListener {
//                        _user.postValue(auth.currentUser)
//                    }.addOnFailureListener { exception ->
//                        _error.postValue("Failed to update user profile: ${exception.message}")
//                    }
//                }
//            }.addOnFailureListener { exception ->
//                _error.postValue("Failed to upload profile picture: ${exception.message}")
//            }
//        } ?: _user.postValue(auth.currentUser)
//    }

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
