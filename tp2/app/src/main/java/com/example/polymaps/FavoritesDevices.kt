package com.example.polymaps

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.polymaps.database.DatabaseManager
import com.example.polymaps.utils.CustomListAdapter

interface FavoritesDevicesListener {
    fun onFavoritesChanged()
}

object FavoritesDevices {
    fun getFavoriteList(listener: (List<DetectedDevice>) -> Unit) {
        DatabaseManager.getInstance().getFavoriteDevices(listener)
    }

    fun addOrRemoveFromList(selectedItem: DetectedDevice, listener: FavoritesDevicesListener) {
        val newIsFavorite = !selectedItem.isFavorite
        if (newIsFavorite) {
            selectedItem.isFavorite = true
            DatabaseManager.getInstance().addToFavorites(selectedItem)
            listener.onFavoritesChanged()
        } else {
            selectedItem.isFavorite = false
            DatabaseManager.getInstance().removeFromFavorites(selectedItem)
            listener.onFavoritesChanged()
        }
    }

}
