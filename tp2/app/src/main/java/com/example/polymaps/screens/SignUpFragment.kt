package com.example.polymaps.screens

import android.Manifest
import com.example.polymaps.auth.AuthViewModel
import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.polymaps.R
import com.example.polymaps.auth.AuthManager
import com.example.polymaps.auth.AuthViewModelFactory
import com.example.polymaps.databinding.FragmentSignUpBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.ByteArrayOutputStream
import java.util.*
import com.example.polymaps.utils.showToast
import com.google.firebase.auth.FirebaseUser
import java.io.IOException


class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var authViewModel: AuthViewModel
    private val authManager = AuthManager()
    private var selectedImageUri: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val PERMISSION_REQUEST_CAMERA = 2

    companion object {
        private val TAG = "SignUpFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        authViewModel = ViewModelProvider(this, AuthViewModelFactory(requireActivity().application, authManager)).get(
            AuthViewModel::class.java)

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.visibility = View.GONE

        setupSignUpButton()
        setupUploadProfilePictureButton()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonLogin.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment, LoginFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.visibility = View.GONE
    }

    private fun setupSignUpButton() {
        binding.buttonSignUp.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                binding.editTextEmail.error = "Please enter email"
                binding.editTextPassword.error = "Please enter password"
                return@setOnClickListener
            }

            authViewModel.signUp(email, password,
                onSuccess = {
                    Log.d(TAG, "signUp onSuccess called")
                    selectedImageUri?.let { uri ->
                        authViewModel.uploadProfilePhoto(authManager.getCurrentUser()!!, uri,
                            onSuccess = {
                                Log.d(TAG, "Profile photo uploaded successfully.")
                                navigateToMapFragment()
                            },
                            onFailure = { errorMessage ->
                                // Handle failure
                                Log.e(TAG, "Failed to upload profile photo: $errorMessage")
                            }
                        )
                    } ?: run {
                        navigateToMapFragment()
                    }
                }
            ) { errorMessage ->
                showToast(requireContext(), errorMessage)
            }
        }
    }

    private fun setupUploadProfilePictureButton() {
        binding.ivProfilePicture.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                takePicture()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_REQUEST_CAMERA
                )
            }
        }
    }


    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun navigateToMapFragment() {
        requireActivity().supportFragmentManager.popBackStack()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, MapFragment())
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            val imageBitmap = data.extras?.get("data") as Bitmap?
            if (imageBitmap != null) {
                binding.ivProfilePicture.setImageBitmap(imageBitmap)
                selectedImageUri = getImageUri(requireContext(), imageBitmap)
            } else {
                Log.e(TAG, "Error retrieving image bitmap from camera")
            }
        }
    }

    private fun getImageUri(context: Context, imageBitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            imageBitmap,
            "IMG_" + Calendar.getInstance().timeInMillis,
            null
        )
        return Uri.parse(path)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            val imageBitmap = data?.extras?.get("data") as Bitmap?
//            if (imageBitmap != null) {
//                binding.ivProfilePicture.setImageBitmap(imageBitmap)
//                selectedImageUri = getImageBytes(requireContext(), imageBitmap)
//            } else {
//                Log.e(TAG, "Error retrieving image bitmap from camera")
//            }
//        }
//    }

//    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
//        val bytes = ByteArrayOutputStream()
//        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
//        val path = MediaStore.Images.Media.insertImage(
//            inContext.contentResolver,
//            inImage,
//            "IMG_" + Calendar.getInstance().timeInMillis,
//            null
//        )
//        return Uri.parse(path)
//    }

    private fun getImageBytes(uri: Uri): ByteArray {
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        val outputStream = ByteArrayOutputStream()
        var length: Int
        while (inputStream?.read(buffer).also { length = it ?: 0 } != -1) {
            outputStream.write(buffer, 0, length)
        }
        return outputStream.toByteArray()
    }

}

