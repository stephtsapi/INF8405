package com.example.polymaps.screens

import com.example.polymaps.auth.AuthViewModel
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.polymaps.R
import com.example.polymaps.auth.AuthManager
import com.example.polymaps.auth.AuthViewModelFactory
import com.example.polymaps.databinding.FragmentLoginBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: AuthViewModel
    private val authManager = AuthManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        setupSignUpButtonListener()
        setupLoginButtonListener()

        return binding.root

    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.visibility = View.GONE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, AuthViewModelFactory(requireActivity().application, authManager)).get(
            AuthViewModel::class.java)

    }

    private fun setupSignUpButtonListener() {
        binding.buttonSignUp.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment, SignUpFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupLoginButtonListener() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                binding.editTextEmail.error = "Please enter email"
                binding.editTextPassword.error = "Please enter password"
                return@setOnClickListener
            }

            viewModel.signIn(email, password,
                onSuccess = {
                    navigateToMapFragment()
                },
                onFailure = { errorMessage ->
                    binding.editTextEmail.error = errorMessage
                    binding.editTextPassword.error = errorMessage
                }
            )
        }
    }

    private fun navigateToMapFragment() {
        requireActivity().supportFragmentManager.popBackStack()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, MapFragment())
            .commit()
    }
}