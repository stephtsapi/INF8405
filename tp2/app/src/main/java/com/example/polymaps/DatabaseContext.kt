//package com.example.polymaps
//import android.content.Context
//import java.io.File
//import com.google.android.gms.maps.model.LatLng
//
//
//class DatabaseContext {
//    companion object {
//        private lateinit var fileName: String
//
//        fun init(context: Context) {
//            this.fileName =  context.filesDir.absolutePath + "devices.txt"
//            val file = File(this.fileName)
//
//            if (!file.exists()) {
//                file.createNewFile()
//                println("${this.fileName} created successfully!")
//            } else {
//                println("${this.fileName} already exists.")
//            }
//        }
//
//        fun getFavoriteDevices(): ArrayList<DetectedDevice> {
//            val allDevices = getAllDevices()
//            return ArrayList(allDevices.filter { it.isFavorite })
//        }
//
//        private fun getAllDevices(): ArrayList<DetectedDevice> {
//            val file = File(fileName)
//            val fileContent = file.readText()
//            val stringifiedDeviceList = fileContent.split("\n")
//
//            val deviceList : ArrayList<DetectedDevice> = ArrayList()
//
//            for (stringifiedDevice in stringifiedDeviceList) {
//                if (stringifiedDevice != "") {
//                    val device = parseDevice(stringifiedDevice)
//                    deviceList.add(device!!)
//                }
//            }
//            return deviceList
//        }
//
//        fun addToFavorites(device: DetectedDevice): Boolean {
//            if (getDevice(device.macAddress) != null) {
//                return false
//            }
//
//            val file = File(this.fileName)
//            val deviceToAdd = stringifyDevice(device)
//            file.appendText(deviceToAdd)
//            return true;
//        }
//
//        fun removeFromFavorites(device: DetectedDevice): Boolean {
//            if (getDevice(device.macAddress) == null) {
//                return false
//            }
//
//            val file = File(this.fileName)
//            val fileContent = file.readText()
//
//            val pattern = Regex("${device.macAddress}\t.*[\n\r]")
//            val matchResult = pattern.find(fileContent)
//
//            if (matchResult != null) {
//                val newFileContent = pattern.replace(fileContent, "")
//                file.writeText(newFileContent)
//
//                return true
//            }
//            return false;
//        }
//
//        private fun getDevice(deviceMac: String): DetectedDevice? {
//            val file = File(fileName)
//            val fileContent = file.readText()
//
//            val pattern = Regex("$deviceMac\t.*(?=[\n\r])")
//            val matchResult = pattern.find(fileContent)
//
//            if (matchResult != null) {
//                return parseDevice(matchResult.value)
//            }
//
//            return null;
//        }
//
//        private fun stringifyDevice(device: DetectedDevice): String {
//            return "${device.macAddress}\t${device.name}\t${device.position}\t${device.type}\t${device.distance}\t${device.isFavorite}\n"
//        }
//
////        private fun parseDevice(text: String): DetectedDevice? {
////            return try {
////                val fields = text.split("\t")
////                val macAddress = fields[0]
////                val name = fields[1]
////                val latLng = fields[2].substringAfter("(").substringBefore(")").split(",")
////                val position = LatLng(latLng[0].toDouble(), latLng[1].toDouble())
////                val type = fields[3]
////                val distance = fields[4]
////                val isFavorite = fields[5].toBoolean()
////                DetectedDevice(name, macAddress, position, type, distance, isFavorite)
////            } catch (e: Exception) {
////                e.printStackTrace()
////                DetectedDevice("", "", LatLng(0.0, 0.0), "", "", false)
////            }
////        }
//    }
//}