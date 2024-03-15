package com.example.polymaps.screens

import android.R
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDelegate
import com.example.polymaps.auth.AuthManager
import com.example.polymaps.databinding.FragmentSettingsBinding
import com.example.polymaps.utils.isNightModeActive
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SettingsFragment : Fragment() {
    private lateinit var spinner: Spinner
    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val authManager = AuthManager()

    companion object {
        val languages = arrayOf("English", "Français")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = authManager.getCurrentUser()
        if (user != null) {
            binding.userEmail.text = user.email
            binding.userEmailContainer.visibility = View.VISIBLE
        } else {
            binding.userEmailContainer.visibility = View.GONE
        }

        setupLogoutButtonListener()

        val switch = binding.buttonTheme
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        switch.isChecked = isNightModeActive(requireContext())

        spinner = binding.spinner
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        val defaultPosition = if (Locale.getDefault().language == "en") 0 else 1
        spinner.setSelection(defaultPosition)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (parent?.getItemAtPosition(position).toString()) {
                    "English" -> {
                        if (Locale.getDefault().language != "en") {
                            setLocal(requireActivity(), "en")
                            requireActivity().recreate()
                        }
                    }
                    "Français" -> {
                        if (Locale.getDefault().language != "fr") {
                            setLocal(requireActivity(), "fr")
                            requireActivity().recreate()
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupLogoutButtonListener() {
        binding.logoutButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(requireContext().getString(com.example.polymaps.R.string.log_out))
                .setMessage(requireContext().getString(com.example.polymaps.R.string.log_out_sure))
                .setPositiveButton(requireContext().getString(com.example.polymaps.R.string.log_out)) { _, _ ->
                    authManager.signOut()
                    navigateToLoginFragment()
                }
                .setNegativeButton(requireContext().getString(com.example.polymaps.R.string.cancel), null)
                .show()
        }
    }

    private fun navigateToLoginFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(com.example.polymaps.R.id.flFragment, LoginFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(com.example.polymaps.R.id.bottomNavigationView)
        bottomNavigationView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setLocal(activity: Activity, langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val resources = activity.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }


}