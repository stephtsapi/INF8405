package com.example.polymaps.database

import android.content.Context
import android.util.Log
import com.example.polymaps.DetectedDevice
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.database.*
import com.google.gson.Gson
import java.io.*

class DatabaseManager private constructor(private val context: Context) {

//    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
//    private val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
//    private val userId: String? = user?.uid
//    private val favoritesRef: DatabaseReference = database.reference.child("users").child(userId ?: "").child("favorites")

    companion object {
        @Volatile
        private var instance: DatabaseManager? = null
        private var jsonFileName = "favorite_devices.json"

//        fun getInstance(): DatabaseManager =
//            instance ?: synchronized(this) {
//                instance ?: DatabaseManager().also { instance = it }
//            }
        fun getInstance(context: Context): DatabaseManager =
            instance?: synchronized(this) {
                instance ?: DatabaseManager(context.applicationContext).also {instance = it}
            }
    }

    fun getFavoriteDevices(listener: (List<DetectedDevice>) -> Unit) {
        val favoritesDevices = readFromJson(context, "favorite_devices.json")
        listener(favoritesDevices)
    }

    fun addToFavorites(device: DetectedDevice) {
        val favoriteDevices = readFromJson(context, "favorite_devices.json").toMutableList()
        val added = favoriteDevices.add(device)
        Log.d("Favorite devices", "Ça a été ajouté ? $added")
        saveToJson(context, "favorite_devices.json", favoriteDevices)
        Log.d("Favorite devices", "La liste des favoris : $favoriteDevices")

    }

    fun removeFromFavorites(device: DetectedDevice) {
        val favoriteDevices = readFromJson(context, "favorite_devices.json").toMutableList()
//        val removed = favoriteDevices.remove(device)

        val deviceToRemove = listOf(device) // Crée une liste temporaire contenant uniquement le device à supprimer
        val removed = favoriteDevices.removeAll { it.macAddress == device.macAddress }

        saveToJson(context, "favorite_devices.json", favoriteDevices)
        Log.d("Favorite devices", "Le device à retirer : $device")
        Log.d("Favorite devices", "Ça a été retiré ? $removed")
        Log.d("Favorite devices", "La liste des favoris : $favoriteDevices")
    }

    private fun saveToJson(context: Context, fileName: String, data: List<DetectedDevice>) {
        val json = Gson().toJson(data)
        val outputStream: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        outputStream.write(json.toByteArray())
        outputStream.close()
    }

    private fun readFromJson(context: Context, fileName: String): List<DetectedDevice> {
        return try {
            val inputStream: InputStream = context.openFileInput(fileName)
            val json = inputStream.bufferedReader().use {it.readText()}
            Gson().fromJson(json, Array<DetectedDevice>::class.java).toList<DetectedDevice>()
        } catch (e: FileNotFoundException) {
            emptyList()
        }
    }

    fun isFavorite(macAddress: String): Boolean {
        val favoriteDevices = readFromJson(context, "favorite_devices.json")
        return favoriteDevices.any { it.macAddress == macAddress }
    }

}
