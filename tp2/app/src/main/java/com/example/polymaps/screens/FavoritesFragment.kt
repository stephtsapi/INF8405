package com.example.polymaps.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import com.example.polymaps.*
import com.example.polymaps.databinding.FragmentFavoritesBinding
import com.example.polymaps.utils.CustomListAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView


class FavoritesFragment : Fragment(), FavoritesDevicesListener {

    private var _binding: FragmentFavoritesBinding? = null
    private lateinit var listAdapter: CustomListAdapter
    private lateinit var listView: ListView

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = setFavoriteDevices();

        // Fonction appelée lorsque l'utilisateur clique sur le bouton "Add to/Remove from favorites" dans le dialogue
        val onAddOrRemoveFavoriteClickListener = { position: Int ->
            val selectedItem = listAdapter.getItem(position)
            FavoritesDevices.addOrRemoveFromList(selectedItem, this, requireContext())
        }

        // Fonction appelée lorsque l'utilisateur clique sur le bouton "Share" dans le dialogue
        val onShareClickListener = { position: Int ->
            DetectedDevices.shareDeviceInformation(listAdapter.getItem(position), requireActivity(), requireContext())
        }

        val onHowToGoClickListener = { position: Int ->
            DetectedDevices.showDirectionsOnGoogleMaps(listAdapter.getItem(position), requireActivity())
        }

        listView.onItemClickListener =  AdapterView.OnItemClickListener { _, _, position, _ ->

            CustomDialogHelper.buildCustomDialog(
                requireContext(),
                listAdapter.devices,
                position,
                onAddOrRemoveFavoriteClickListener,
                onShareClickListener,
                onHowToGoClickListener)
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.visibility = View.VISIBLE
    }

    private fun setFavoriteDevices(): ListView {
        val favoritesListView = view?.findViewById<ListView>(R.id.favorite_devices)
        val emptyTextView = view?.findViewById<TextView>(R.id.no_favorite_list)

        try {
            FavoritesDevices.getFavoriteList(requireContext()) { list ->
                listAdapter = CustomListAdapter(requireContext(), list as ArrayList<DetectedDevice>)
                favoritesListView?.adapter = listAdapter

                if (list.isEmpty()) {
                    emptyTextView?.visibility = View.VISIBLE
                    favoritesListView?.visibility = View.GONE
                } else {
                    emptyTextView?.visibility = View.GONE
                    favoritesListView?.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return favoritesListView!!
    }


    override fun onFavoritesChanged() {
        FavoritesDevices.getFavoriteList(requireContext()) { list ->
            listAdapter.updateDevices(list)

            val emptyTextView = view?.findViewById<TextView>(R.id.no_favorite_list)
            val favoritesListView = view?.findViewById<ListView>(R.id.favorite_devices)

            if (list.isEmpty()) {
                emptyTextView?.visibility = View.VISIBLE
                favoritesListView?.visibility = View.GONE
            } else {
                emptyTextView?.visibility = View.GONE
                favoritesListView?.visibility = View.VISIBLE
            }
        }
    }
}