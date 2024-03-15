package com.example.polymaps.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.polymaps.DetectedDevice
import com.example.polymaps.R

// Used for device ListView
class CustomListAdapter(context: Context, val devices: ArrayList<DetectedDevice>) : BaseAdapter() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return devices.size
    }

    override fun getItem(position: Int): DetectedDevice {
        return devices[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        val viewHolder: ViewHolder

        if (view == null) {
            view = inflater.inflate(R.layout.list_item_detected_device, parent, false)
            viewHolder = ViewHolder()
            viewHolder.nameTextView = view.findViewById(R.id.nameTextView)
            viewHolder.macAddressTextView = view.findViewById(R.id.macAddressTextView)
            viewHolder.distanceTextView = view.findViewById(R.id.distanceTextView)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        val device = getItem(position) as DetectedDevice
        viewHolder.nameTextView.text = device.name
        viewHolder.macAddressTextView.text = device.macAddress
        viewHolder.distanceTextView.text = device.distance

        return view
    }

    fun updateDevices(newList: List<DetectedDevice>) {
        devices.clear()
        devices.addAll(newList)
        notifyDataSetChanged()
    }

    private class ViewHolder {
        lateinit var nameTextView: TextView
        lateinit var macAddressTextView: TextView
        lateinit var distanceTextView: TextView
    }
}