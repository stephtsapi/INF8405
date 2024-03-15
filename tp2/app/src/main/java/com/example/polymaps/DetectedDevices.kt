package com.example.polymaps

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.polymaps.utils.CustomListAdapter

data class DetectedDevice(
    var name: String = "",
    var macAddress: String = "",
    var position: LatLng = LatLng(),
    var type: String = "",
    var distance: String = "",
    var isFavorite: Boolean = false,
) {
    constructor() : this("", "", LatLng(), "", "", false)

    // rest of the code
    fun setLatLng(latLng: LatLng) {
        position = latLng
    }

    fun getLatLng(listener: (LatLng?) -> Unit) {
        listener(position)
    }
}

class DetectedDevices {

    companion object {

        private val TAG = "MapViewModel"

        // Mutable list of  Bluetooth Device
        val bluetoothDevices = mutableListOf<BluetoothDevice>()
        private lateinit var listAdapter: CustomListAdapter

        //List of actual detected devices
        private val deviceList = arrayListOf<DetectedDevice>()

        fun getDeviceList(): ArrayList<DetectedDevice> { return deviceList }

        fun setListAdapter(context: Context) { listAdapter = CustomListAdapter(context, deviceList) }

        fun getListAdapter(): CustomListAdapter { return listAdapter }

        fun shareDeviceInformation(selectedItem: DetectedDevice, activity: Activity, context: Context) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val deviceInfo = "${context.getString(R.string.device_name) + ": " + selectedItem.name}\n" +
                    "${context.getString(R.string.device_address) + ": " + selectedItem.macAddress}\n" +
                    "${context.getString(R.string.device_type) + ": " + selectedItem.type}\n" +
                    (context.getString(R.string.device_distance) + ": " + selectedItem.distance + " " + context.getString(R.string.meters))
            shareIntent.putExtra(Intent.EXTRA_TEXT, "${context.getString(R.string.detected_share)} + \n\n$deviceInfo")
            activity.startActivity(Intent.createChooser(shareIntent, "Share via"))
        }


        fun showDirectionsOnGoogleMaps(selectedItem: DetectedDevice, activity: Activity) {
            val location = "${selectedItem.position?.latitude},${selectedItem.position?.longitude}"
            val uri = Uri.parse("google.navigation:q=$location")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            mapIntent.setPackage("com.google.android.apps.maps")
            try {
                activity.startActivity(mapIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error opening Google Maps: ${e.message}")
            }
        }

         // Function  to get the Bluetooth class of device
         @SuppressLint("MissingPermission")
         fun getDeviceType(device: BluetoothDevice, context: Context): String = when (device.bluetoothClass.deviceClass) {
             BluetoothClass.Device.AUDIO_VIDEO_CAMCORDER -> context.getString(R.string.audio_video_camcorder)
             BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO -> context.getString(R.string.audio_video_car_audio)
             BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE -> context.getString(R.string.audio_video_handsfree)
             BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES -> context.getString(R.string.audio_video_headphones)
             BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO -> context.getString(R.string.audio_video_hifi_audio)
             BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER -> context.getString(R.string.audio_video_loudspeaker)
             BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE -> context.getString(R.string.audio_video_microphone)
             BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO -> context.getString(R.string.audio_video_portable_audio)
             BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX -> context.getString(R.string.audio_video_set_top_box)
             BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED -> context.getString(R.string.audio_video_uncategorized)
             BluetoothClass.Device.AUDIO_VIDEO_VCR -> context.getString(R.string.audio_video_vcr)
             BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CAMERA -> context.getString(R.string.audio_video_video_camera)
             BluetoothClass.Device.COMPUTER_DESKTOP -> context.getString(R.string.computer_desktop)
             BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA -> context.getString(R.string.computer_handheld_pc_pda)
             BluetoothClass.Device.COMPUTER_LAPTOP -> context.getString(R.string.computer_laptop)
             BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA -> context.getString(R.string.computer_palm_size_pc_pda)
             BluetoothClass.Device.COMPUTER_SERVER -> context.getString(R.string.computer_server)
             BluetoothClass.Device.COMPUTER_UNCATEGORIZED -> context.getString(R.string.computer_uncategorized)
             BluetoothClass.Device.HEALTH_BLOOD_PRESSURE -> context.getString(R.string.health_blood_pressure)
             BluetoothClass.Device.HEALTH_DATA_DISPLAY -> context.getString(R.string.health_data_display)
             BluetoothClass.Device.HEALTH_GLUCOSE -> context.getString(R.string.health_glucose)
             BluetoothClass.Device.HEALTH_PULSE_OXIMETER -> context.getString(R.string.health_pulse_oximeter)
             BluetoothClass.Device.HEALTH_PULSE_RATE -> context.getString(R.string.health_pulse_rate)
             BluetoothClass.Device.HEALTH_THERMOMETER -> context.getString(R.string.health_thermometer)
             BluetoothClass.Device.HEALTH_UNCATEGORIZED -> context.getString(R.string.health_uncategorized)
             BluetoothClass.Device.HEALTH_WEIGHING -> context.getString(R.string.health_weighing)
             BluetoothClass.Device.PHONE_CELLULAR -> context.getString(R.string.phone_cellular)
             BluetoothClass.Device.PHONE_CORDLESS -> context.getString(R.string.phone_cordless)
             BluetoothClass.Device.PHONE_ISDN -> context.getString(R.string.phone_isdn)
             BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY -> context.getString(R.string.phone_modem_or_gateway)
             BluetoothClass.Device.PHONE_SMART -> context.getString(R.string.phone_smart)
             BluetoothClass.Device.PHONE_UNCATEGORIZED -> context.getString(R.string.phone_uncategorized)
             BluetoothClass.Device.TOY_CONTROLLER -> context.getString(R.string.toy_controller)
             BluetoothClass.Device.TOY_DOLL_ACTION_FIGURE -> context.getString(R.string.toy_doll_action_figure)
             BluetoothClass.Device.TOY_GAME -> context.getString(R.string.toy_game)
             BluetoothClass.Device.TOY_ROBOT -> context.getString(R.string.toy_robot)
             BluetoothClass.Device.TOY_UNCATEGORIZED -> context.getString(R.string.toy_uncategorized)
             BluetoothClass.Device.TOY_VEHICLE -> context.getString(R.string.toy_vehicle)
             BluetoothClass.Device.WEARABLE_GLASSES -> context.getString(R.string.wearable_glasses)
             BluetoothClass.Device.WEARABLE_HELMET -> context.getString(R.string.wearable_helmet)
             BluetoothClass.Device.WEARABLE_JACKET -> context.getString(R.string.wearable_jacket)
             BluetoothClass.Device.WEARABLE_PAGER -> context.getString(R.string.wearable_pager)
             BluetoothClass.Device.WEARABLE_UNCATEGORIZED -> context.getString(R.string.wearable_uncategorized)
             BluetoothClass.Device.WEARABLE_WRIST_WATCH -> context.getString(R.string.wearable_wrist_watch)
             else -> context.getString(R.string.unknown_device_type)
        }

    }
}

