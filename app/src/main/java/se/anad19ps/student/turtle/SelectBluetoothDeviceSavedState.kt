package se.anad19ps.student.turtle


import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SelectBluetoothDeviceSavedState(
    val scannedDevicesList: ArrayList<BluetoothDevice>,
    val scannedDevicesNameList: ArrayList<String>,
    val pairedDevicesList: ArrayList<BluetoothDevice>,
    val pairedDevicesNameList: ArrayList<String>
) : Parcelable