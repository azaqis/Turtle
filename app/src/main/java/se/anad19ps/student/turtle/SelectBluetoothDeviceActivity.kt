package se.anad19ps.student.turtle

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_select_bluetooth_device.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class SelectBluetoothDeviceActivity : AppCompatActivity() {

    companion object{
        private val REQUEST_CODE_ENABLE_BT: Int = 1;

        //BT Adapter
        lateinit var  btAdapter: BluetoothAdapter
        lateinit var  btSocket : BluetoothSocket

        lateinit var btAddress : String
        lateinit var  btProgress : ProgressDialog

        var isConnected : Boolean = false

        private var my_UUID: UUID = UUID.fromString("d989d859-9186-452a-8c43-0059a3599ac5")

        //lateinit var btRepos : BluetoothRepository

        var devices = mutableListOf<BluetoothDevice>()

        val list: ArrayList<String> = ArrayList()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_bluetooth_device)

        refreshBluetoothDevicesButton.setBackgroundColor(getResources().getColor(R.color.PrimaryComplement))
        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        //Init BT Adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter()

        refreshBluetoothDevicesButton.setOnClickListener(){
            resetScannedList()

            displayPairedDevices()

            refreshBluetoothDevicesButton.setBackgroundColor(getResources().getColor(R.color.GreyedButton))
            refreshBluetoothDevicesButton.setText("Now searching...")

            discoverBluetoothDevices()
            /*refreshBluetoothDevicesButton.setBackgroundColor(getResources().getColor(R.color.PrimaryComplement))
            refreshBluetoothDevicesButton.setText("Refresh")*/
        }

        //???????????
        /*val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)*/
    }

    override fun onStart(){
        super.onStart()
        //Check if BT is available on device
        checkBluetoothAvailability()

        //Check if fine permission is allowed
        checkFineLocationPermission()

        resetScannedList()

        discoverBluetoothDevices()
        displayPairedDevices()
    }

    private fun resetScannedList(){
        list.clear()
        devices.clear()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        selectScannedDeviceList.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            REQUEST_CODE_ENABLE_BT ->
                if (resultCode == Activity.RESULT_OK){
                    Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "Bluetooth is not enabled, please try again", Toast.LENGTH_SHORT).show()
                }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkBluetoothAvailability(){
        if(btAdapter == null){
            Toast.makeText(this, "Bluetooth is not avaliable on this device", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "Bluetooth is avaliable on this device", Toast.LENGTH_SHORT).show()
            //Turn on BT if not turned on
            if(!btAdapter.isEnabled){
                var intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, REQUEST_CODE_ENABLE_BT)
            }
        }
    }

    private fun checkFineLocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(baseContext,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
            }
        }
    }

    private fun displayPairedDevices(){
        lateinit var pairedDevices : Set<BluetoothDevice>
        pairedDevices = btAdapter!!.bondedDevices
        val list: ArrayList<String> = ArrayList()

        if(pairedDevices.isNotEmpty()){
            for(device : BluetoothDevice in pairedDevices){
                list.add(device.getName())

                //Toast.makeText(this, device.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        else{
            Toast.makeText(this,"No devices found", Toast.LENGTH_SHORT).show()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        selectPairedDeviceList.adapter = adapter

    }

    private fun discoverBluetoothDevices(){
        if(btAdapter.isDiscovering){
            btAdapter.cancelDiscovery()
        }
        btAdapter.startDiscovery()
        val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, discoverDevicesIntent)
    }

    fun addDeviceToList(btDevice : BluetoothDevice){
        if(btDevice.name == null){
            list.add(btDevice.address)
        }
        else{
            list.add(btDevice.name)
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        selectScannedDeviceList.adapter = adapter
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address
                    if (device != null && !devices.contains(device)) {
                        devices.add(device)
                        addDeviceToList(device)
                    }
                }
            }
        }
    }



    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            btProgress = ProgressDialog.show(context, "Connecting...", "please wait")
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (btSocket == null || !isConnected) {
                    btAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = btAdapter.getRemoteDevice(btAddress)
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(my_UUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    btSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                isConnected = true
            }
            btProgress.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        btAdapter.cancelDiscovery()
        unregisterReceiver(receiver)
    }
}
