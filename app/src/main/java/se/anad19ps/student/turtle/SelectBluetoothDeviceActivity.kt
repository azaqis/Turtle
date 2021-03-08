package se.anad19ps.student.turtle

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_select_bluetooth_device.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*
import java.io.IOException
import java.io.OutputStream


class SelectBluetoothDeviceActivity : AppCompatActivity() {

    companion object{
        val scannedDevicesList: ArrayList<BluetoothDevice> = ArrayList()
        val pairedDevicesList: ArrayList<BluetoothDevice> = ArrayList()
        val scannedDevicesNameList: ArrayList<String> = ArrayList()
        val pairedDevicesNameList: ArrayList<String> = ArrayList()

        private const val REQUEST_CODE_ENABLE_BT: Int = 1
        private const val TAG = "BluetoothActivity"

        //Adapters
        lateinit var btAdapter: BluetoothAdapter
        lateinit var scannedDevicesNameListViewAdapter: ArrayAdapter<String>
        lateinit var pairedDevicesNameListViewAdapter: ArrayAdapter<String>

        var bluetoothConnectionThreadActive : Boolean = false
        var inputBuffer = ByteArray(1024)
        var messageRecieved : String? = null
        lateinit var clientThread : BluetoothClient
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_bluetooth_device)



        hideProgressShowButton()
        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)


        initAdapters()
        initFilters()


        refreshBluetoothDevicesButton.setOnClickListener{
            //Check permissions if it is possible to scan
            checkBluetoothAvailability()
            checkFineLocationAllowed()

            //Only scan if BT is enabled, worthless otherwise
            if(btAdapter.isEnabled)
            {
                //If it is already scanning, dont cancel and scan again, just wait for it to finish scanning. If it is done, then scan again
                if(btAdapter.isDiscovering) {
                    Utils.UtilsObject.showUpdatedToast("Please wait, still scanning...", this)
                }
                else{
                    clearBothScannedLists()
                    clearBothPairedList()
                    updatePairedDevicesList()
                    discoverBluetoothDevices()
                }
            }
            else
                //Clear list to show that even if there were BT-devices, you shouldnt be able to connect to them
                clearBothScannedLists()
        }
    }

    private fun initAdapters(){
        //Init for adapter for paired devices list view
        pairedDevicesNameListViewAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            pairedDevicesNameList
        )
        selectPairedDeviceList.adapter = pairedDevicesNameListViewAdapter

        selectPairedDeviceList.onItemClickListener = AdapterView.OnItemClickListener { _, v, position, _ ->
            val device: BluetoothDevice = pairedDevicesList[position]
            //Here the app tries to connect with the device chosen in the listview
            Utils.UtilsObject.showUpdatedToast("Connecting to: " + device.name + "...", this)

            showProgressHideButton()
            clientThread = BluetoothClient(device, this)
            clientThread.start()
        }


        //Init for adapter for scanned devices list view
        scannedDevicesNameListViewAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            scannedDevicesNameList
        )
        selectScannedDeviceList.adapter = scannedDevicesNameListViewAdapter

        selectScannedDeviceList.onItemClickListener = AdapterView.OnItemClickListener { _, v, position, _ ->
            val device: BluetoothDevice = scannedDevicesList[position]
            //Here the app tries to bond with the device chosen in the listview
            showProgressHideButton()
            tryBonding(device, v)
        }

        //Init BT Adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    private fun initFilters(){
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
            clearBothPairedList()
            clearBothScannedLists()
            discoverBluetoothDevices()
            updatePairedDevicesList()
        }
    }

    private fun updatePairedDevicesList(){
        val devices: ArrayList<BluetoothDevice>? = getPairedDevices()

        if(devices != null){
            for(device : BluetoothDevice in devices){
                pairedDevicesList.add(device)
                if(device.name != null)
                    pairedDevicesNameList.add(device.name)
                else
                    pairedDevicesNameList.add(device.address)
            }
            selectPairedDeviceList.invalidateViews()
            pairedDevicesNameListViewAdapter.notifyDataSetChanged()
        }
        else
            Utils.UtilsObject.showUpdatedToast("There are no paired devices on this device", this)
    }

    private fun clearBothScannedLists(){
        scannedDevicesList.clear()
        scannedDevicesNameList.clear()
        selectScannedDeviceList.invalidateViews()
    }

    private fun clearBothPairedList(){
        pairedDevicesList.clear()
        pairedDevicesNameList.clear()
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
            showLocationPrompt()
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
        //If version is lower than Q, dont need to check. Will always allow in that case
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            REQUEST_CODE_ENABLE_BT ->
                if (resultCode == Activity.RESULT_OK) {
                    Utils.UtilsObject.showUpdatedToast("Bluetooth is enabled", this)
                    //Needed to show paired devices after allowing BT permission on view open
                    updatePairedDevicesList()
                    discoverBluetoothDevices()
                }
                else
                    Utils.UtilsObject.showUpdatedToast("Bluetooth is not enabled, please try again", this)

            LocationRequest.PRIORITY_HIGH_ACCURACY -> {
                if (resultCode == Activity.RESULT_OK) {
                    Log.e("Status: ", "On")
                } else {
                    Log.e("Status: ", "Off")
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun getPairedDevices() : ArrayList<BluetoothDevice>?{
        //Make a set that holds all of the pre-paired devices
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
        else
            Utils.UtilsObject.showUpdatedToast("No devices found", this)

        return null
    }

    private fun discoverBluetoothDevices(){
        //If the adapter is trying to discover, cancel it first and then start (as you should do cited from developer site)
        if(btAdapter.isDiscovering){
            btAdapter.cancelDiscovery()
        }
        btAdapter.startDiscovery()

        val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoverReceiver, discoverDevicesIntent)
    }

    private fun addDeviceToScannedList(device: BluetoothDevice){
        //If a device is found, add it to the scannedDevicesList
        scannedDevicesList.add(device)

        if(device.name != null)
            scannedDevicesNameList.add(device.name)
        else
            scannedDevicesNameList.add(device.address)

        scannedDevicesNameListViewAdapter.notifyDataSetChanged()











        /*//OBS! THIS DOES NOT UPDATE IF REMOVED EVEN IF IT EXISTS IN onCreate!!!!!
        scannedDevicesNameListViewAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            getDevicesNameArray(scannedDevicesList)
        )*/












    }

    private fun getDevicesNameArray(array: ArrayList<BluetoothDevice>) : ArrayList<String>{
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
        //Dont bond if it is already bonded, then connect instead
        if(!pairedDevicesList.contains(device)){
            Utils.UtilsObject.showUpdatedToast("Trying to bond to device: " + device.name, this)
            Log.d(TAG, "Trying to bond with " + device.name)

            device.createBond()
        }
        else{
            Utils.UtilsObject.showUpdatedToast("Connecting to: " + device.name + "...", this)
            Log.d(TAG, "Trying to connect to: " + device.name)

            clientThread = BluetoothClient(device, this)
            clientThread.start()
        }
    }


    class BluetoothClient(device: BluetoothDevice, uiactivity : Activity): Thread() {
        private val socket = device.createRfcommSocketToServiceRecord(device.uuids?.get(0)!!.uuid)
        private val activity = uiactivity
        private lateinit var outputStream : OutputStream

        override fun run() {
            bluetoothConnectionThreadActive = true
            try{
                Log.d(TAG, "Connecting")
                this.socket.connect()
            } catch (e : IOException){
                Log.d(TAG, "Connection failed")
                Utils.UtilsObject.showUpdatedToast("Connection failed, device may be already connected or out of range", activity.applicationContext)
                bluetoothConnectionThreadActive = false
                this.activity.runOnUiThread {
                    activity.progressBar.visibility = View.INVISIBLE
                    activity.refreshBluetoothDevicesButton.visibility = View.VISIBLE
                }
            }

            if(this.socket.isConnected){
                outputStream = this.socket.outputStream
                val inputStream = this.socket.inputStream
                var bytes : Int

                while(bluetoothConnectionThreadActive) {
                    try{
                        bytes = inputStream.read(inputBuffer)
                        messageRecieved = String(inputBuffer, 0, bytes)
                        Utils.UtilsObject.bluetoothRecieveStringReady(messageRecieved!!)
                    } catch (e : IOException){
                        Log.e(TAG, "Error reading Input Stream. ", e)
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
                Log.d(TAG, "Sent")
            } catch(e: IOException) {
                Log.d(TAG, "Cannot send", e)
            }
        }
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
                    hideProgressShowButton()
                    Utils.UtilsObject.showUpdatedToast("Connected to: " + mDevice.name, context)
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.")
                    Log.e(TAG, "BroadcastReceiver: BOND_BONDED." + mDevice.name)
                    btAdapter.cancelDiscovery()
                }
                //case2: creating a bond
                if (mDevice.bondState == BluetoothDevice.BOND_BONDING) {
                    Utils.UtilsObject.showUpdatedToast("Connecting to: " + mDevice.name + "...", context)
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.")
                    Log.e(TAG, "BroadcastReceiver: BOND_BONDING." + mDevice.name)
                }
                //case3: breaking a bond
                if (mDevice.bondState == BluetoothDevice.BOND_NONE) {
                    hideProgressShowButton()
                    Utils.UtilsObject.showUpdatedToast("Connection failed", context)
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
                hideProgressShowButton()
                val mDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Utils.UtilsObject.showUpdatedToast("Connected to: " + mDevice?.name, context)
            }
        }
    }

    private val aclDisconnectedRequestedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED) {
                hideProgressShowButton()
                val mDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Utils.UtilsObject.showUpdatedToast("Disconnected requested: " + mDevice?.name, context)
            }
        }
    }

    private val aclDisconnectedReciever: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
                hideProgressShowButton()
                val mDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Utils.UtilsObject.showUpdatedToast("Disconnected: " + mDevice?.name, context)
            }
        }
    }


    private fun showLocationPrompt() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val result: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(this).checkLocationSettings(
            builder.build()
        )

        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                // All location settings are satisfied. The client can initialize location
                // requests here.
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            // Cast to a resolvable exception.
                            val resolvable: ResolvableApiException =
                                exception as ResolvableApiException
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                this, LocationRequest.PRIORITY_HIGH_ACCURACY
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.

                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                    }
                }
            }
        }
    }

    private fun showProgressHideButton(){
        progressBar.visibility = View.VISIBLE
        refreshBluetoothDevicesButton.visibility = View.INVISIBLE
    }
    private fun hideProgressShowButton(){
        progressBar.visibility = View.INVISIBLE
        refreshBluetoothDevicesButton.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        btAdapter.cancelDiscovery()
        unregisterReceiver(discoverReceiver)
    }

}
