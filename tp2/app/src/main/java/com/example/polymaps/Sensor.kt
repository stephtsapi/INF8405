package com.example.polymaps

import android.content.Context
import android.hardware.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService

interface ISensor : SensorEventListener {

    var data: String
    var mSensorManager: SensorManager
    var mSensor : Sensor
    var mSensorValues: MutableList<String> // Liste pour stocker les valeurs du senseurs
    var type : String

     fun mount()
     fun unmount()
}

class Accelerometer : ISensor {

    override var mSensorManager: SensorManager
    override var mSensor: Sensor
    override var mSensorValues: MutableList<String>
    override var type: String
    override var data: String

    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable
    private var isRunning : Boolean
    private var mContext: Context

    constructor(context: Context)
    {
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorValues = mutableListOf<String>()
        type = "Acc"
        isRunning = false
        data = ""
        mContext = context
    }

    override fun mount() {

        if(isRunning)
            return // avoid launching multiple threads

        mHandler = Handler(Looper.getMainLooper())
        mRunnable = Runnable {
            mSensorManager.registerListener(this@Accelerometer, mSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.e("thread", "iterated")
            mHandler.postDelayed(mRunnable, 1000)
        }
        mHandler.post(mRunnable) // Start the Runnable
        isRunning = true
    }

     override fun unmount() {
         mHandler.removeCallbacks(mRunnable)
         mSensorManager.unregisterListener(this)
         isRunning = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = String.format("%.2f", event.values[0])
                val y = String.format("%.2f", event.values[1])
                val z = String.format("%.2f", event.values[2])
                val data = "X: $x\tY: $y\tZ: $z"

                (mContext as MainActivity).runOnUiThread {
                    val accelerometerTextView = (mContext as MainActivity).findViewById<TextView>(R.id.accelerometer)
                    if (accelerometerTextView !== null) {
                        accelerometerTextView.text = mContext.getString(R.string.accelerometer_label, data)
                    }
                }
            }
        }
        this.mSensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }
}

class MotionDetector : ISensor, TriggerEventListener {

    override var mSensorManager: SensorManager
    override var mSensor: Sensor
    override var mSensorValues: MutableList<String>
    override var type: String
    override var data: String = "False"
    private var mContext: Context

    constructor(context: Context) {
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)
        mSensorValues = mutableListOf<String>()
        type = "Significant motion detector"
        mContext = context

        (mContext as MainActivity).runOnUiThread {
            val motionDetectorTextView = (mContext as MainActivity).findViewById<TextView>(R.id.motion)
            if (motionDetectorTextView !== null) {
                motionDetectorTextView.text = mContext.getString(R.string.motion_label, mContext.getString(R.string.stable))
            }
        }
    }

    override fun mount() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME)
        mSensorManager.requestTriggerSensor(this, mSensor)
    }

    override fun unmount() {
        mSensorManager.unregisterListener(this)
        mSensorManager.cancelTriggerSensor(this, mSensor)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        return
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onTrigger(event: TriggerEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_SIGNIFICANT_MOTION) {
            Log.e("Significant motion detector", "Significant motion detected!")
            data = mContext.getString(R.string.abrupt)

            (mContext as MainActivity).runOnUiThread {
                val motionDetectorTextView = (mContext as MainActivity).findViewById<TextView>(R.id.motion)
                if (motionDetectorTextView !== null) {
                    motionDetectorTextView.text = mContext.getString(R.string.motion_label, data)
                }
            }

            // Revert back to "Stable" after 5 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                data = mContext.getString(R.string.stable)
                (mContext as MainActivity).runOnUiThread {
                    val motionDetectorTextView = (mContext as MainActivity).findViewById<TextView>(R.id.motion)
                    if (motionDetectorTextView !== null) {
                        motionDetectorTextView.text = mContext.getString(R.string.motion_label, data)
                    }
                }
            }, 2000)

            this@MotionDetector.mSensorManager.requestTriggerSensor(this, mSensor)
        }
    }
}
