import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context

class BluetoothDeviceScanner (private val applicationContext: Context) {

    private var bluetoothManager =  applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager;

    public val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter;


    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        bluetoothAdapter.cancelDiscovery()
        bluetoothAdapter.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun getDiscoveredDevices(): List<BluetoothDevice> {
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }
    @SuppressLint("MissingPermission")
    fun printDiscoveredDevices() {

        val devices = getDiscoveredDevices()
        if (devices.isNotEmpty()) {
            println("Devices discovered:")
            devices.forEach { device ->
                println("Name: ${device.name} - Address: ${device.address}")
            }
        } else {
            println("No devices discovered.")
        }
    }
}
