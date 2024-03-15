package com.example.polymaps

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.MarkerOptions

class MapViewModel : ViewModel() {
    private val TAG = "MapViewModel"

    private val _markers = MutableLiveData<List<MarkerOptions>>()
    val markers: LiveData<List<MarkerOptions>> = _markers

    private val _deviceLocations = MutableLiveData<List<LatLng>>()
    val deviceLocations: LiveData<List<LatLng>>
        get() = _deviceLocations

    fun addDeviceLocation(deviceName: String, latLng: LatLng) {
        val deviceLocations = _deviceLocations.value.orEmpty()
        if (!deviceLocations.contains(latLng)) {
            _deviceLocations.value = deviceLocations.plus(latLng)
            addMarkerToMap(deviceName, latLng)
        } else {
            Log.i(TAG, "Device location already exists at $latLng")
        }
    }

    private fun addMarkerToMap(deviceName: String, latLng: LatLng) {
        val markerLatLng = com.google.android.gms.maps.model.LatLng(latLng.latitude, latLng.longitude)
        val markerOptions = MarkerOptions().position(markerLatLng).title(deviceName)
        _markers.value = markers.value.orEmpty().plus(markerOptions)

        Log.i(TAG, "Marker added at $latLng!")
    }

    fun clearMap() {
        _markers.value = emptyList()
        _deviceLocations.value = emptyList()
    }
}
