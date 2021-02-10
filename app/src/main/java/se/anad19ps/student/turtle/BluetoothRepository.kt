package se.anad19ps.student.turtle

class BluetoothRepository {
    private val devices = mutableListOf<BluetoothDevice>()

    fun addBluetoothDevice(bluetoothDevice : BluetoothDevice){

        val btDevice: BluetoothDevice? = devices.find {
            it.adress == bluetoothDevice.adress
        }

        if(btDevice != null){
            devices.add(bluetoothDevice)
        }
    }

    fun clear(){
        devices.clear()
    }

    fun getNameByAdress(adress : String) : String{
        val btDevice: BluetoothDevice? = devices.find {
            it.adress == adress
        }

        if(btDevice != null){
            return btDevice!!.name
        }

        return "NO DEVICE FOUND"
    }
}