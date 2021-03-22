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


class HamburgerMenu {


    companion object {
        private enum class ActivityNumber {
            SELECT_BLUETOOTH_DEVICE_ACTIVITY,
            PROGRAMMING_ACTIVITY,
            CONTROLLER_ACTIVITY,
            MANAGE_CUSTOM_DRAG_DROP_BLOCKS_ACTIVITY,
            NOT_DEFINED
        }

        private var currentActivity: ActivityNumber = ActivityNumber.NOT_DEFINED

        private lateinit var actionBarToggle: ActionBarDrawerToggle
    }

    fun setUpHamburgerMenu(
        con: Context,
        navView: NavigationView,
        drawerLayout: DrawerLayout,
        hamburgerMenuIcon: ImageView
    ) {

        actionBarToggle = ActionBarDrawerToggle(
            con as Activity?,
            drawerLayout,
            R.string.open,
            R.string.close
        )

        fun updateAndSetCurrentActivity() {
            currentActivity = when (con) {
                is SelectBluetoothDeviceActivity -> ActivityNumber.SELECT_BLUETOOTH_DEVICE_ACTIVITY
                is ProgrammingActivity -> ActivityNumber.PROGRAMMING_ACTIVITY
                is ControllerActivity -> ActivityNumber.CONTROLLER_ACTIVITY
                is ManageCustomDragDropBlocksActivity -> ActivityNumber.MANAGE_CUSTOM_DRAG_DROP_BLOCKS_ACTIVITY
                else -> ActivityNumber.NOT_DEFINED
            }
        }

        fun changeIntent(it: MenuItem) {
            updateAndSetCurrentActivity()

            when (it.itemId) {
                R.id.drawerItemBluetoothConnect -> {
                    if (currentActivity == ActivityNumber.SELECT_BLUETOOTH_DEVICE_ACTIVITY) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        val intent = Intent(con, SelectBluetoothDeviceActivity::class.java)
                        startActivity(con, intent, null)
                        con.finish()
                    }
                }
                R.id.drawerItemProgramming -> {
                    if (currentActivity == ActivityNumber.PROGRAMMING_ACTIVITY) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        val intent = Intent(con, ProgrammingActivity::class.java)
                        startActivity(con, intent, null)
                        con.finish()
                    }
                }
                R.id.drawerItemRemoteController -> {
                    if (currentActivity == ActivityNumber.CONTROLLER_ACTIVITY) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        val intent = Intent(con, ControllerActivity::class.java)
                        startActivity(con, intent, null)
                        con.finish()
                    }
                }
                R.id.drawerItemManageCommands -> {
                    if (currentActivity == ActivityNumber.MANAGE_CUSTOM_DRAG_DROP_BLOCKS_ACTIVITY) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        val intent = Intent(con, ManageCustomDragDropBlocksActivity::class.java)
                        startActivity(con, intent, null)
                        con.finish()
                    }
                }
            }
        }

        fun checkIfInProgrammingAndProjectActive(it: MenuItem) {
            if (currentActivity == ActivityNumber.PROGRAMMING_ACTIVITY && Utils.UtilsObject.programmingIsTraversingList()) {
                val dialogWantToSave = android.app.AlertDialog.Builder(con)
                dialogWantToSave.setTitle(R.string.changing_activity_while_traversing_list)
                dialogWantToSave.setMessage(R.string.change_activity_or_stay_traverse_list)
                val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_NEGATIVE -> {
                            drawerLayout.closeDrawer(GravityCompat.START)
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
        }

        fun checkIfInProgrammingAndIfProjectIsModified(it: MenuItem) {
            if (currentActivity == ActivityNumber.PROGRAMMING_ACTIVITY && Utils.UtilsObject.getIsProjectModified()) {
                if (it.itemId != R.id.drawerItemProgramming) {
                    val dialogWantToSave = android.app.AlertDialog.Builder(con)
                    dialogWantToSave.setTitle(R.string.leaving_unsaved_project)
                    dialogWantToSave.setMessage(R.string.leaving_unsaved_project_warning)
                    val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                        when (which) {
                            DialogInterface.BUTTON_NEGATIVE -> {
                                drawerLayout.closeDrawer(GravityCompat.START)
                            }
                            DialogInterface.BUTTON_POSITIVE -> {
                                changeIntent(it)
                            }
                        }
                    }
                    dialogWantToSave.setPositiveButton(R.string.change_view, dialogClickListener)
                    dialogWantToSave.setNegativeButton(R.string.stay, dialogClickListener)
                    dialogWantToSave.create().show()
                } else
                    drawerLayout.closeDrawer(GravityCompat.START)
            } else
                changeIntent(it)
        }


        drawerLayout.addDrawerListener(actionBarToggle)

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

        actionBarToggle.syncState()
        drawerLayout.visibility = View.INVISIBLE

        hamburgerMenuIcon.setOnClickListener {
            drawerLayout.visibility = View.VISIBLE
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener {
            updateAndSetCurrentActivity()
            checkIfInProgrammingAndProjectActive(it)
            checkIfInProgrammingAndIfProjectIsModified(it)
            true
        }
    }
}
