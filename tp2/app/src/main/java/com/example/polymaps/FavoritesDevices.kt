package com.example.polymaps

import android.content.Context
import com.example.polymaps.database.DatabaseManager

interface FavoritesDevicesListener {
    fun onFavoritesChanged()
}

object FavoritesDevices {
    fun getFavoriteList(context: Context, listener: (List<DetectedDevice>) -> Unit) {
        DatabaseManager.getInstance(context).getFavoriteDevices(listener)
    }

    fun addOrRemoveFromList(selectedItem: DetectedDevice, listener: FavoritesDevicesListener, context: Context) {
        val newIsFavorite = !selectedItem.isFavorite
        if (newIsFavorite) {
            selectedItem.isFavorite = true
            DatabaseManager.getInstance(context).addToFavorites(selectedItem)
            listener.onFavoritesChanged()
        } else {
            selectedItem.isFavorite = false
            DatabaseManager.getInstance(context).removeFromFavorites(selectedItem)
            listener.onFavoritesChanged()
        }
    }

}
