package se.anad19ps.student.turtle

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Toast


class Utils {

    object UtilsObject {

        private var toastMessage: Toast? = null

        //This function sends strings to the module which in turn sends it into the robot through UART
        fun bluetoothSendString(string: String, uiContext: Context) {
            if (SelectBluetoothDeviceActivity.bluetoothConnectionThreadActive)
                SelectBluetoothDeviceActivity.clientThread.writeToConnectedDevice(string)
            else
                showUpdatedToast(
                    uiContext.getString(R.string.not_connected_to_bt_device_warning),
                    uiContext
                )
        }

        fun isBluetoothConnectionThreadActive(): Boolean {
            return SelectBluetoothDeviceActivity.bluetoothConnectionThreadActive
        }

        fun bluetoothReceiveStringReady(receivedString: String) {
            ControllerDebugFragment().addStringToDebugList(receivedString)
        }

        /*This function toasts a message in the UI-thread only needing string and context.
        This also changes the current toast to the newest toast to not stack toasts*/
        fun showUpdatedToast(string: String, uicontext: Context) {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                if (toastMessage != null)
                    toastMessage!!.cancel()
                toastMessage = Toast.makeText(uicontext, string, Toast.LENGTH_SHORT)
                toastMessage!!.show()
            }
        }

        fun programmingIsTraversingList(): Boolean {
            return ProgrammingActivity.traversingList
        }

        fun stopTraversingList() {
            ProgrammingActivity.traversingList = false
        }

        fun vibrate(view: View) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }

        fun getIsProjectModified(): Boolean {
            return ProgrammingActivity.isProjectModified()
        }
    }
}

