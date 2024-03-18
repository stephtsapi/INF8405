package com.example.polymaps

import BluetoothDeviceScanner
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.TrafficStats
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.polymaps.databinding.ActivityMainBinding
import com.example.polymaps.utils.calculateDistance
import com.example.polymaps.utils.generateNewPosition
import com.google.android.gms.location.*
import java.util.*
import kotlin.math.*
// Import for usage of
import android.os.Handler
import android.os.BatteryManager
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.util.Log
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import com.example.polymaps.auth.AuthManager
import com.example.polymaps.auth.AuthViewModel
import com.example.polymaps.auth.AuthViewModelFactory
import com.example.polymaps.screens.*
import com.example.polymaps.database.DatabaseManager



const val REQUEST_LOCATION_PERMISSION = 100
const val REQUEST_ENABLE_BT = 101


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding
    private var currentFragment: Fragment? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapViewModel: MapViewModel
    private lateinit var authViewModel: AuthViewModel
    private var authManager: AuthManager = AuthManager()

    private val TAG = "MainActivity"
    private val handlerLink = Handler()
    private val handlerBattery: Handler = Handler()
//    private val sensorList =  arrayListOf<ISensor>()
    lateinit var runnableBattery: Runnable
    lateinit var runnableLink: Runnable
    private var remainingBatteryPct: Int = 0
    private lateinit var accelerometer: Accelerometer
    private lateinit var motionDetector: MotionDetector


    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    // Clause to check is device already exist in the table and avoid duplicate device
                    if (!isDeviceAlreadyAdded(device!!)) {
                        DetectedDevices.bluetoothDevices.add(device)

//                      Get the current location using FusedLocationProviderClient
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location ->
                                if (location != null) {
                                    // Current location is available, generate new position around it
                                    val newPosition = generateNewPosition(
                                        location.latitude,
                                        location.longitude,
                                        15.0
                                    )
                                    // Calculate the distance between the current location and the new position
                                    val distance = calculateDistance(
                                        location.latitude,
                                        location.longitude,
                                        newPosition.latitude,
                                        newPosition.longitude
                                    )
                                    val deviceName = if (device.name != null) device.name else context.getString(R.string.unknown_device_name)
                                    val detectedDevice = DetectedDevice(
                                        deviceName,
                                        device.address,
                                        newPosition,
                                        DetectedDevices.getDeviceType((device), context),
                                        "${distance.roundToInt()} m",
                                        DatabaseManager.getInstance(context).isFavorite(device.address)
                                    )
                                    DetectedDevices.getDeviceList().add(detectedDevice)
                                    mapViewModel.addDeviceLocation(deviceName, newPosition)
                                    DetectedDevices.getListAdapter().notifyDataSetChanged()
                                }
                            }
                    }
                }
            }

        }
    }


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        authViewModel = ViewModelProvider(this, AuthViewModelFactory(application, authManager)).get(
            AuthViewModel::class.java)
        
        motionDetector = MotionDetector(this)
        accelerometer = Accelerometer(this)

        // Listen to sensor updates...
        mountSensors()

        val mapFragment= MapFragment()
        val favoriteFragment= FavoritesFragment()
        val settingsFragment= SettingsFragment()

        DetectedDevices.setListAdapter(this)

        if(authManager.getCurrentUser() == null) {
            currentFragment = LoginFragment()
        } else {
            currentFragment = if (savedInstanceState != null) {
                remainingBatteryPct = savedInstanceState.getInt("remainingBatteryPct", 0)
                // Restore the previous fragment -- useful for color theme change
                when (savedInstanceState.getInt("currentFragmentIndex")) {
                    0 -> MapFragment()
                    1 -> FavoritesFragment()
                    2 -> SettingsFragment()
                    else -> null
                }
            } else {
                MapFragment()
            }
        }

        setCurrentFragment(currentFragment!!)

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.map->setCurrentFragment(mapFragment)
                R.id.favorites->setCurrentFragment(favoriteFragment)
                R.id.settings->setCurrentFragment(settingsFragment)

            }
            true
        }

        // Call for battery Level update
        startBatteryUpdate()

        // Code for calculate uplink and down-link
        startLinkUpdate()

        val scanner = BluetoothDeviceScanner(this@MainActivity)
        scanner.startDiscovery()
        if (!scanner.bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
            scanner.startDiscovery()
        }

        // Request permission to access location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // Permission is granted
            searchForDevices()
        }

    }



    private fun searchForDevices() {
        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                searchForDevices()
            } else {
                // Permission denied
                // ...
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth is enabled
                searchForDevices()
            } else {
                // ...
            }
        }
    }

    fun isDeviceAlreadyAdded(device: BluetoothDevice): Boolean {
        return DetectedDevices.bluetoothDevices.any { it.address == device.address }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment)
            .commit()
        currentFragment = fragment
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("remainingBatteryPct", remainingBatteryPct)
        // Save the current fragment
        when (currentFragment) {
            is MapFragment -> outState.putInt("currentFragmentIndex", 0)
            is FavoritesFragment -> outState.putInt("currentFragmentIndex", 1)
            is SettingsFragment -> outState.putInt("currentFragmentIndex", 2)
        }
    }

    override fun onBackPressed() {
        // Do nothing here...
    }

    private fun startBatteryUpdate() {
        val batteryManager: BatteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = registerReceiver(null, intentFilter)

        var initialBatteryLevel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val initialScale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        initialBatteryLevel = initialBatteryLevel!! * 100 / initialScale!!.toFloat().toInt()

        val runnableBattery = object : Runnable {
            override fun run() {
                // Update the battery status object to get the current battery level
                val batteryStatus = registerReceiver(null, intentFilter)
                val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = level!! * 100 / scale!!.toFloat()

                val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                val currentPower = currentNow * chargeCounter / 1000


                val currentPowerTextView = findViewById<TextView>(R.id.currentPower)
                if (currentPowerTextView != null)
                    currentPowerTextView.text = getString(R.string.current_power, currentPower)

                val remainingBatteryPct = -(batteryPct.toInt() - initialBatteryLevel)
                val currentLevelTextView = findViewById<TextView>(R.id.currentLevel)
                if (currentLevelTextView != null)
                    currentLevelTextView.text = getString(R.string.current_level, remainingBatteryPct)
                handlerBattery.postDelayed(this, 1000) // Update every 1000ms (1 second)
            }
        }

        handlerBattery.post(runnableBattery)
    }


    private fun startLinkUpdate() {
        var lastUplinkBytes: Long = 0
        var lastDownlinkBytes: Long = 0

        val runnableLink = object : Runnable {
            override fun run() {
                val currentUplinkBytes = TrafficStats.getTotalTxBytes()
                val currentDownlinkBytes = TrafficStats.getTotalRxBytes()

                // Set consumption of Bandwidth
                val uplinkBandwidth = (currentUplinkBytes - lastUplinkBytes) / 1024 // en Ko/s
                val downlinkBandwidth = (currentDownlinkBytes - lastDownlinkBytes) / 1024 // en Ko/s

                lastUplinkBytes = currentUplinkBytes
                lastDownlinkBytes = currentDownlinkBytes

                // Value of downSpeed display in the view
                val downSpeedTextView = findViewById<TextView>(R.id.downSpeed)
                if (downSpeedTextView != null)
                    downSpeedTextView.text = getString(R.string.down_speed, downlinkBandwidth)

                // Value of upSpeed display in the view.
                val upSpeedTextView = findViewById<TextView>(R.id.upSpeed)
                if (upSpeedTextView != null)
                    upSpeedTextView.text = getString(R.string.up_speed, uplinkBandwidth)

                println("Downlink: $downlinkBandwidth Ko/s ")
                println("Uplink: $uplinkBandwidth Ko/s ")

                handlerLink.postDelayed(this, 2500) // Mettre à jour toutes les 2500ms (2,5 secondes)
            }
        }

        handlerLink.post(runnableLink)
    }

    override fun onResume() {
        super.onResume()
        mountSensors()
    }

    override fun onPause() {
        super.onPause()
        unMountSensors()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
        handlerLink.removeCallbacksAndMessages(null)
        if (::runnableBattery.isInitialized) {
            handlerBattery.removeCallbacks(runnableBattery)
        }
        if (::runnableLink.isInitialized) {
            handlerLink.removeCallbacks(runnableLink)
        }

        unMountSensors()
    }

    private fun mountSensors() {
        accelerometer.mount()
        motionDetector.mount()
    }

    private fun unMountSensors() {
        accelerometer.unmount()
        motionDetector.unmount()
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        Log.e("SENSOR", "onSensorChanged")
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.e("SENSOR", "onSensorChanged")
    }

}