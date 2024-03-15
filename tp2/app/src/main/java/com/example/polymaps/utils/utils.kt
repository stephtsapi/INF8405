package com.example.polymaps.utils

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.polymaps.LatLng
import java.util.*
import kotlin.math.*

//Detects if the device has dark theme set to true
//Source: https://stackoverflow.com/questions/41391404/how-to-get-appcompatdelegate-current-mode-if-default-is-auto
fun isNightModeActive(context: Context): Boolean {
    val defaultNightMode = AppCompatDelegate.getDefaultNightMode()
    if (defaultNightMode == AppCompatDelegate.MODE_NIGHT_YES)
        return true

    if (defaultNightMode == AppCompatDelegate.MODE_NIGHT_NO)
        return false

    val currentNightMode: Int = (context.resources.configuration.uiMode
            and Configuration.UI_MODE_NIGHT_MASK)
    when (currentNightMode) {
        Configuration.UI_MODE_NIGHT_NO -> return false
        Configuration.UI_MODE_NIGHT_YES -> return true
        Configuration.UI_MODE_NIGHT_UNDEFINED -> return false
    }
    return false
}

// Function to generate a new position around a given latitude and longitude within a given radius (in meters)
fun generateNewPosition(lat: Double, lng: Double, radius: Double): LatLng {
    val random = Random()
    // Convert radius from meters to degrees
    val radiusInDegrees = radius / 111000f

    val u = random.nextDouble()
    val v = random.nextDouble()
    val w = radiusInDegrees * sqrt(u)
    val t = 2 * PI * v
    val x = w * cos(t)
    val y = w * sin(t)

    // Adjust the x-coordinate for the shrinking of the east-west distances
    val newLng = lng + x / cos(Math.toRadians(lat))

    return LatLng(lat + y, newLng)
}

// Function to calculate the distance between two latitude-longitude pairs using the Haversine formula
fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadius = 6371000 // in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
