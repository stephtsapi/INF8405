package com.example.polymaps.auth

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage

class AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference

    fun signIn(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception?.message ?: "An error occurred")
                }
            }
    }

    fun signUp(email: String, password: String, onSuccess: (Any?) -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(onSuccess)
                } else {
                    onFailure(task.exception?.message ?: "An error occurred")
                }
            }
    }

    fun uploadProfileImage(user: FirebaseUser, uri: Uri, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        uri.let {
            val storageRef = storageRef.child("profile_pictures/${user.uid}")
            val uploadTask = storageRef.putFile(uri)

            uploadTask.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setPhotoUri(downloadUri)
                        .build()

                    user.updateProfile(profileUpdates).addOnSuccessListener {
                        onSuccess()
                    }.addOnFailureListener { exception ->
                        onFailure("Failed to update user profile: ${exception.message}")
                    }
                }
            }.addOnFailureListener { exception ->
                onFailure("Failed to upload profile picture: ${exception.message}")
            }
        } ?: onSuccess()
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}
