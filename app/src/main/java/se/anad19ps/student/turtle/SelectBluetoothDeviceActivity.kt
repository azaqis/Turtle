package se.anad19ps.student.turtle

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_select_bluetooth_device.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*



class SelectBluetoothDeviceActivity : AppCompatActivity() {

    lateinit var bluetoothAdapter : BluetoothAdapter? = null
    lateinit var pairedDevices : Set<BluetoothDevice>
    val REQUEST_ENABLE_BLUETOOTH = 1

    companion object{
        val EXTRA_ADRESS : String = "Device_address"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_bluetooth_device)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter == null){
            Toast.makeText(this,"Error, Bluetooth is not enable. Device may not have support for Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        if(!bluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        refreshBlutoothDevicesButton.setOnClickListener{paieredDeviceList()}


    }

    private fun paieredDeviceList(){
        pairedDevices = bluetoothAdapter!!.bondedDevices
        val list : ArrayList<BluetoothDevice> = ArrayList()

        if(!pairedDevices.isEmpty()){
            for(device : BluetoothDevice in pairedDevices){
                list.add(device)
            }
        }
        else{
            Toast.makeText(this,"No devices found", Toast.LENGTH_SHORT).show()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        selectDeviceList.adapter = adapter
        selectDeviceList.onItemClickListener = AdapterView.OnItemClickListener {_, _, position, _ ->
            val device : BluetoothDevice = list[position]
            val address : String = device.address

            val intent = Intent(this, ControlActivity::class.java)
            intent.putExtra(EXTRA_ADRESS, address)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BLUETOOTH){
            if(resultCode == Activity.RESULT_OK){
                if(bluetoothAdapter!!.isEnabled){
                    Toast.makeText(this,"Bluetooth enabled", Toast.LENGTH_SHORT).show()
                } else{
                    Toast.makeText(this,"Bluetooth disabled", Toast.LENGTH_SHORT).show()
                }
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this,"Bluetooth enabling canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}