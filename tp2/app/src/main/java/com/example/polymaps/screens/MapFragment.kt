package com.example.polymaps.screens

import CustomDialogHelper
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.polymaps.*
import com.example.polymaps.DetectedDevices.Companion.setListAdapter
import com.example.polymaps.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import android.Manifest
import android.content.Context
import android.content.res.Configuration
import com.example.polymaps.utils.CustomListAdapter
import com.example.polymaps.utils.isNightModeActive
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomnavigation.BottomNavigationView


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MapFragment : Fragment(), OnMapReadyCallback, OnMarkerClickListener, FavoritesDevicesListener {

    private var _binding: FragmentMapBinding? = null
    private lateinit var mMap: GoogleMap
    private lateinit var mapViewModel: MapViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListAdapter(requireContext())

        // Fonction appelée lorsque l'utilisateur clique sur le bouton "Add to/Remove from favorites" dans le dialogue
        val onAddOrRemoveFavoriteClickListener = { position: Int ->
//            FavoritesDevices.addOrRemoveFromList(DetectedDevices.getDeviceList()[position], DetectedDevices.getListAdapter(), requireContext())
            FavoritesDevices.addOrRemoveFromList(DetectedDevices.getDeviceList()[position], this)
        }


        // Fonction appelée lorsque l'utilisateur clique sur le bouton "Share" dans le dialogue
        val onShareClickListener = { position: Int ->
            DetectedDevices.shareDeviceInformation(DetectedDevices.getDeviceList()[position], requireActivity(), requireContext())
        }

        val openGoogleMapsClickListener = { position: Int ->
            DetectedDevices.showDirectionsOnGoogleMaps(DetectedDevices.getDeviceList()[position], requireActivity())
        }

        binding.detectedDeviceContainer.adapter = DetectedDevices.getListAdapter()
        binding.detectedDeviceContainer.onItemClickListener =  AdapterView.OnItemClickListener { _, _, position, _ ->
            CustomDialogHelper.buildCustomDialog(requireContext(), DetectedDevices.getDeviceList(), position,
                onAddOrRemoveFavoriteClickListener,
                onShareClickListener,
                openGoogleMapsClickListener)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.visibility = View.VISIBLE
    }

    override fun onFavoritesChanged() {
        // Update the UI to reflect the changes in the favorites list
        DetectedDevices.getListAdapter().notifyDataSetChanged()
    }


    // TODO: Clean this stuff...
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (isNightModeActive(requireContext())) {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_dark))
        } else {
            googleMap.setMapStyle(null)
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // request the permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        // enable the location layer and move the camera to the current location
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 19f))
            }
        }
        mMap.setOnMarkerClickListener(this)

        observeDeviceLocations()
    }

    private fun observeDeviceLocations() {
        mapViewModel.deviceLocations.observe(this) { deviceLocations -> updateMap() }
    }

    private fun updateMap() {
        if (::mMap.isInitialized) {
            mMap.clear()
            // mapViewModel.clearMap() // clear existing markers

            // Add markers to map
            mapViewModel.markers.value?.forEach { markerOptions ->
                mMap.addMarker(markerOptions)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val deviceList = DetectedDevices.getDeviceList()
        val selectedItem = deviceList.find {
            LatLng(it.position.latitude, it.position.longitude) == marker.position && it.name == marker.title
        }

        if (selectedItem != null) {
            CustomDialogHelper.buildCustomDialog(
                context = requireContext(),
                list = deviceList,
                position = deviceList.indexOf(selectedItem),
                onAddOrRemoveFavoriteClicked = { position -> toggleFavorite(position) },
                onShareClicked = { position -> shareDeviceInformation(position, requireContext()) },
                onHowtoGoCLicked = { position -> showDirectionsOnGoogleMaps(position) },
            )
        }
        return true
    }

    private fun toggleFavorite(position: Int) {
//        FavoritesDevices.addOrRemoveFromList(DetectedDevices.getDeviceList()[position], DetectedDevices.getListAdapter(), requireContext())
        FavoritesDevices.addOrRemoveFromList(DetectedDevices.getDeviceList()[position], this)
        DetectedDevices.getListAdapter().notifyDataSetChanged()
    }

    private fun shareDeviceInformation(position: Int, context: Context) {
        DetectedDevices.shareDeviceInformation(DetectedDevices.getDeviceList()[position], requireActivity(), context)
    }

    private fun showDirectionsOnGoogleMaps(position: Int) {
        DetectedDevices.showDirectionsOnGoogleMaps(DetectedDevices.getDeviceList()[position], requireActivity())
    }

}