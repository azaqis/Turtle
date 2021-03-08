package se.anad19ps.student.turtle

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_select_bluetooth_device.*
import java.io.IOException
import java.io.OutputStream

class BluetoothClient(device: BluetoothDevice, uiactivity : Activity): Thread() {
    private val socket = device.createRfcommSocketToServiceRecord(device.uuids?.get(0)!!.uuid)
    private val activity = uiactivity
    private lateinit var outputStream : OutputStream

    override fun run() {
        SelectBluetoothDeviceActivity.bluetoothConnectionThreadActive = true
        try{
            Log.d("BT.Client", "Connecting")
            this.socket.connect()
        } catch (e : IOException){
            Log.d("BT.Client", "Connection failed")
            Utils.UtilsObject.showUpdatedToast("Connection failed, device may be already connected or out of range", activity.applicationContext)
            SelectBluetoothDeviceActivity.bluetoothConnectionThreadActive = false
            this.activity.runOnUiThread {
                activity.progressBar.visibility = View.INVISIBLE
                activity.refreshBluetoothDevicesButton.visibility = View.VISIBLE
            }
        }

        if(this.socket.isConnected){
            outputStream = this.socket.outputStream
            val inputStream = this.socket.inputStream
            var bytes : Int

            while(SelectBluetoothDeviceActivity.bluetoothConnectionThreadActive) {
                try{
                    bytes = inputStream.read(SelectBluetoothDeviceActivity.inputBuffer)
                    SelectBluetoothDeviceActivity.messageRecieved = String(
                        SelectBluetoothDeviceActivity.inputBuffer, 0, bytes)
                    Utils.UtilsObject.bluetoothReceiveStringReady(SelectBluetoothDeviceActivity.messageRecieved!!)
                } catch (e : IOException){
                    Log.e("BT.Client", "Error reading Input Stream. ", e)
                }
            }
            outputStream.close()
            inputStream.close()
            this.socket.close()
        }
    }

    fun write(stringToWrite : String){
        try {
            outputStream.write(stringToWrite!!.toByteArray())
            outputStream.flush()
            Log.d("BT.Client", "Sent")
        } catch(e: IOException) {
            Log.d("BT.Client", "Cannot send", e)
        }
    }
}