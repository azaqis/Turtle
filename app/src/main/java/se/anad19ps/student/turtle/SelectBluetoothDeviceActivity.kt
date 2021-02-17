package se.anad19ps.student.turtle

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_select_bluetooth_device.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*
import java.util.*
import kotlin.collections.ArrayList


class SelectBluetoothDeviceActivity : AppCompatActivity() {

    companion object{
        val scannedDevicesList: ArrayList<BluetoothDevice> = ArrayList()
        val pairedDevicesList: ArrayList<BluetoothDevice> = ArrayList()

        private const val REQUEST_CODE_ENABLE_BT: Int = 1
        private const val TAG = "BluetoothActivity"

        //Adapters
        lateinit var btAdapter: BluetoothAdapter
        lateinit var scannedDevicesNameListViewAdapter: ArrayAdapter<String>
        lateinit var pairedDevicesNameListViewAdapter: ArrayAdapter<String>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_bluetooth_device)

        refreshBluetoothDevicesButton.setBackgroundColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.PrimaryComplement
            )
        )

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)


        //Init for adapter for paired devices list view
        pairedDevicesNameListViewAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            getDevicesNameArray(pairedDevicesList)
        )
        selectPairedDeviceList.adapter = pairedDevicesNameListViewAdapter
        selectPairedDeviceList.onItemClickListener = AdapterView.OnItemClickListener { _, v, position, _ ->
            val device: BluetoothDevice = pairedDevicesList[position]
            //Here the app tries to connect/bond with the device chosen in the listview
            Toast.makeText(v.context, "Connecting to: " + device.name + "...", Toast.LENGTH_SHORT).show()
            //Takes a bit of more time, maybe use GATT or something else if time for it exists
            tryConnect(device)
        }


        //Init for adapter for scanned devices list view
        scannedDevicesNameListViewAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            getDevicesNameArray(scannedDevicesList)
        )
        selectScannedDeviceList.adapter = scannedDevicesNameListViewAdapter
        selectScannedDeviceList.onItemClickListener = AdapterView.OnItemClickListener { _, v, position, _ ->
            val device: BluetoothDevice = scannedDevicesList[position]
            //Here the app tries to connect/bond with the device chosen in the listview
            tryBonding(device, v)
        }


        //Init BT Adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter()


        refreshBluetoothDevicesButton.setOnClickListener{
            //Check permissions if it is possible to scan
            checkBluetoothAvailability()
            checkFineLocationAllowed()

            //Only scan if BT is enabled, worthless otherwise
            if(btAdapter.isEnabled)
            {
                //If it is already scanning, dont cancel and scan again, just wait for it to finish scanning. If it is done, then scan again
                if(btAdapter.isDiscovering) {
                    //To make it look dynamic
                    Toast.makeText(this, "Please wait, scanning...", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Please wait, scanning...", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Please wait, scanning...", Toast.LENGTH_SHORT).show()
                }
                else{
                    clearScannedList()
                    clearPairedList()
                    updatePairedDevicesList()
                    discoverBluetoothDevices()
                }
            }
            else
                //Clear list to show that even if there were BT-devices, you shouldnt be able to connect to them
                clearScannedList()
        }

        //Intent-filters
        val filterBond = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(pairingReceiver, filterBond)
        val filterDiscover = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoverReceiver, filterDiscover)
        val filterACLConnected = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
        registerReceiver(aclConnectedReceiver, filterACLConnected)
        val filterACLDisconnectRequested = IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
        registerReceiver(aclDisconnectedRequestedReceiver, filterACLDisconnectRequested)
        val filterACLDisconnected = IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        registerReceiver(aclDisconnectedReciever, filterACLDisconnected)
    }

    override fun onStart(){
        super.onStart()

        //Check permissions
        checkBluetoothAvailability()
        if(btAdapter.isEnabled && checkFineLocationAllowed()){
            clearPairedList()
            clearScannedList()
            discoverBluetoothDevices()
            updatePairedDevicesList()
        }
    }

    private fun updatePairedDevicesList(){
        val devices: ArrayList<BluetoothDevice>? = getPairedDevices()

        if(devices != null){
            for(device : BluetoothDevice in devices){
                pairedDevicesList.add(device)
            }
            selectPairedDeviceList.invalidateViews()
            pairedDevicesNameListViewAdapter.notifyDataSetChanged()
        }
        else
            Toast.makeText(this, "There are no paired devices on this device", Toast.LENGTH_SHORT).show()





        //OBS! THIS DOES NOT UPDATE IF REMOVED EVEN IF IT EXISTS IN onCreate!!!!!
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            getDevicesNameArray(pairedDevicesList)
        )
        selectPairedDeviceList.adapter = adapter





    }

    private fun clearScannedList(){
        scannedDevicesList.clear()
        selectScannedDeviceList.invalidateViews()
    }

    private fun clearPairedList(){
        pairedDevicesList.clear()
        selectPairedDeviceList.invalidateViews()
    }

    private fun checkBluetoothAvailability(){
        //If there is no adapter, its not possible with BT
        if(!btAdapter.isEnabled){
            //Permission dialog appears
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BT)
        }
    }

    private fun checkFineLocationAllowed() : Boolean{
        //Permission for fine location which needs to be checked if you run a later API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    baseContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2
                )
            }
            //Returns if fine location is permitted or not
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
        //If version is lower than Q, dont need to check. Will always alow in that case
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            REQUEST_CODE_ENABLE_BT ->
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show()

                    //Needed to show paired devices after allowing BT permission on view open
                    updatePairedDevicesList()
                    discoverBluetoothDevices()


                } else {
                    Toast.makeText(
                        this,
                        "Bluetooth is not enabled, please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun getPairedDevices() : ArrayList<BluetoothDevice>?{
        //Make a set (type of list?) that holds all of the pre-paired devices
        val pairedDevices : Set<BluetoothDevice> = btAdapter.bondedDevices

        val listOfPairedDevices: ArrayList<BluetoothDevice> = ArrayList()

        //If there are devices that have been paired to before, do code below
        if(pairedDevices.isNotEmpty()){
            //First, get all of the devices to a list
            for(device : BluetoothDevice in pairedDevices){
                listOfPairedDevices.add(device)
            }
            return listOfPairedDevices
        }
        else{
            Toast.makeText(this, "No devices found", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    private fun discoverBluetoothDevices(){
        //If the adapter is trying to discover, cancel it first and then start (as you should do cited from developer site)
        if(btAdapter.isDiscovering){
            btAdapter.cancelDiscovery()
        }
        btAdapter.startDiscovery()
        //Not sure what below does
        val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoverReceiver, discoverDevicesIntent)
    }

    private fun addDeviceToScannedList(btDevice: BluetoothDevice){
        //If a device is found, add it to the scannedDevicesList
        scannedDevicesList.add(btDevice)
        selectScannedDeviceList.invalidateViews()




        //OBS! THIS DOES NOT UPDATE IF REMOVED EVEN IF IT EXISTS IN onCreate!!!!!
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            getDevicesNameArray(scannedDevicesList)
        )
        selectScannedDeviceList.adapter = adapter
        selectScannedDeviceList.onItemClickListener = AdapterView.OnItemClickListener { _, v, position, _ ->
            val device: BluetoothDevice = scannedDevicesList[position]
            //Here the app tries to connect/bond with the device chosen in the listview
            tryBonding(device, v)
        }





    }

    private fun getDevicesNameArray(array : ArrayList<BluetoothDevice>) : ArrayList<String>{
        val devicesNameList: ArrayList<String> = ArrayList()
        for(device : BluetoothDevice in array){
            //Primarily, show device name, but some only have an address, then show the address
            if(device.name != null)
                devicesNameList.add(device.name)
            else
                devicesNameList.add(device.address)
        }
        return devicesNameList
    }

    private fun tryBonding(device: BluetoothDevice, v: View){
        Toast.makeText(v.context, "Trying to bond to device: " + device.name, Toast.LENGTH_SHORT).show()
        //create.bond() doesnt work for all APIs
        Log.d(TAG, "Trying to bond with " + device.name)
        device.createBond()
    }

    private fun tryConnect(device: BluetoothDevice) {
        val thread = Thread {
            //create.bond() doesnt work for all APIs
            Log.d(TAG, "Trying to connect with " + device.name)
            val id: UUID = device.uuids?.get(0)!!.uuid
            val bts = device.createRfcommSocketToServiceRecord(id)
            bts?.connect()
        }
        thread.start()
    }

    private val discoverReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    if (device != null && !scannedDevicesList.contains(device)) {
                        addDeviceToScannedList(device)
                    }
                }
            }
        }
    }

    private val pairingReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val mDevice =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                //3 cases:
                //case1: bonded already
                if (mDevice!!.bondState == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(context, "Connected to: " + mDevice.name, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.")
                    Log.e(TAG, "BroadcastReceiver: BOND_BONDED." + mDevice.name)
                    btAdapter.cancelDiscovery()
                }
                //case2: creating a bone
                if (mDevice.bondState == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(context, "Connecting to: " + mDevice.name + "...", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.")
                    Log.e(TAG, "BroadcastReceiver: BOND_BONDING." + mDevice.name)
                }
                //case3: breaking a bond
                if (mDevice.bondState == BluetoothDevice.BOND_NONE) {
                    Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.")
                    Log.e(TAG, "BroadcastReceiver: BOND_NONE." + mDevice.name)
                }
            }
        }
    }

    private val aclConnectedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_ACL_CONNECTED) {
                val mDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Toast.makeText(context, "Connected to: " + mDevice?.name, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val aclDisconnectedRequestedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED) {
                val mDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Toast.makeText(context, "Disconnected requested: " + mDevice?.name, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val aclDisconnectedReciever: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
                val mDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Toast.makeText(context, "Disconnected: " + mDevice?.name, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        btAdapter.cancelDiscovery()
        unregisterReceiver(discoverReceiver)
    }

}
