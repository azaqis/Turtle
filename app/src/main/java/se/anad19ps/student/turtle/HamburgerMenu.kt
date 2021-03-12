package se.anad19ps.student.turtle

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import com.google.android.material.navigation.NavigationView


class HamburgerMenu(){

    companion object{
        private enum class ActivityNumber{
            SELECT_BLUETOOTH_DEVICE_ACTIVITY,
            PROGRAMMING_ACTIVITY,
            CONTROLLER_ACTIVITY,
            MANAGE_CUSTOM_DRAG_DROP_BLOCKS_ACTIVITY,
            NOT_DEFINED
        }
        private var currentActivity : ActivityNumber = ActivityNumber.NOT_DEFINED

        private lateinit var toggle: ActionBarDrawerToggle
    }

    fun setUpHamburgerMenu(
        con: Context,
        navView: NavigationView,
        drawerLayout: DrawerLayout,
        hamburgerMenuIcon: ImageView
    ) {

        toggle = ActionBarDrawerToggle(
            con as Activity?,
            drawerLayout,
            R.string.open,
            R.string.close
        )

        fun setCurrentActivity(){
            currentActivity = when (con){
                is SelectBluetoothDeviceActivity -> ActivityNumber.SELECT_BLUETOOTH_DEVICE_ACTIVITY
                is ProgrammingActivity -> ActivityNumber.PROGRAMMING_ACTIVITY
                is ControllerActivity -> ActivityNumber.CONTROLLER_ACTIVITY
                is ManageCustomDragDropBlocksActivity -> ActivityNumber.MANAGE_CUSTOM_DRAG_DROP_BLOCKS_ACTIVITY
                else -> ActivityNumber.NOT_DEFINED
            }
        }

        fun changeIntent(it : MenuItem){
            setCurrentActivity()

            when (it.itemId) {
                R.id.drawerItemBluetoothConnect -> {
                    if(currentActivity != ActivityNumber.SELECT_BLUETOOTH_DEVICE_ACTIVITY){
                        val intent = Intent(con, SelectBluetoothDeviceActivity::class.java)
                        startActivity(con, intent, null)
                        con.finish()
                    }
                    else
                        drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.drawerItemProgramming -> {
                    if(currentActivity != ActivityNumber.PROGRAMMING_ACTIVITY){
                        val intent = Intent(con, ProgrammingActivity::class.java)
                        startActivity(con, intent, null)
                        con.finish()
                    }
                    else
                        /*
                        if(){
                            val dialogWantToSave = android.app.AlertDialog.Builder(con)
                            dialogWantToSave.setTitle("Leaving unsaved project")
                            dialogWantToSave.setMessage("Do you really want to leave this unsaved project. All progress will be lost if you continue!")
                            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                                when (which) {
                                    DialogInterface.BUTTON_NEGATIVE -> {
                                        drawerLayout.closeDrawer(GravityCompat.START)
                                    }
                                    DialogInterface.BUTTON_POSITIVE -> {
                                        val intent = Intent(con, ProgrammingActivity::class.java)
                                        startActivity(con, intent, null)
                                        con.finish()
                                    }
                                }
                            }
                            dialogWantToSave.setPositiveButton(R.string.change_view, dialogClickListener)
                            dialogWantToSave.setNegativeButton(R.string.stay, dialogClickListener)
                            dialogWantToSave.create().show()
                        }

                         */
                        drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.drawerItemRemoteController -> {
                    if(currentActivity != ActivityNumber.CONTROLLER_ACTIVITY){
                        val intent = Intent(con, ControllerActivity::class.java)
                        startActivity(con, intent, null)
                        con.finish()
                    }
                    else
                        drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.drawerItemManageCommands -> {
                    if(currentActivity != ActivityNumber.MANAGE_CUSTOM_DRAG_DROP_BLOCKS_ACTIVITY){
                        val intent = Intent(con, ManageCustomDragDropBlocksActivity::class.java)
                        startActivity(con, intent, null)
                        con.finish()
                    }
                    else
                        drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
        }

        fun checkIfInProgrammingAndProjectActive(it : MenuItem){
            if (con is ProgrammingActivity && Utils.UtilsObject.programmingIsTraversingList()){
                val dialogWantToSave = android.app.AlertDialog.Builder(con)
                dialogWantToSave.setTitle(R.string.changing_activity_while_traversinglist)
                dialogWantToSave.setMessage(R.string.change_activity_or_stay_traverselist)
                val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_NEGATIVE -> {

                        }
                        DialogInterface.BUTTON_POSITIVE -> {
                            Utils.UtilsObject.stopTraversingList()
                            changeIntent(it)
                        }
                    }
                }
                dialogWantToSave.setPositiveButton(R.string.change_view, dialogClickListener)
                dialogWantToSave.setNegativeButton(R.string.stay, dialogClickListener)
                dialogWantToSave.create().show()
            }
            else{
                changeIntent(it)
            }
        }


        drawerLayout.addDrawerListener(toggle)

        drawerLayout.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(view: View, v: Float) {
                drawerLayout.visibility = View.VISIBLE
                /*
                Eftersom drawer döljs och visas fungerar inte slide funktionen. Detta borde lösas här, helst  genom att anropa orginalkoden för slide men innan det göra den vissible. Detta fick jag ej dock att fungera!

                super.onDrawerSlide(view, v)
                */
            }
            override fun onDrawerOpened(view: View) {
            }
            override fun onDrawerClosed(view: View) {
                drawerLayout.visibility = View.INVISIBLE
            }

            override fun onDrawerStateChanged(i: Int) {}
        })

        toggle.syncState()
        drawerLayout.visibility = View.INVISIBLE

        hamburgerMenuIcon.setOnClickListener {
            drawerLayout.visibility = View.VISIBLE
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener {
            checkIfInProgrammingAndProjectActive(it)
            true
        }
    }
}
