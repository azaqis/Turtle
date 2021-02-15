package se.anad19ps.student.turtle

class BluetoothRepository{
    private val devices = mutableListOf<BluetoothDeviceInfo>()

    fun addBluetoothDevice(bluetoothDeviceInfo : BluetoothDeviceInfo){
        val btDeviceInfo: BluetoothDeviceInfo? = devices.find {
            it.adress == bluetoothDeviceInfo.adress
        }

        if(btDeviceInfo != null){
            devices.add(bluetoothDeviceInfo)
        }
    }

    fun clear(){
        devices.clear()
    }

    fun getNameByAdress(adress : String) : String{
        val btDeviceInfo: BluetoothDeviceInfo? = devices.find {
            it.adress == adress
        }

        if(btDeviceInfo != null){
            return btDeviceInfo!!.name
        }

        return "NO DEVICE FOUND"
    }

    fun isEmpty() : Boolean{
        if(devices.isEmpty()){
            return false
        }
        return true
    }

    fun getAmountOfDevices() : Int{
        return devices.size
    }

    fun getDeviceByIndex(index : Int) : BluetoothDeviceInfo{
        return devices[index]
    }


}