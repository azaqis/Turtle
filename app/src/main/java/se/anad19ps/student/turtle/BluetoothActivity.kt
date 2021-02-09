/*package se.anad19ps.student.turtle

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.*

class BluetoothActivity : AppCompatActivity() {

    companion object{
        var bluetoothUUID : UUID = UUID.fromString("0000000000000000000000000000000000000000")
        var bluetoothSocket : BluetoothSocket? = null
        lateinit var progress : ProgressDialog
        lateinit var bluetoothAdapter : BluetoothAdapter
        var bluetoothIsConnected : Boolean = false
        lateinit var bluetoothAdress : String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothAdress = intent.getStringExtra(SelectBluetoothDeviceActivity.EXTRA_ADRESS).toString()

        ConnectToDevice(this).execute()
    }

    private fun sendCommand(input: String){
        if(bluetoothSocket != null){
            try {
                bluetoothSocket!!.outputStream.write(input.toByteArray())
            }
            catch (e : IOException){
                e.printStackTrace()
            }
        }
    }

    private fun disconnect(){
        if(bluetoothSocket != null){
            try {
                bluetoothSocket!!.close()
                bluetoothSocket = null
                bluetoothIsConnected = false
            }
            catch(e : IOException){
                e.printStackTrace()
            }
        }
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>(){
        private var connectSuccess : Boolean = true
        private val context : Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progress = ProgressDialog.show(context, "Connecting...", "please wait")

        }

        override fun doInBackground(vararg params: Void?){
            try {
                if (bluetoothSocket == null || !bluetoothIsConnected){
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = bluetoothAdress.getRemoteDevice(bluetoothAdress)
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(bluetoothUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    bluetoothSocket!!.connect()
                }
            }
            catch (e : IOException){
                connectSuccess = false
                e.printStackTrace()
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!connectSuccess){
                Log.i("BT", "BT COULD NOT CONNECT")
            }
            else{
                bluetoothIsConnected = true
            }
            progress.dismiss()
        }
    }
}*/