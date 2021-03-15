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


@Suppress("UNCHECKED_CAST")
class SelectBluetoothDeviceActivity : AppCompatActivity() {

    companion object {
        val scannedDevicesList: ArrayList<BluetoothDevice> = ArrayList()
        val pairedDevicesList: ArrayList<BluetoothDevice> = ArrayList()
        val scannedDevicesNameList: ArrayList<String> = ArrayList()
        val pairedDevicesNameList: ArrayList<String> = ArrayList()

        private const val REQUEST_CODE_ENABLE_BT: Int = 1
        private const val TAG = "BluetoothActivity"

        lateinit var bluetoothAdapter: BluetoothAdapter
        lateinit var clientThread: BluetoothClient
        lateinit var scannedDevicesNameListViewAdapter: ArrayAdapter<String>
        lateinit var pairedDevicesNameListViewAdapter: ArrayAdapter<String>

        var bluetoothConnectionThreadActive: Boolean = false
        var inputBuffer = ByteArray(1024)
        var messageReceived: String? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_bluetooth_device)

        if (savedInstanceState != null) {
            val savedState =
                savedInstanceState.getParcelable<SelectBluetoothDeviceSavedState>("savedStateObject")

            if (savedState != null) {
                scannedDevicesList.clear()
                scannedDevicesList.addAll(savedState.scannedDevicesList)
                pairedDevicesList.clear()
                pairedDevicesList.addAll(savedState.pairedDevicesList)

                scannedDevicesNameList.clear()
                scannedDevicesNameList.addAll(savedState.scannedDevicesNameList)
                pairedDevicesNameList.clear()
                pairedDevicesNameList.addAll(savedState.pairedDevicesNameList)

                scannedDevicesNameListViewAdapter.notifyDataSetChanged()
                pairedDevicesNameListViewAdapter.notifyDataSetChanged()
            }
        }

        hideProgressShowButton()

        HamburgerMenu().setUpHamburgerMenu(
            this,
            drawer_layout_nav_view,
            drawer_layout,
            hamburger_menu_icon
        )

        setupAdapters()
        setupFilters()

        select_bluetooth_device_button_refresh.setOnClickListener {
            onRefreshButtonClicked()
        }
    }

    private fun onRefreshButtonClicked() {
        checkBluetoothAvailability()
        checkFineLocationAllowed()

        //Only scan if BT is enabled, worthless otherwise
        if (bluetoothAdapter.isEnabled) {
            //If it is already scanning, dont cancel and scan again, just wait for it to finish scanning.
            //If it is done, then scan again
            if (bluetoothAdapter.isDiscovering) {
                Utils.UtilsObject.showUpdatedToast(
                    getString(R.string.please_wait_still_scanning),
                    this
                )
            } else {
                clearBothScannedLists()
                clearBothPairedList()
                updatePairedDevicesList()
                discoverBluetoothDevices()
            }
        } else
        //Clear list to show that even if there were BT-devices, you shouldn't be able to connect to them
            clearBothScannedLists()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val scannedDevicesListClone: ArrayList<BluetoothDevice> =
            scannedDevicesList.clone() as ArrayList<BluetoothDevice>
        val scannedDevicesNameListClone: ArrayList<String> =
            scannedDevicesNameList.clone() as ArrayList<String>
        val pairedDevicesListClone: ArrayList<BluetoothDevice> =
            pairedDevicesList.clone() as ArrayList<BluetoothDevice>
        val pairedDevicesNameListClone: ArrayList<String> =
            pairedDevicesNameList.clone() as ArrayList<String>
        val saveState =
            SelectBluetoothDeviceSavedState(
                scannedDevicesListClone,
                scannedDevicesNameListClone,
                pairedDevicesListClone,
                pairedDevicesNameListClone
            )
        outState.putParcelable("savedStateObject", saveState)
    }

    private fun setupAdapters() {
        //Init for adapter for paired devices list view
        pairedDevicesNameListViewAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            pairedDevicesNameList
        )
        select_bluetooth_device_list_view_paired_devices.adapter = pairedDevicesNameListViewAdapter

        select_bluetooth_device_list_view_paired_devices.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val device: BluetoothDevice = pairedDevicesList[position]
                //Here the app tries to connect with the device chosen in the listview
                Utils.UtilsObject.showUpdatedToast(
                    getString(R.string.connecting_to) + ": " + device.name + "...",
                    this
                )

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
        select_bluetooth_device_list_view_scanned_devices.adapter =
            scannedDevicesNameListViewAdapter

        select_bluetooth_device_list_view_scanned_devices.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val device: BluetoothDevice = scannedDevicesList[position]
                //Here the app tries to bond with the device chosen in the listview
                showProgressHideButton()
                bondToDevice(device)
            }

        //Init BT Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    private fun setupFilters() {
        //Intent-filters
        val filterBond = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(pairingReceiver, filterBond)
        val filterDiscover = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoverReceiver, filterDiscover)
        val filterACLConnected = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
        registerReceiver(aclConnectedReceiver, filterACLConnected)
        val filterACLDisconnectRequested =
            IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
        registerReceiver(aclDisconnectedRequestedReceiver, filterACLDisconnectRequested)
        val filterACLDisconnected = IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        registerReceiver(aclDisconnectedReciever, filterACLDisconnected)
    }

    override fun onStart() {
        super.onStart()

        //Check permissions
        checkBluetoothAvailability()
        if (bluetoothAdapter.isEnabled && checkFineLocationAllowed()) {
            discoverBluetoothDevices()
            updatePairedDevicesList()
        }
    }

    private fun updatePairedDevicesList() {
        val devices: ArrayList<BluetoothDevice>? = getInternalPairedDevicesList()

        if (devices != null) {
            for (device: BluetoothDevice in devices) {
                if (!pairedDevicesList.contains(device)) {
                    pairedDevicesList.add(device)
                    if (device.name != null)
                        pairedDevicesNameList.add(device.name)
                    else
                        pairedDevicesNameList.add(device.address)
                }
            }
            select_bluetooth_device_list_view_paired_devices.invalidateViews()
            pairedDevicesNameListViewAdapter.notifyDataSetChanged()
        } else
            Utils.UtilsObject.showUpdatedToast(getString(R.string.no_paired_device_warning), this)
    }

    private fun clearBothScannedLists() {
        scannedDevicesList.clear()
        scannedDevicesNameList.clear()
        select_bluetooth_device_list_view_scanned_devices.invalidateViews()
    }

    private fun clearBothPairedList() {
        pairedDevicesList.clear()
        pairedDevicesNameList.clear()
        select_bluetooth_device_list_view_paired_devices.invalidateViews()
    }

    private fun checkBluetoothAvailability() {
        //If there is no adapter, its not possible with BT
        if (!bluetoothAdapter.isEnabled) {
            //Permission dialog appears
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BT)
        }
    }

    private fun checkFineLocationAllowed(): Boolean {
        //Permission for fine location which needs to be checked if you run a later API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            showLocationPrompt()
            if (ContextCompat.checkSelfPermission(
                    baseContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
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
        when (requestCode) {
            REQUEST_CODE_ENABLE_BT ->
                if (resultCode == Activity.RESULT_OK) {
                    Utils.UtilsObject.showUpdatedToast(
                        getString(R.string.bluetooth_is_enabled),
                        this
                    )
                    //Needed to show paired devices after allowing BT permission on view open
                    updatePairedDevicesList()
                    discoverBluetoothDevices()
                } else
                    Utils.UtilsObject.showUpdatedToast(
                        getString(R.string.bluetooth_is_not_enabled_warning),
                        this
                    )

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


    private fun getInternalPairedDevicesList(): ArrayList<BluetoothDevice>? {
        //Make a set that holds all of the pre-paired devices
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices

        val listOfPairedDevices: ArrayList<BluetoothDevice> = ArrayList()

        //If there are devices that have been paired to before, do code below
        if (pairedDevices.isNotEmpty()) {
            //First, get all of the devices to a list
            for (device: BluetoothDevice in pairedDevices) {
                listOfPairedDevices.add(device)
            }
            return listOfPairedDevices
        } else
            Utils.UtilsObject.showUpdatedToast(getString(R.string.no_devices_found), this)

        return null
    }

    private fun discoverBluetoothDevices() {
        //If the adapter is trying to discover, cancel it first and then start
        //(as you should do cited from developer site)
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()

        val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoverReceiver, discoverDevicesIntent)
    }

    private fun addDeviceToScannedList(device: BluetoothDevice) {
        //If a device is found, add it to the scannedDevicesList
        if (!scannedDevicesList.contains(device)) {
            scannedDevicesList.add(device)

            if (device.name != null)
                scannedDevicesNameList.add(device.name)
            else
                scannedDevicesNameList.add(device.address)

            scannedDevicesNameListViewAdapter.notifyDataSetChanged()
        }
    }

    private fun bondToDevice(device: BluetoothDevice) {
        //Dont bond if it is already bonded, then connect instead
        if (!pairedDevicesList.contains(device)) {
            Utils.UtilsObject.showUpdatedToast(
                getString(R.string.trying_to_bond_to_device) + ": " + device.name,
                this
            )
            Log.d(TAG, "Trying to bond with " + device.name)

            device.createBond()
        } else {
            Utils.UtilsObject.showUpdatedToast(
                getString(R.string.connecting_to) + ": " + device.name + "...",
                this
            )
            Log.d(TAG, "Trying to connect to: " + device.name)

            clientThread = BluetoothClient(device, this)
            clientThread.start()
        }
    }

    private val discoverReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
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
                //Case1: bonded already
                if (mDevice!!.bondState == BluetoothDevice.BOND_BONDED) {
                    hideProgressShowButton()
                    Utils.UtilsObject.showUpdatedToast(
                        getString(R.string.connected_to) + ": " + mDevice.name,
                        context
                    )
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.")
                    Log.e(TAG, "BroadcastReceiver: BOND_BONDED." + mDevice.name)
                    bluetoothAdapter.cancelDiscovery()
                }
                //Case2: creating a bond
                if (mDevice.bondState == BluetoothDevice.BOND_BONDING) {
                    Utils.UtilsObject.showUpdatedToast(
                        getString(R.string.connecting_to) + ": " + mDevice.name + "...",
                        context
                    )
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.")
                    Log.e(TAG, "BroadcastReceiver: BOND_BONDING." + mDevice.name)
                }
                //Case3: breaking a bond
                if (mDevice.bondState == BluetoothDevice.BOND_NONE) {
                    hideProgressShowButton()
                    Utils.UtilsObject.showUpdatedToast(
                        getString(R.string.connection_failed),
                        context
                    )
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
                val mDevice =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Utils.UtilsObject.showUpdatedToast(
                    getString(R.string.connected_to) + ": " + mDevice?.name,
                    context
                )
            }
        }
    }

    private val aclDisconnectedRequestedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED) {
                hideProgressShowButton()
                val mDevice =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Utils.UtilsObject.showUpdatedToast(
                    getString(R.string.disconnection_requested) + ": " + mDevice?.name,
                    context
                )
            }
        }
    }

    private val aclDisconnectedReciever: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
                hideProgressShowButton()
                bluetoothConnectionThreadActive = false
                val mDevice =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Utils.UtilsObject.showUpdatedToast(
                    getString(R.string.disconnected) + ": " + mDevice?.name,
                    context
                )
            }
        }
    }

    private fun showLocationPrompt() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(this).checkLocationSettings(
                builder.build()
            )

        result.addOnCompleteListener { task ->
            try {
                task.getResult(ApiException::class.java)
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

    private fun showProgressHideButton() {
        select_bluetooth_device_progress_bar.visibility = View.VISIBLE
        select_bluetooth_device_button_refresh.visibility = View.INVISIBLE
    }

    private fun hideProgressShowButton() {
        select_bluetooth_device_progress_bar.visibility = View.INVISIBLE
        select_bluetooth_device_button_refresh.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothAdapter.cancelDiscovery()
        unregisterReceiver(discoverReceiver)
    }
}
