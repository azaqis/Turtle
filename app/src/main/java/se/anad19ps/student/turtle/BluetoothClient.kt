package se.anad19ps.student.turtle

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_select_bluetooth_device.*
import java.io.IOException
import java.io.OutputStream

class BluetoothClient(device: BluetoothDevice, uiactivity: Activity) : Thread() {
    private val socket = device.createRfcommSocketToServiceRecord(device.uuids?.get(0)!!.uuid)
    private val activityUI = uiactivity
    private lateinit var outputStream: OutputStream

    override fun run() {
        SelectBluetoothDeviceActivity.bluetoothConnectionThreadActive = true
        try {
            Log.d("BT.Client", "Connecting")
            this.socket.connect()
        } catch (e: IOException) {
            Log.d("BT.Client", "Connection failed")
            Utils.UtilsObject.showUpdatedToast(activityUI.getString(R.string.connection_failed), activityUI.applicationContext)
            SelectBluetoothDeviceActivity.bluetoothConnectionThreadActive = false
            this.activityUI.runOnUiThread {
                activityUI.progressBar.visibility = View.INVISIBLE
                activityUI.refreshBluetoothDevicesButton.visibility = View.VISIBLE
            }
        }

        if (this.socket.isConnected) {
            outputStream = this.socket.outputStream
            val inputStream = this.socket.inputStream
            var bytesBuffer: Int

            while (SelectBluetoothDeviceActivity.bluetoothConnectionThreadActive) {
                try {
                    bytesBuffer = inputStream.read(SelectBluetoothDeviceActivity.inputBuffer)
                    SelectBluetoothDeviceActivity.messageRecieved = String(
                        SelectBluetoothDeviceActivity.inputBuffer, 0, bytesBuffer
                    )
                    Utils.UtilsObject.bluetoothReceiveStringReady(SelectBluetoothDeviceActivity.messageRecieved!!)
                } catch (e: IOException) {
                    Log.e("BT.Client", "Error reading Input Stream. ", e)
                }
            }
            outputStream.close()
            inputStream.close()
            this.socket.close()
        }
    }

    fun writeToConnectedDevice(stringToWrite: String) {
        try {
            outputStream.write(stringToWrite.toByteArray())
            outputStream.flush()
            Log.d("BT.Client", "Sent")
        } catch (e: IOException) {
            Log.d("BT.Client", "Cannot send", e)
        }
    }
}