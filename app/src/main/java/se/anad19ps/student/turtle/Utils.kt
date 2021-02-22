package se.anad19ps.student.turtle

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast


class Utils() {

    object UtilsObject{
        fun bluetoothSendString(string : String) {
            SelectBluetoothDeviceActivity.messageToSend = string
            SelectBluetoothDeviceActivity.sendBluetoothDataIsReady = true
        }

        fun showToast(string : String, uicontext : Context) {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                Toast.makeText(uicontext, string, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

