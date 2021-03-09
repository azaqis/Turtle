package se.anad19ps.student.turtle

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.Settings.Global.getString
import android.widget.Toast


class Utils() {

    object UtilsObject{

        private var toastMessage : Toast? = null

        //This function sends strings to the module which in turn sends it into the robot through UART
        fun bluetoothSendString(string : String, uicontext: Context) {
            if(SelectBluetoothDeviceActivity.bluetoothConnectionThreadActive)
                SelectBluetoothDeviceActivity.clientThread.writeToConnectedDevice(string)
            else
                showUpdatedToast(uicontext.getString(R.string.not_connected_to_bt_device_warning), uicontext)
        }

        fun isBluetoothConnectionThreadActive() : Boolean{
            return SelectBluetoothDeviceActivity.bluetoothConnectionThreadActive
        }

        fun bluetoothReceiveStringReady(recievedString : String){
            ControllerDebugFragment().addStringToDebugList(recievedString)
        }

        //This function toasts a message in the UI-thread only needing string and context. This also changes the current toast to the newest toast to not stack toasts
        fun showUpdatedToast(string : String, uicontext : Context) {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                if(toastMessage!=null)
                    toastMessage!!.cancel()
                toastMessage = Toast.makeText(uicontext, string, Toast.LENGTH_SHORT)
                toastMessage!!.show()
            }
        }
    }
}

