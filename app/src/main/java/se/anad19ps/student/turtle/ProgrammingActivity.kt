package se.anad19ps.student.turtle

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_programming.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.input_dialog_layout.view.*
import kotlinx.android.synthetic.main.input_text_dialog.view.*
import kotlinx.android.synthetic.main.top_bar.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import java.util.*
import kotlin.collections.ArrayList


class ProgrammingActivity : AppCompatActivity(), ProgrammingRecyclerAdapter.ItemClickListener {
    private enum class RunState {
        IDLE,
        RUNNING,
        PAUSE
    }

    private enum class OpenDialog {
        DIALOG_INPUT_NAME,
        DIALOG_NAME_BLANK_WARNING,
        DIALOG_NAME_EXISTS_WARNING,
        DIALOG_ASK_IF_WANT_TO_SAVE,
        DIALOG_DELETE_PROJECT,
        DIALOG_CHANGE_PARAMETER,
        NONE
    }

    companion object {
        var traversingList: Boolean = false

        fun isProjectModified() : Boolean{
            val savedList = saveFilesManager.loadProject(projectName)
            if(recyclerViewItemList.size != savedList.size)
                return true

            var index = 0
            for(block in recyclerViewItemList){
                if(block != savedList[index])
                    return true
                index+=1
            }
            return false
        }

        private var openDialog = OpenDialog.NONE

        private var alertParameterPosition: Int = -1

        private var blocksAreSelected = false //Marks if a click should add to deleteList
        private var selectedItemsList = ArrayList<DragDropBlock>()

        private lateinit var newProjectStandardName: String

        private lateinit var recycleViewAdapter: ProgrammingRecyclerAdapter

        private lateinit var spinnerDriveAdapter: ProgrammingSpinnerAdapter
        private lateinit var spinnerModulesAdapter: ProgrammingSpinnerAdapter
        private lateinit var spinnerCustomAdapter: ProgrammingSpinnerAdapter

        private var recyclerViewItemList = ArrayList<DragDropBlock>()

        /*Used to assign unique id to each dragDropBlock. 0 reserved for non added. Needed because
        kotlin seems to do some optimization making all items with same data get removed, edited etc
        at the same time*/
        private var itemIdCounter: Long = 1

        private var driveBlocksSpinnerList =
            mutableListOf<DragDropBlock>() //Lists for items in spinners
        private var modulesBlocksSpinnerList = mutableListOf<DragDropBlock>()
        private var customBlocksSpinnerList = mutableListOf<DragDropBlock>()

        private lateinit var itemTouchHelper: ItemTouchHelper   //For RecyclerView drag and drop. Receives events from RecyclerView

        private lateinit var state: RunState    //State for iteration through list. Needed for play, pause and stop
        private val sem = Semaphore(1)  //Can force the state machine to halt coroutine when pausing

        private lateinit var saveFilesManager: SaveFilesManager
        private lateinit var projectName: String
        private lateinit var customCommandManager: SaveCustomDragDropBlockManager

        private lateinit var recyclerSimpleCallback: ItemTouchHelper.SimpleCallback

        /*Start coroutine from button click. Traverse list*/
        private var traverseListCoroutine: Job? = null
        /*For saving information about runtime configuration*/
        private var inputText: String? = null
        private var inputtedTextExists: String? = null
        private var changeIntentNotNull: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programming)

        HamburgerMenu().setUpHamburgerMenu(this, drawer_layout_nav_view, drawer_layout, hamburger_menu_icon)

        state = RunState.IDLE

        /*De loading main thread to avoid slowed start of activity*/
        Thread(Runnable {
            setupSpinners()
            setupButtons()
            apiDependentConfigurations()
        }).start()

        setupRecyclerSimpleCallback()   //Enables RecyclerView drag and drop
        restoreRuntimeConfigurationState(savedInstanceState)

        newProjectStandardName = getString(R.string.new_project)
        saveFilesManager = SaveFilesManager(this)
        customCommandManager = SaveCustomDragDropBlockManager(this)

        checkForIntentExtra()

        if(recyclerViewItemList.count() > 0) {
            /*The way it works when loading lists is that last element has highest id number.
        * We want to start counting from there.*/
            itemIdCounter = recyclerViewItemList[recyclerViewItemList.count() - 1].idNumber + 1
        }

        /*Setting RecyclerView layout to linear*/
        val layoutManager = LinearLayoutManager(this)
        programming_recycle_view.layoutManager = layoutManager

        recycleViewAdapter = ProgrammingRecyclerAdapter(recyclerViewItemList, this)
        programming_recycle_view.adapter = recycleViewAdapter

        itemTouchHelper = ItemTouchHelper(recyclerSimpleCallback)
        itemTouchHelper.attachToRecyclerView(programming_recycle_view)


    }

    /*Save necessary states and variables for run time configuration changes*/
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val itemListClone: ArrayList<DragDropBlock> = recyclerViewItemList.clone() as ArrayList<DragDropBlock>
        val selectedItemsListClone: ArrayList<DragDropBlock> =
            selectedItemsList.clone() as ArrayList<DragDropBlock>

        val saveStates =
            ProgrammingSavedState(
                itemListClone,
                selectedItemsListClone,
                itemIdCounter,
                alertParameterPosition
            )
        outState.putParcelable("savedStateObject", saveStates)
        outState.putString("traversingList", traversingList.toString())
        outState.putString("openDialog", openDialog.toString())
        outState.putBoolean("changeIntentNotNull", changeIntentNotNull)

        when (openDialog) {
            OpenDialog.DIALOG_INPUT_NAME -> outState.putString("inputText", inputText)
            OpenDialog.DIALOG_NAME_EXISTS_WARNING -> outState.putString(
                "inputtedTextExists",
                inputtedTextExists
            )
            OpenDialog.DIALOG_CHANGE_PARAMETER -> {
                outState.putString("inputText", inputText)
            }
            else -> {
                //No action
            }
        }
    }

    override fun onStart() {
        super.onStart()
        programming_text_view_current_project.text = projectName
    }

    override fun onDestroy() {
        super.onDestroy()
        if (traverseListCoroutine != null) {    //Cancel
            traverseListCoroutine!!.cancel()
        }
    }

    private fun apiDependentConfigurations() {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            programming_delete_btn.elevation = 10f
            programming_reset_btn.elevation = 10f
            programming_play_or_pause_button.elevation = 10f
            programming_load_button.elevation = 10f
            programming_save_button.elevation = 10f
            programming_delete_btn_selected.elevation = 10f
        }
    }

    private fun setupButtons() {
        /*Play button*/
        programming_play_or_pause_button.setOnClickListener {
            when (state) {
                RunState.IDLE -> {  //When play button is first pressed
                    traverseListCoroutine = GlobalScope.launch(Dispatchers.Main) {
                        if (Utils.UtilsObject.isBluetoothConnectionThreadActive()) {
                            if (recyclerViewItemList.isNotEmpty()) {
                                state = RunState.RUNNING
                                programming_play_or_pause_button.setImageResource(R.drawable.ic_pause)
                                traverseList()
                            } else
                                Utils.UtilsObject.showUpdatedToast(
                                    getString(R.string.project_is_empty),
                                    baseContext
                                )
                        } else {
                            Utils.UtilsObject.showUpdatedToast(
                                getString(R.string.not_connected_to_bt_device_warning),
                                baseContext
                            )
                        }
                    }
                }
                RunState.PAUSE -> {
                    if (sem.availablePermits == 0) {
                        sem.release()   //Used to avoid idle wait in traverseList
                        traversingList = true
                    }
                    programming_play_or_pause_button.setImageResource(R.drawable.ic_pause)
                    state = RunState.RUNNING
                }
                RunState.RUNNING -> {
                    programming_play_or_pause_button.setImageResource(R.drawable.ic_play_arrow)
                    state = RunState.PAUSE
                    traversingList = false
                }
            }
        }

        /*Reset button*/
        programming_reset_btn.setOnClickListener {
            traverseListCoroutine?.cancel()
            resetListTraverse()
        }

        /*Delete button*/
        programming_delete_btn.setOnClickListener {
            if (!blocksAreSelected) {
                if (projectName != newProjectStandardName || recyclerViewItemList.isNotEmpty()) {
                    displayDialogDeleteProject()
                } else {
                    Utils.UtilsObject.showUpdatedToast(
                        getString(R.string.empty_project_delete_button_toast),
                        this
                    )
                }
            }
        }

        /*Load button*/
        programming_load_button.setOnClickListener {
            val newIntent = Intent(this, SavedProjectsActivity::class.java)

            if (projectName != newProjectStandardName || recyclerViewItemList.isNotEmpty()) {
                displayDialogAskIfWantToSave(newIntent)
            } else {
                startActivity(newIntent)
                finish()
            }

        }

        programming_save_button.setOnClickListener {
            if (projectName != newProjectStandardName || recyclerViewItemList.isNotEmpty()) {
                displayDialogInputName()
            } else {
                Utils.UtilsObject.showUpdatedToast(
                    getString(R.string.save_project_when_empty),
                    this
                )
            }
        }

        programming_delete_btn_selected.setOnClickListener {
            if (selectedItemsList.isNotEmpty()) {
                val indexes = ArrayList<Int>()  //To record indexed that should be deleted

                recyclerViewItemList.forEachIndexed { index, dragDropBlock -> //Record indexes
                    if (selectedItemsList.contains(dragDropBlock)) {
                        indexes.add(index)
                    }
                }

                indexes.sort()  //Sort indexes by size
                indexes.reverse()   //So largest index is first. This way we don't need to change index after every removal

                for (i in 0 until indexes.size) {
                    //deleteList[itemList[indexes[i]]]?.card_drag_drop?.setCardBackgroundColor(Color.WHITE)   //Reset holders to standard color
                    recyclerViewItemList.removeAt(indexes[i])
                    recycleViewAdapter.notifyDataSetChanged()
                    //adapter.notifyItemRemoved(indexes[i])
                }

                selectedItemsList.clear()
                blocksAreSelected = false //So clicks no longer marks for deletion

                showUnselectedButtonsHideSelectedButtons()
            }
        }
    }

    /*Get information (if any) that may have been passed to this activity from another*/
    private fun checkForIntentExtra() {
        val intent = intent
        if (intent.hasExtra("PROJECT_NAME")) {
            projectName =
                intent.getSerializableExtra("PROJECT_NAME").toString()

            val loadedProject = saveFilesManager.getProject(projectName)

            if (loadedProject != null) {
                recyclerViewItemList = loadedProject
            }

        } else {
            val lastOpenProject = saveFilesManager.getNameOfLastOpenedProject()
            if (lastOpenProject != null) {
                val loadedProject = saveFilesManager.getProject(lastOpenProject)

                if (loadedProject != null) {
                    recyclerViewItemList = loadedProject
                    projectName = lastOpenProject
                }

            } else {
                projectName = newProjectStandardName
            }

        }
    }

    /*Restore saved state if there is any. Affects runtime configuration changes*/
    private fun restoreRuntimeConfigurationState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val savedStates =
                savedInstanceState.getParcelable<ProgrammingSavedState>("savedStateObject")

            if (savedStates != null) {
                recyclerViewItemList = savedStates.itemList
                itemIdCounter = savedStates.itemIdCounter
                selectedItemsList = savedStates.selectedList
                alertParameterPosition = savedStates.positionAlertDialog

                if (savedInstanceState.getString("traversingList") == "true") {
                    traverseListCoroutine = GlobalScope.launch(Dispatchers.Main) {
                        if (Utils.UtilsObject.isBluetoothConnectionThreadActive()) {
                            if (recyclerViewItemList.isNotEmpty()) {
                                state = RunState.RUNNING
                                programming_play_or_pause_button.setImageResource(R.drawable.ic_pause)
                                traverseList()
                            } else
                                Utils.UtilsObject.showUpdatedToast(
                                    getString(R.string.project_is_empty),
                                    baseContext
                                )
                        } else {
                            Utils.UtilsObject.showUpdatedToast(
                                getString(R.string.not_connected_to_bt_device_warning),
                                baseContext
                            )
                        }
                    }
                }

                openDialog =
                    savedInstanceState.getString("openDialog")?.let { OpenDialog.valueOf(it) }!!
                changeIntentNotNull = savedInstanceState.getBoolean("changeIntentNotNull")

                var intentToChangeTo: Intent? = null
                if (changeIntentNotNull) {
                    intentToChangeTo = Intent(this, SavedProjectsActivity::class.java)
                }

                when (openDialog) {
                    OpenDialog.DIALOG_INPUT_NAME -> {
                        inputText = savedInstanceState.getString("inputText")
                        displayDialogInputName(intentToChangeTo, inputText)
                    }
                    OpenDialog.DIALOG_NAME_EXISTS_WARNING -> {
                        inputtedTextExists = savedInstanceState.getString("inputtedTextExists")
                        displayDialogNameExistsWarning(inputtedTextExists!!, intentToChangeTo)
                    }
                    OpenDialog.DIALOG_NAME_BLANK_WARNING -> displayDialogNameBlankWarning(
                        intentToChangeTo
                    )
                    OpenDialog.DIALOG_ASK_IF_WANT_TO_SAVE -> displayDialogAskIfWantToSave(
                        intentToChangeTo!!
                    )
                    OpenDialog.DIALOG_DELETE_PROJECT -> displayDialogDeleteProject()
                    OpenDialog.DIALOG_CHANGE_PARAMETER -> {
                        inputText = savedInstanceState.getString("inputText")
                        changeItemParameterDialog(alertParameterPosition, inputText)
                    }
                    else -> {
                        //No action
                    }
                }
            }
        }
    }

    /*For rearranging items in RecyclerView*/
    private fun setupRecyclerSimpleCallback() {
        recyclerSimpleCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP.or(
                ItemTouchHelper.DOWN
            ), 0
        ) {
            override fun onMove(    //Handles reordering and movement in recyclerview
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val startPosition = viewHolder.adapterPosition
                val endPosition = target.adapterPosition

                Collections.swap(recyclerViewItemList, startPosition, endPosition)
                recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //No swipe
            }

            /*Disable longPress so longClicks are reserved for hold to select function. Move by calling startDrag()*/
            override fun isLongPressDragEnabled(): Boolean {
                return false
            }
        }
    }

    private fun setupSpinners() {
        driveBlocksSpinnerList = createSpinnerDrivingBlocks()
        spinnerDriveAdapter = ProgrammingSpinnerAdapter(driveBlocksSpinnerList, this)
        programming_spinner_driving.adapter = spinnerDriveAdapter
        programming_spinner_driving.setSelection(0, false)

        modulesBlocksSpinnerList = createSpinnerModuleBlocks()
        spinnerModulesAdapter = ProgrammingSpinnerAdapter(modulesBlocksSpinnerList, this)
        programming_spinner_modules.adapter = spinnerModulesAdapter
        programming_spinner_modules.setSelection(0, false)

        customBlocksSpinnerList =
            SaveCustomDragDropBlockManager(this).getArrayWithCustomDragDropBlocks()
                .clone() as ArrayList<DragDropBlock>
        spinnerCustomAdapter = ProgrammingSpinnerAdapter(customBlocksSpinnerList, this)
        customBlocksSpinnerList.add(    //This is the title block. Always on top. Cannot be added to list.
            0, DragDropBlock(
                R.drawable.ic_drag_dots, R.drawable.ic_custom, "Custom", "Null", 1.0,
                1.0, DragDropBlock.e_type.CUSTOM, false, 0
            )
        )
        programming_spinner_custom.adapter = spinnerCustomAdapter
        programming_spinner_custom.setSelection(0, false)

        /*So we can scroll to the added item*/
        val recycler = findViewById<RecyclerView>(R.id.programming_recycle_view)

        /*When an item is selected. Three similar setups for onItemSelectedListener*/
        programming_spinner_driving.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) { //We shouldn't add the title block
                    val block = (parent?.getItemAtPosition(position) as DragDropBlock).copy()
                    block.idNumber =
                        itemIdCounter++    //Increment after adding id
                    recyclerViewItemList.add(block)
                    recycleViewAdapter.notifyItemInserted(recycleViewAdapter.itemCount)
                    recycler.scrollToPosition(recycleViewAdapter.itemCount - 1)
                    programming_spinner_driving.setSelection(
                        0,
                        false
                    )//Make title block stay on top
                }
            }
        }

        programming_spinner_modules.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    val block = (parent?.getItemAtPosition(position) as DragDropBlock).copy()
                    block.idNumber =
                        itemIdCounter++
                    recyclerViewItemList.add(block)
                    recycleViewAdapter.notifyItemInserted(recycleViewAdapter.itemCount)
                    recycler.scrollToPosition(recycleViewAdapter.itemCount - 1)
                    programming_spinner_modules.setSelection(0, false)
                }
            }
        }

        programming_spinner_custom.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    val block = (parent?.getItemAtPosition(position) as DragDropBlock).copy()
                    block.idNumber =
                        itemIdCounter++
                    recyclerViewItemList.add(block)
                    recycleViewAdapter.notifyItemInserted(recycleViewAdapter.itemCount)
                    recycler.scrollToPosition(recycleViewAdapter.itemCount - 1)
                    programming_spinner_custom.setSelection(0, false)
                }
            }
        }
    }

    private fun createSpinnerModuleBlocks(): ArrayList<DragDropBlock> {
        val list = ArrayList<DragDropBlock>()
        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_baseline_highlight_24,
                getString(R.string.module_spinner_led_on),
                "Not implemented",
                1.0,
                1.0,
                DragDropBlock.e_type.MODULE,
                false,
                0
            )
        )

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_baseline_highlight_24,
                getString(R.string.module_spinner_led_off),
                "Not implemented",
                1.0,
                1.0,
                DragDropBlock.e_type.MODULE,
                false,
                0
            )
        )

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_baseline_surround_sound_24,
                getString(R.string.module_spinner_buzzer),
                "Not implemented",
                1.0,
                1.0,
                DragDropBlock.e_type.MODULE,
                true,
                0
            )
        )

        list.add(
            0,   //Unused object. Shown only in title. Cannot be added to itemList
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_modules,
                getString(R.string.module_spinner_title),
                "Null",
                1.0,
                1.0,
                DragDropBlock.e_type.MODULE,
                false,
                0
            )
        )

        return list
    }

    private fun createSpinnerDrivingBlocks(): ArrayList<DragDropBlock> {
        val list = ArrayList<DragDropBlock>()

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_arrow_up,
                getString(R.string.drive_spinner_forward),
                "2",
                1.0,
                1.0,
                DragDropBlock.e_type.DRIVE,
                true,
                0
            )
        )

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_arrow_right,
                getString(R.string.drive_spinner_turn_right),
                "6",
                1.0,
                1.0,
                DragDropBlock.e_type.DRIVE,
                true,
                0
            )
        )

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_arrow_down,
                getString(R.string.drive_spinner_reverse),
                "8",
                1.0,
                1.0,
                DragDropBlock.e_type.DRIVE,
                true,
                0
            )
        )

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_arrow_left,
                getString(R.string.drive_spinner_turn_left),
                "4",
                1.0,
                1.0,
                DragDropBlock.e_type.DRIVE,
                true,
                0
            )
        )

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_stop,
                getString(R.string.drive_spinner_stop),
                "5",
                1.0,
                0.0,
                DragDropBlock.e_type.DRIVE,
                true,
                0
            )
        )

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_gear,
                getString(R.string.drive_spinner_gear_up),
                "u",
                0.0,
                0.0,
                DragDropBlock.e_type.DRIVE,
                false,
                0
            )
        )

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_gear,
                getString(R.string.drive_spinner_gear_down),
                "d",
                0.0,
                0.0,
                DragDropBlock.e_type.DRIVE,
                false,
                0
            )
        )

        list.add(
            0,   //Unused object. Shown only in title. Cannot be added to itemList
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_drive,
                getString(R.string.drive_spinner_title),
                "Null",
                1.0,
                1.0,
                DragDropBlock.e_type.DRIVE,
                false,
                0
            )
        )
        return list
    }

    private fun showUnselectedButtonsHideSelectedButtons() {
        programming_button_area_not_selected.visibility = View.VISIBLE
        programming_button_area_selected.visibility = View.GONE
    }

    private fun showSelectedButtonsHideUnselectedButtons() {
        programming_button_area_selected.visibility = View.VISIBLE
        programming_button_area_not_selected.visibility = View.GONE
    }

    private fun displayDialogDeleteProject() {
        openDialog = OpenDialog.DIALOG_DELETE_PROJECT

        val dialogWantToSave = android.app.AlertDialog.Builder(this)
        dialogWantToSave.setTitle(R.string.do_you_want_to_delete_this_project)
        dialogWantToSave.setMessage(R.string.all_progress_for_this_project_will_be_lost)
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEGATIVE -> {
                    Utils.UtilsObject.showUpdatedToast(
                        getString(R.string.project_not_deleted),
                        this
                    )
                    Log.e("FILE_LOG", "No clicked, Project not deleted")
                }
                DialogInterface.BUTTON_POSITIVE -> {
                    Utils.UtilsObject.showUpdatedToast(getString(R.string.project_deleted), this)
                    Log.e("FILE_LOG", "Yes clicked, project deleted")

                    saveFilesManager.deleteProject(projectName)
                    recyclerViewItemList.clear()
                    projectName = newProjectStandardName
                    programming_text_view_current_project.text = projectName
                    recycleViewAdapter.notifyDataSetChanged()
                }
            }
        }
        dialogWantToSave.setPositiveButton(R.string.yes, dialogClickListener)
        dialogWantToSave.setNegativeButton(R.string.no, dialogClickListener)
        dialogWantToSave.setCancelable(false)
        dialogWantToSave.create().show()
    }

    private fun displayDialogInputName(intent: Intent? = null, savedInputText: String? = null) {
        changeIntentNotNull = intent != null

        openDialog = OpenDialog.DIALOG_INPUT_NAME

        val dialogInputName = LayoutInflater.from(this).inflate(R.layout.input_text_dialog, null)
        val dialogInputNameBuilder = AlertDialog.Builder(this).setView(dialogInputName)
        dialogInputNameBuilder.setTitle(R.string.enter_project_name)
        dialogInputNameBuilder.setMessage(R.string.enter_project_name_warning)

        if (savedInputText == null) {
            dialogInputName.input_text_dialog_layout_dialog_text_field_name.setText(projectName)
        } else {
            dialogInputName.input_text_dialog_layout_dialog_text_field_name.setText(savedInputText)
        }

        val inputNameDialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    Utils.UtilsObject.showUpdatedToast(getString(R.string.project_not_saved), this)
                    openDialog = OpenDialog.NONE
                    Log.e("FILE_LOG", "Cancel clicked, project not saves")
                }
                DialogInterface.BUTTON_POSITIVE -> {
                    if (dialogInputName.input_text_dialog_layout_dialog_text_field_name.text.toString().isBlank()) {
                        displayDialogNameBlankWarning(intent)
                    } else if (saveFilesManager.saveProject(
                            dialogInputName.input_text_dialog_layout_dialog_text_field_name.text.toString(),
                            recyclerViewItemList,
                            false
                        )
                    ) {
                        Utils.UtilsObject.showUpdatedToast(getString(R.string.project_saved), this)
                        projectName = dialogInputName.input_text_dialog_layout_dialog_text_field_name.text.toString()
                        programming_text_view_current_project.text = projectName

                        if (intent != null) {
                            startActivity(intent)
                            finish()
                        }

                    } else {
                        displayDialogNameExistsWarning(
                            dialogInputName.input_text_dialog_layout_dialog_text_field_name.text.toString(),
                            intent
                        )
                    }
                }
            }
        }
        dialogInputNameBuilder.setPositiveButton(R.string.save, inputNameDialogClickListener)
        dialogInputNameBuilder.setNeutralButton(R.string.cancel, inputNameDialogClickListener)
        dialogInputNameBuilder.setCancelable(false)
        dialogInputNameBuilder.show()

        dialogInputName!!.input_text_dialog_layout_dialog_text_field_name.doAfterTextChanged {
            inputText = dialogInputName.input_text_dialog_layout_dialog_text_field_name.text.toString()
        }
    }

    //Asking user if it wants to save project, and then changes activity.
    private fun displayDialogAskIfWantToSave(intent: Intent) {
        changeIntentNotNull = true

        openDialog = OpenDialog.DIALOG_ASK_IF_WANT_TO_SAVE

        val dialogWantToSave = android.app.AlertDialog.Builder(this)
        dialogWantToSave.setTitle(R.string.save_before_opening_new_project)
        dialogWantToSave.setMessage(R.string.dont_save_progress_loss_warning)
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEGATIVE -> {
                    Utils.UtilsObject.showUpdatedToast(getString(R.string.project_not_saved), this)
                    startActivity(intent)
                    finish()
                }
                DialogInterface.BUTTON_POSITIVE -> {
                    displayDialogInputName(intent)
                }
            }
        }
        dialogWantToSave.setPositiveButton(R.string.yes, dialogClickListener)
        dialogWantToSave.setNegativeButton(R.string.no, dialogClickListener)
        dialogWantToSave.setCancelable(false)
        dialogWantToSave.create().show()
    }

    /*Clicking an item in the RecyclerView*/
    override fun onItemClick(position: Int) {
        /*Used for selecting items*/
        if (blocksAreSelected) {
            /*If item is already added to deleteList we want to deselect it*/
            if (selectedItemsList.contains(recyclerViewItemList[position])) {
                recyclerViewItemList[position].dragImage = R.drawable.ic_drag_dots
                selectedItemsList.remove(recyclerViewItemList[position])
                recycleViewAdapter.notifyDataSetChanged()
                if (selectedItemsList.isEmpty()) {
                    blocksAreSelected = false
                    showUnselectedButtonsHideSelectedButtons()
                }
            } else {
                recyclerViewItemList[position].dragImage = R.drawable.ic_baseline_check_circle_24
                selectedItemsList.add(recyclerViewItemList[position])
                recycleViewAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onParameterButtonClicked(position: Int) {
        changeItemParameterDialog(position)
    }

    /*Used for activating selection by clicking items*/
    override fun onLongClick(position: Int) {
        if (!blocksAreSelected) {
            showSelectedButtonsHideUnselectedButtons()
            recyclerViewItemList[position].dragImage = R.drawable.ic_baseline_check_circle_24
            selectedItemsList.add(recyclerViewItemList[position])
            recycleViewAdapter.notifyDataSetChanged()
            blocksAreSelected = true
        }
    }

    override fun onDragDots(view: RecyclerView.ViewHolder) {
        /*onMove from ItemTouchHelper.simpleCallback will be accessed through startDrag*/
        itemTouchHelper.startDrag(view)
    }

    /*Shows an input dialog for changing an items parameter.*/
    private fun changeItemParameterDialog(position: Int, savedInputText: String? = null) {
        alertParameterPosition = position

        openDialog = OpenDialog.DIALOG_CHANGE_PARAMETER

        val builder = AlertDialog.Builder(this).create()
        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.input_dialog_layout, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.input_dialog_layout_text_in)

        if (savedInputText != null) {
            editText.setText(savedInputText)
        } else {
            editText.setText(recyclerViewItemList[position].parameter.toString())
        }

        builder.setTitle(getString(R.string.change_parameter))
        builder.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.okay)) { _, _ ->
            updateItemValue(position, editText.text.toString().toDouble())
            alertParameterPosition = -1
            openDialog = OpenDialog.NONE
        }
        builder.setButton(
            AlertDialog.BUTTON_NEGATIVE,
            getString(R.string.cancel)
        ) { _, _ ->
            alertParameterPosition = -1
            openDialog = OpenDialog.NONE
        }
        builder.setView(dialogLayout)
        builder.setCancelable(false)
        builder.show()

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //Unused
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Unused
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                /*Button is enabled if no non numeric chars in editText*/
                builder.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !s.isNullOrBlank()
                inputText = dialogLayout.input_dialog_layout_text_in.text.toString()
            }
        })
    }

    private fun updateItemValue(position: Int, value: Double) {
        recyclerViewItemList[position].parameter = value
        recyclerViewItemList[position].displayParameter = value
        recycleViewAdapter.notifyDataSetChanged()
    }

    private suspend fun traverseList() {

        if (Utils.UtilsObject.isBluetoothConnectionThreadActive()) {
            val recycler = findViewById<RecyclerView>(R.id.programming_recycle_view)
            val tenthOfSecondInMS: Long = 100
            val secondInMS: Long = 1000
            traversingList = true

            recyclerViewItemList.forEachIndexed { index, item ->

                if (traversingList) {
                    recycler.scrollToPosition(index) //Scrolls list so that current item is on screen

                    var parameter: Int = item.displayParameter.toInt()

                    if (item.parameterEnabled) {
                        while (item.displayParameter > 0) {
                            when (state) {  //State machine
                                RunState.RUNNING -> {
                                    Utils.UtilsObject.bluetoothSendString(
                                        "7${item.command}$parameter",
                                        this.baseContext
                                    )
                                    delay(tenthOfSecondInMS) //Will finish current 'delayTimeMillis' period before pause
                                    parameter--


                                    item.displayParameter =
                                        String.format(
                                            Locale.ENGLISH,
                                            "%.1f",
                                            item.displayParameter - 0.1
                                        )
                                            .toDouble()

                                    recycleViewAdapter.notifyDataSetChanged()
                                }
                                RunState.PAUSE -> {
                                    sem.acquire() //Pause button takes the semaphore. This coroutine will wait for it
                                }
                                else -> {
                                    state = RunState.IDLE
                                }
                            }
                        }
                    } else {
                        when (state) {  //State machine
                            RunState.RUNNING -> {
                                Utils.UtilsObject.bluetoothSendString(
                                    "7${item.command}1",
                                    this.baseContext
                                )
                                delay(tenthOfSecondInMS) //Will finish current 'delayTimeMillis' period before pause

                                recycleViewAdapter.notifyDataSetChanged()
                            }
                            RunState.PAUSE -> {
                                sem.acquire() //Pause button takes the semaphore. This coroutine will wait for it
                            }
                            else -> {
                                state = RunState.IDLE
                            } //Go to a known state if something would go wrong
                        }
                    }
                }
            }

            //Stop the robot
            Utils.UtilsObject.bluetoothSendString("5", this.baseContext)
            Utils.UtilsObject.showUpdatedToast(
                getString(R.string.project_has_run_through_completely),
                this
            )
            delay(secondInMS * 3)

            resetListTraverse()
            traversingList = false
        } else
            Utils.UtilsObject.showUpdatedToast(
                getString(R.string.bluetooth_client_not_active),
                this
            )
    }

    /*Resetting list to its original state*/
    private fun resetListTraverse() {
        if (sem.availablePermits == 0) {
            sem.release()
        }
        state = RunState.IDLE
        for (i in recyclerViewItemList)
            i.displayParameter = i.parameter
        recycleViewAdapter.notifyDataSetChanged()
        programming_play_or_pause_button.setImageResource(R.drawable.ic_play_arrow)
    }

    private fun displayDialogNameBlankWarning(intent: Intent? = null) {
        changeIntentNotNull = intent != null

        openDialog = OpenDialog.DIALOG_NAME_BLANK_WARNING

        val dialogNameBlankWarning = android.app.AlertDialog.Builder(this)
        dialogNameBlankWarning.setTitle(R.string.name_can_not_be_blank)
        dialogNameBlankWarning.setMessage(R.string.name_can_not_be_blank_warning)
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    displayDialogInputName(intent)
                }
            }
        }
        dialogNameBlankWarning.setNeutralButton(R.string.okay, dialogClickListener)
        dialogNameBlankWarning.setCancelable(false)
        dialogNameBlankWarning.create().show()
    }

    private fun displayDialogNameExistsWarning(
        inputNameThatExists: String,
        intent: Intent? = null
    ) {
        changeIntentNotNull = intent != null

        openDialog = OpenDialog.DIALOG_NAME_EXISTS_WARNING

        inputtedTextExists = inputNameThatExists

        val dialogNameExistsWarning = android.app.AlertDialog.Builder(this)
        dialogNameExistsWarning.setTitle(R.string.project_name_already_exists)
        dialogNameExistsWarning.setMessage(R.string.do_you_want_to_override_the_existing_save_file)

        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEGATIVE -> {
                    displayDialogInputName(intent)
                }
                DialogInterface.BUTTON_POSITIVE -> {
                    if (saveFilesManager.saveProject(
                            inputNameThatExists,
                            recyclerViewItemList,
                            true
                        )
                    ) {
                        Utils.UtilsObject.showUpdatedToast(
                            getString(R.string.overwriting) + ": $projectName",
                            this
                        )
                        Log.e("FILE_LOG", "Overwriting: $projectName")
                        projectName =
                            inputNameThatExists
                        programming_text_view_current_project.text = projectName

                        if (intent != null) {
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
        dialogNameExistsWarning.setPositiveButton(R.string.yes, dialogClickListener)
        dialogNameExistsWarning.setNegativeButton(R.string.no, dialogClickListener)
        dialogNameExistsWarning.setCancelable(false)
        dialogNameExistsWarning.create().show()
    }
}