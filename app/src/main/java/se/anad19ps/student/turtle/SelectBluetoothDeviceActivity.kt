package se.anad19ps.student.turtle

/*import android.app.ProgressDialog
import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.widget.ListView
import java.io.IOException
import java.util.**/
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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


class SelectBluetoothDeviceActivity : AppCompatActivity() {

    companion object{
        var devices = mutableListOf<BluetoothDevice>()
        //The two lists below are containing the same thing if the devices have a name, but the one above has both integrated, why create a new one?
        val scannedDevicesList: ArrayList<BluetoothDevice> = ArrayList()
        val scannedDevicesNameList: ArrayList<String> = ArrayList()

        private const val REQUEST_CODE_ENABLE_BT: Int = 1
        private const val TAG = "BluetoothActivity"

        //BT Adapter
        lateinit var  btAdapter: BluetoothAdapter
        /*lateinit var  btSocket : BluetoothSocket

        lateinit var btAddress : String
        lateinit var  btProgress : ProgressDialog

        var isConnected : Boolean = false

        private var my_UUID: UUID = UUID.fromString("d989d859-9186-452a-8c43-0059a3599ac5")

        lateinit var btRepos : BluetoothRepository*/
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

        //Do we need this everywhere?
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, scannedDevicesList)
        selectScannedDeviceList.adapter = adapter

        //val selectScannedDeviceListView : ListView = findViewById(R.id.selectScannedDeviceList)

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
                    discoverBluetoothDevices()
                }
            }
            else
                //Clear list to show that even if there were BT-devices, you shouldnt be able to connect to them
                clearScannedList()
        }
        val filterBond = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(pairingReceiver, filterBond)

        val filterDiscover = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoverReceiver, filterDiscover)

    }

    override fun onStart(){
        super.onStart()

        //Check permissions
        checkBluetoothAvailability()
        checkFineLocationAllowed()

        //OBS! DOESNT WORK BUT NEEDS TO WORK, MAKES APP CRASH WHEN CHANGING VIEW, LATEINIT FAULT?
        /*
        if(btAdapter.isEnabled){
            discoverBluetoothDevices()
            displayPairedDevices()
        }*/

        //Clear list and start to discover to show all devices without pressing any button when getting into this activity
        clearScannedList()
        discoverBluetoothDevices()
        displayPairedDevices()
    }

    private fun clearScannedList(){
        scannedDevicesNameList.clear()
        scannedDevicesList.clear()
        devices.clear()

        //Do we need this?
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, scannedDevicesList)
        selectScannedDeviceList.adapter = adapter
    }

    private fun checkBluetoothAvailability(){
        //If there is no adapter, its not possible with BT
        if(!btAdapter.isEnabled){
            //Permission dialog appears
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BT)
        }
    }

    private fun checkFineLocationAllowed(){
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
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            REQUEST_CODE_ENABLE_BT ->
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show()

                    //Is this needed here?
                    scannedDevicesList.clear()
                    devices.clear()

                    //Is this needed here?
                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        scannedDevicesList
                    )
                    selectScannedDeviceList.adapter = adapter

                    //Is this needed here?
                    discoverBluetoothDevices()
                    displayPairedDevices()

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


    private fun displayPairedDevices(){
        //Make a set (type of list?) that holds all of the pre-paired devices
        val pairedDevices : Set<BluetoothDevice> = btAdapter.bondedDevices

        //One list containing the devices and one containing names (would look better with one that you could access both from, two needed now because of code further down)
        val listOfPairedDevices: ArrayList<BluetoothDevice> = ArrayList()
        val listOfPairedDevicesNames: ArrayList<String> = ArrayList()

        //If there are devices that have been paired to before, do code below
        if(pairedDevices.isNotEmpty()){
            //First, get all of the devices to a list
            for(device : BluetoothDevice in pairedDevices){
                listOfPairedDevices.add(device)
            }
            //Second, get names. IF name is null, use address as name which is used later for adapter
            for(device : BluetoothDevice in listOfPairedDevices){
                if(device.name != null)
                    listOfPairedDevicesNames.add(device.name)
                else
                    listOfPairedDevicesNames.add(device.address)
            }
        }
        else{
            Toast.makeText(this, "No devices found", Toast.LENGTH_SHORT).show()
        }

        //Make adapter that shows the names in the listview
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            listOfPairedDevicesNames
        )
        selectPairedDeviceList.adapter = adapter
        selectPairedDeviceList.onItemClickListener = AdapterView.OnItemClickListener { _, v, position, _ ->
            val device: BluetoothDevice = listOfPairedDevices[position]
            //Here the app tries to connect/bond with the device chosen in the listview
            tryBonding(device, v)
        }

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

    fun addDeviceToList(btDevice: BluetoothDevice){
        //If a device is found, add it to the scannedDevicesList
        scannedDevicesList.add(btDevice)
        //Primarily, show device name, but some only have an address, then show the address
        if(btDevice.name != null)
            scannedDevicesNameList.add(btDevice.name)
        else
            scannedDevicesNameList.add(btDevice.address)

        //Adapter for showing the scanned devices in the listview
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            scannedDevicesNameList
        )
        selectScannedDeviceList.adapter = adapter
        selectScannedDeviceList.onItemClickListener = AdapterView.OnItemClickListener { _, v, position, _ ->
            val device: BluetoothDevice = scannedDevicesList[position]
            //Here the app tries to connect/bond with the device chosen in the listview
            tryBonding(device, v)

            //ConnectToDevice(this).execute()
        }
    }

    private fun tryBonding(device: BluetoothDevice, v: View){
        Toast.makeText(v.context, "Trying to connect to device: " + device.name, Toast.LENGTH_SHORT).show()
        //create.bond() doesnt work for all APIs
        Log.d(TAG, "Trying to pair with " + device.name)
        device.createBond()
    }

    private val discoverReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    if (device != null && !devices.contains(device)) {
                        devices.add(device)
                        addDeviceToList(device)
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

    override fun onDestroy() {
        super.onDestroy()
        btAdapter.cancelDiscovery()
        unregisterReceiver(discoverReceiver)
    }


    /*private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
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
   }*/
}
