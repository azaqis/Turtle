package se.anad19ps.student.turtle

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_select_bluetooth_device.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*


class SelectBluetoothDeviceActivity : AppCompatActivity() {

    private val REQUEST_CODE_ENABLE_BT: Int = 1;

    //BT Adapter
    lateinit var  btAdapter: BluetoothAdapter

    //Array adapter for paired devices
    private var btArrayAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_bluetooth_device)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        //Init BT Adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter()

        //Check if BT is avaliable on device
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

        refreshBluetoothDevicesButton.setOnClickListener(){
            displayPairedDevices()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            REQUEST_CODE_ENABLE_BT ->
                if (requestCode == Activity.RESULT_OK){
                    Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "Bluetooth is not enabled, please try again", Toast.LENGTH_SHORT).show()
                }
        }
        super.onActivityResult(requestCode, resultCode, data)
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
        selectDeviceList.adapter = adapter
    }


}