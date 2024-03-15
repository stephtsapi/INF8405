package com.example.polymaps.database

import android.util.Log
import com.example.polymaps.DetectedDevice
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class DatabaseManager private constructor() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private val userId: String? = user?.uid
    private val favoritesRef: DatabaseReference = database.reference.child("users").child(userId ?: "").child("favorites")

    companion object {
        @Volatile
        private var instance: DatabaseManager? = null

        fun getInstance(): DatabaseManager =
            instance ?: synchronized(this) {
                instance ?: DatabaseManager().also { instance = it }
            }
    }

    fun getFavoriteDevices(listener: (List<DetectedDevice>) -> Unit) {

        favoritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val devices = mutableListOf<DetectedDevice>()
                for (deviceSnapshot in dataSnapshot.children) {
                    val device = deviceSnapshot.getValue(DetectedDevice::class.java)
                    device?.let {
                        it.getLatLng { latLng ->
                            if (latLng != null) {
                                it.position = latLng
                            }
                            devices.add(it)
                            if (devices.size == dataSnapshot.childrenCount.toInt()) {
                                listener(devices)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("DatabaseManager", error.message)
            }
        })
    }

    fun addToFavorites(device: DetectedDevice) {
        val deviceRef = favoritesRef.child(device.macAddress)
        deviceRef.setValue(DetectedDevice(device.name, device.macAddress, device.position, device.type, device.distance, device.isFavorite))
    }

    fun removeFromFavorites(device: DetectedDevice) {
        val deviceRef = favoritesRef.child(device.macAddress)
        deviceRef.removeValue()
    }

}
