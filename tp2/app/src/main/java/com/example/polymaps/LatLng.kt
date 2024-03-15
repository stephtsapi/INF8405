package com.example.polymaps


import android.os.Parcel
import android.os.Parcelable

class LatLng : Parcelable {
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    constructor() {
        // Required empty constructor for Firebase
    }

    constructor(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }

    // Parcelable implementation
    constructor(parcel: Parcel) : this() {
        latitude = parcel.readDouble()
        longitude = parcel.readDouble()
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "(${String.format("%.6f", latitude).replace(',', '.')}, ${String.format("%.6f", longitude).replace(',', '.')})"
    }

    companion object CREATOR : Parcelable.Creator<LatLng> {
        override fun createFromParcel(parcel: Parcel): LatLng {
            return LatLng(parcel)
        }

        override fun newArray(size: Int): Array<LatLng?> {
            return arrayOfNulls(size)
        }
    }
}


