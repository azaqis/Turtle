package se.anad19ps.student.turtle

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_select_bluetooth_device.*


class Utils() {

    object UtilsObject{
        private var toastMessage : Toast? = null

        //This function sends strings to the module which in turn sends it into the robot through UART
        fun bluetoothSendString(string : String, uicontext: Context) {
            if(SelectBluetoothDeviceActivity.bluetoothConnectionThreadActive){
                SelectBluetoothDeviceActivity.messageToSend = string
                SelectBluetoothDeviceActivity.sendBluetoothDataIsReady = true
            }
            else
                showUpdatedToast("You are not connected to a bluetooth device", uicontext)
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

