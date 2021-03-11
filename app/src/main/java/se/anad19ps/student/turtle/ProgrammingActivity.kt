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

    private enum class OpenDialog{
        DIALOG_INPUT_NAME,
        DIALOG_NAME_BLANK_WARNING,
        DIALOG_NAME_EXISTS_WARNING,
        DIALOG_ASK_IF_WANT_TO_SAVE,
        DIALOG_DELETE_PROJECT,
        DIALOG_CHANGE_PARAMETER,
        NONE
    }

    companion object{
        private var openDialog = OpenDialog.NONE

        private var alertParameterPosition: Int = -1

		//Should this be hardcoded?
		// Yes, i think sååå
		private val newProjectStandardName = "New Project"

		private var blocksAreSelected = false //Marks if a click should add to deleteList
		private var selectedItemsList = ArrayList<DragDropBlock>()


        private lateinit var adapter: ProgrammingRecyclerAdapter

        private lateinit var spinnerDriveAdapter: ProgrammingSpinnerAdapter
        private lateinit var spinnerModulesAdapter: ProgrammingSpinnerAdapter
        private lateinit var spinnerCustomAdapter: ProgrammingSpinnerAdapter

        private var itemList = ArrayList<DragDropBlock>()   //List for items in RecyclerView
        private var itemIdCounter: Long =
            1  //Used to assign unique id to each dragDropBlock. 0 reserved for non added

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

        /*Start coroutine from button click. Traverse list*/
        private var job: Job? = null

        var traversingList : Boolean = false
    }

    private var inputText : String? = null
    private var inputtedTextExists : String? = null
    private var changeIntentNotNull : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programming)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        Thread(Runnable {    //Setting up spinners and buttons slowed down startup of entire activity
            setupSpinners()
            setupButtons()
        }).start()

        saveFilesManager = SaveFilesManager(this)
        customCommandManager = SaveCustomDragDropBlockManager(this)

        val intent = intent

        if (intent.hasExtra("SAVED_PROJECT_MANAGER")) {
            saveFilesManager =
                intent.getSerializableExtra("SAVED_PROJECT_MANAGER") as SaveFilesManager
            projectName = saveFilesManager.getNameOfLastOpenedProject().toString()

            val loadedProject = saveFilesManager.getProject(projectName, this)

            if (loadedProject != null) {
                itemList = loadedProject
            }

        } else {
            val lastOpenProject = saveFilesManager.getNameOfLastOpenedProject()
            if (lastOpenProject != null) {
                val loadedProject = saveFilesManager.getProject(lastOpenProject, this)

                if (loadedProject != null) {
                    itemList = loadedProject
                    projectName = lastOpenProject
                }

            } else {
                projectName = newProjectStandardName
            }

        }

        state = RunState.IDLE

        /*Setting RecyclerView layout to linear*/
        val layoutManager = LinearLayoutManager(this)
        programming_recycle_view.layoutManager = layoutManager

        /*Restore saved state if there is any. Affects runtime configuration changes*/
        if (savedInstanceState != null) {
            /*itemList =
                savedInstanceState.getParcelableArrayList<DragDropBlock>("itemList") as ArrayList<DragDropBlock>
            itemIdCounter = savedInstanceState.getLong("itemIdCounter")*/
            val savedStates =
                savedInstanceState.getParcelable<ProgrammingSavedState>("savedStateObject")

            if (savedStates != null) {
                itemList = savedStates.itemList
                itemIdCounter = savedStates.itemIdCounter
                selectedItemsList = savedStates.deleteList
                alertParameterPosition = savedStates.positionAlertDialog

                if (savedInstanceState.getString("traversingList") == "true"){
                    job = GlobalScope.launch(Dispatchers.Main) {
                        if (Utils.UtilsObject.isBluetoothConnectionThreadActive()) {
                            if(itemList.isNotEmpty()){
                                state = RunState.RUNNING
                                programming_play_or_pause_button.setImageResource(R.drawable.ic_pause)
                                traverseList()
                            }
                            else
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

                openDialog = savedInstanceState.getString("openDialog")?.let { OpenDialog.valueOf(it) }!!
                changeIntentNotNull = savedInstanceState.getBoolean("changeIntentNotNull")

                var intentToChangeTo : Intent? = null
                if(changeIntentNotNull){
                    intentToChangeTo = Intent(this, SavedProjectsActivity::class.java)
                }

                when(openDialog){
                    OpenDialog.DIALOG_INPUT_NAME -> {
                        inputText = savedInstanceState.getString("inputText")
                        displayDialogInputName(intentToChangeTo, inputText)
                    }
                    OpenDialog.DIALOG_NAME_EXISTS_WARNING -> {
                        inputtedTextExists = savedInstanceState.getString("inputtedTextExists")
                        displayDialogNameExistsWarning(inputtedTextExists!!, intentToChangeTo)
                    }
                    OpenDialog.DIALOG_NAME_BLANK_WARNING -> displayDialogNameBlankWarning(intentToChangeTo)
                    OpenDialog.DIALOG_ASK_IF_WANT_TO_SAVE -> displayDialogAskIfWantToSave(intentToChangeTo!!)
                    OpenDialog.DIALOG_DELETE_PROJECT -> displayDialogDeleteProject()
                    OpenDialog.DIALOG_CHANGE_PARAMETER -> {
                        inputText = savedInstanceState.getString("inputText")
                        changeItemParameterDialog(alertParameterPosition, inputText)
                    }
                }
            }

        }

        adapter = ProgrammingRecyclerAdapter(itemList, this)
        programming_recycle_view.adapter = adapter

        itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(programming_recycle_view)
    }

    /*Save necessary states and variables for run time configuration changes*/
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val saveStates =
            ProgrammingSavedState(itemList, selectedItemsList, itemIdCounter, alertParameterPosition)
        outState.putParcelable("savedStateObject", saveStates)
        outState.putString("traversingList", traversingList.toString())
        outState.putString("openDialog", openDialog.toString())
        outState.putBoolean("changeIntentNotNull", changeIntentNotNull)

        when(openDialog){
            OpenDialog.DIALOG_INPUT_NAME -> outState.putString("inputText", inputText)
            OpenDialog.DIALOG_NAME_EXISTS_WARNING -> outState.putString("inputtedTextExists", inputtedTextExists)
            OpenDialog.DIALOG_CHANGE_PARAMETER -> {
                outState.putString("inputText", inputText)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        programming_text_view_current_project.text = projectName
    }

    private fun setupButtons() {
        /*Play button*/
        programming_play_or_pause_button.setOnClickListener {
            if (state == RunState.IDLE) {
                job = GlobalScope.launch(Dispatchers.Main) {
                    if (Utils.UtilsObject.isBluetoothConnectionThreadActive()) {
                        if(itemList.isNotEmpty()){
                            state = RunState.RUNNING
                            programming_play_or_pause_button.setImageResource(R.drawable.ic_pause)
                            traverseList()
                        }
                        else
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
            } else if (state == RunState.PAUSE) {
                if (sem.availablePermits == 0) {
                    sem.release()   //Used to avoid idle wait in traverseList
                    traversingList = true
                }
                programming_play_or_pause_button.setImageResource(R.drawable.ic_pause)
                state = RunState.RUNNING
            } else if (state == RunState.RUNNING) {
                programming_play_or_pause_button.setImageResource(R.drawable.ic_play_arrow)
                state = RunState.PAUSE
                traversingList = false
            }
        }

        /*Reset button*/
        programming_reset_btn.setOnClickListener {  //Stop coroutine and reset list traversal
            job?.cancel()
            resetListTraverse()
        }

        /*Delete button*/
        programming_delete_btn.setOnClickListener {
            if(!blocksAreSelected) {
                if (projectName != newProjectStandardName || itemList.isNotEmpty()) {
                    displayDialogDeleteProject()
                } else {
                    Utils.UtilsObject.showUpdatedToast("Project is empty, nothing to delete", this)
                }
            }
        }

        /*Load button*/
        programming_load_button.setOnClickListener {
            val newIntent = Intent(this, SavedProjectsActivity::class.java)

            if (projectName != newProjectStandardName || itemList.isNotEmpty()) {
                displayDialogAskIfWantToSave(newIntent)
            } else {
                newIntent.putExtra("SAVED_PROJECT_MANAGER", saveFilesManager)
                startActivity(newIntent)
            }

        }

        programming_save_button.setOnClickListener {
            if (projectName != newProjectStandardName || itemList.isNotEmpty()) {
                displayDialogInputName()
            } else {
                Utils.UtilsObject.showUpdatedToast(
                    getString(R.string.save_project_when_empty),
                    this
                )
            }
        }

        programming_delete_btn_selected.setOnClickListener{
            if (selectedItemsList.isNotEmpty()) {
                val indexes = ArrayList<Int>()  //To record indexed that should be deleted

                itemList.forEachIndexed { index, dragDropBlock -> //Record indexes
                    if (selectedItemsList.contains(dragDropBlock)) {
                        indexes.add(index)
                    }
                }

                indexes.sort()  //Sort indexes by size
                indexes.reverse()   //So largest index is first. This way we don't need to change index after every removal

                for (i in 0 until indexes.size) {
                    //deleteList[itemList[indexes[i]]]?.card_drag_drop?.setCardBackgroundColor(Color.WHITE)   //Reset holders to standard color
                    itemList.removeAt(indexes[i])
                    adapter.notifyItemRemoved(indexes[i])
                }

                selectedItemsList.clear()
                blocksAreSelected = false //So clicks no longer marks for deletion

                showUnselectedButtonsHideSelectedButtons()
            }
        }

        programming_save_btn_selected.setOnClickListener{
            //CODE TO SAVE SELECTED BLOCK SET AS GROUPED BLOCKS
        }
    }

    private fun showUnselectedButtonsHideSelectedButtons(){
        programming_button_area_not_selected.visibility = View.VISIBLE
        programming_button_area_selected.visibility = View.GONE
    }

    private fun showSelectedButtonsHideUnselectedButtons(){
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

                    saveFilesManager.deleteProject(projectName, this)
                    itemList.clear()
                    projectName = newProjectStandardName
                    programming_text_view_current_project.text = projectName
                    adapter.notifyDataSetChanged()
                }
            }
        }
        dialogWantToSave.setPositiveButton(R.string.yes, dialogClickListener)
        dialogWantToSave.setNegativeButton(R.string.no, dialogClickListener)
        dialogWantToSave.setCancelable(false)
        dialogWantToSave.create().show()
    }

    // This and saveProjectAndChangeActivity() are basically the same code, could reuse code if i could make a return inside the dialog click listeners, but that seems to not be possible
    private fun displayDialogInputName(intent : Intent? = null, savedInputText : String? = null) {
        changeIntentNotNull = intent != null

        openDialog = OpenDialog.DIALOG_INPUT_NAME

        val dialogInputName = LayoutInflater.from(this).inflate(R.layout.input_text_dialog, null)
        val dialogInputNameBuilder = AlertDialog.Builder(this).setView(dialogInputName)
        dialogInputNameBuilder.setTitle(R.string.enter_project_name)
        dialogInputNameBuilder.setMessage(R.string.enter_project_name_warning)

        if(savedInputText == null){
            dialogInputName.dialogTextFieldName.setText(projectName)
        }
        else{
            dialogInputName.dialogTextFieldName.setText(savedInputText)
        }

        val inputNameDialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    Utils.UtilsObject.showUpdatedToast(getString(R.string.project_not_saved), this)
                    openDialog = OpenDialog.NONE
                    Log.e("FILE_LOG", "Cancel clicked, project not saves")
                }
                DialogInterface.BUTTON_POSITIVE -> {
                    if (dialogInputName.dialogTextFieldName.text.toString().isBlank()) {
                        displayDialogNameBlankWarning(intent)
                    } else if (saveFilesManager.saveProject(
                            dialogInputName.dialogTextFieldName.text.toString(),
                            itemList,
                            false,
                            this
                        )
                    ) {
                        Utils.UtilsObject.showUpdatedToast(getString(R.string.project_saved), this)
                        projectName = dialogInputName.dialogTextFieldName.text.toString()
                        programming_text_view_current_project.text = projectName

                        if(intent != null){
                            startActivity(intent)
                            finish()
                        }

                    } else {
                        displayDialogNameExistsWarning(dialogInputName.dialogTextFieldName.text.toString(), intent)
                    }
                }
            }
        }
        dialogInputNameBuilder.setPositiveButton(R.string.save, inputNameDialogClickListener)
        dialogInputNameBuilder.setNeutralButton(R.string.cancel, inputNameDialogClickListener)
        dialogInputNameBuilder.setCancelable(false)
        dialogInputNameBuilder.show()

        dialogInputName!!.dialogTextFieldName.doAfterTextChanged {
            inputText = dialogInputName!!.dialogTextFieldName.text.toString()
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

    private fun populateList(
        num: Int,
        type: DragDropBlock.e_type
    ): ArrayList<DragDropBlock> {
        val list = ArrayList<DragDropBlock>()

        for (i in 0 until num) {
            val drawable = when (i % 4) {
                0 -> R.drawable.ic_arrow_up
                1 -> R.drawable.ic_arrow_down
                2 -> R.drawable.ic_arrow_right
                else -> R.drawable.ic_arrow_left
            }
            val item = DragDropBlock(
                R.drawable.ic_drag_dots,
                drawable,
                "Some text",
                "Command",
                1.0,
                1.0,
                type,
                true,
                0
            )
            list.add(item)
        }
        return list
    }

    /*Creating a list containing all drive blocks for the drive spinner*/
    private fun createSpinnerDrivingBlocks(): ArrayList<DragDropBlock> {
        val list = ArrayList<DragDropBlock>()

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots, R.drawable.ic_arrow_up, "Drive forward", "2", 1.0,
                1.0, DragDropBlock.e_type.DRIVE, true, 0
            )
        )

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots, R.drawable.ic_arrow_right, "Turn right", "6", 1.0,
                1.0, DragDropBlock.e_type.DRIVE, true, 0
            )
        )

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots, R.drawable.ic_arrow_down, "Reverse", "8", 1.0,
                1.0, DragDropBlock.e_type.DRIVE, true, 0
            )
        )

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots, R.drawable.ic_arrow_left, "Turn left", "4", 1.0,
                1.0, DragDropBlock.e_type.DRIVE, true, 0
            )
        )

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots, R.drawable.ic_stop, "Stop", "5", 0.0,
                0.0, DragDropBlock.e_type.DRIVE, false, 0
            )
        )

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots, R.drawable.ic_gear, "Gear up", "u", 0.0,
                0.0, DragDropBlock.e_type.DRIVE, false, 0
            )
        )

        list.add(
            DragDropBlock(
                R.drawable.ic_drag_dots, R.drawable.ic_gear, "Gear down", "d", 0.0,
                0.0, DragDropBlock.e_type.DRIVE, false, 0
            )
        )

        list.add(
            0,   //Unused object. Shown only in title. Cannot be added to itemList
            DragDropBlock(
                R.drawable.ic_drag_dots, R.drawable.ic_drive, "Driving", "Null", 1.0,
                1.0, DragDropBlock.e_type.DRIVE, false, 0
            )
        )
        return list
    }

    /*May want to limit number of chars in each spinner item. Affects the size of the spinners*/
    private fun setupSpinners() {
        driveBlocksSpinnerList = createSpinnerDrivingBlocks()
        spinnerDriveAdapter = ProgrammingSpinnerAdapter(driveBlocksSpinnerList, this)
        programming_spinner_driving.adapter = spinnerDriveAdapter
        programming_spinner_driving.setSelection(0, false)


        modulesBlocksSpinnerList = populateList(5, DragDropBlock.e_type.MODULE)
        spinnerModulesAdapter = ProgrammingSpinnerAdapter(modulesBlocksSpinnerList, this)
        modulesBlocksSpinnerList.add(
            0, DragDropBlock(
                R.drawable.ic_drag_dots, R.drawable.ic_modules, "Modules", "Null",
                1.0, 1.0, DragDropBlock.e_type.MODULE, false, 0
            )
        )

        programming_spinner_modules.adapter = spinnerModulesAdapter
        programming_spinner_modules.setSelection(0, false)

        //Ugly way to do it, should not initialize a new SaveCustomDragDropBlockManager, rather use the one in the main thread
        customBlocksSpinnerList = SaveCustomDragDropBlockManager(this).getArrayWithCustomDragDropBlocks().clone() as ArrayList<DragDropBlock>
        spinnerCustomAdapter = ProgrammingSpinnerAdapter(customBlocksSpinnerList, this)
        spinnerCustomAdapter.setDropDownViewResource(R.layout.programming_spinner_modules_dropdown_layout)
        customBlocksSpinnerList.add(
            0, DragDropBlock(
                R.drawable.ic_drag_dots, R.drawable.ic_custom, "Custom", "Null", 1.0,
                1.0, DragDropBlock.e_type.CUSTOM, false, 0
            )
        )

        programming_spinner_custom.adapter = spinnerCustomAdapter
        programming_spinner_custom.setSelection(0, false)

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
                        itemIdCounter++    //Increment after adding id. No worries about itemIdCounter overflow.
                    itemList.add(block)
                    adapter.notifyItemInserted(itemList.size)
                    //adapter.notifyDataSetChanged()
                    programming_spinner_driving.setSelection(//Always make title block stay on top
                        0,
                        false
                    )
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
                    itemList.add(block)
                    adapter.notifyDataSetChanged()
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
                    itemList.add(block)
                    adapter.notifyDataSetChanged()
                    programming_spinner_custom.setSelection(0, false)
                }
            }
        }
    }

    /*For rearranging recyclerview*/
    private var simpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP.or(
            ItemTouchHelper.DOWN
        ), ItemTouchHelper.RIGHT
    ) {
        override fun onMove(    //Handles reordering and movement in recyclerview
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val startPosition = viewHolder.adapterPosition
            val endPosition = target.adapterPosition

            Collections.swap(itemList, startPosition, endPosition)
            recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            //No swipe
        }

        /*Disable longPress so longClicks are reserved for hold to delete function. Move by calling startDrag()*/
        override fun isLongPressDragEnabled(): Boolean {
            return false
        }
    }

    /*When touching dragDots*/
    override fun onDragDots(view: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(view) //onMove from ItemTouchHelper.simpleCallback will be accessed through startDrag
    }

    /*Used to mark item for deletion*/
    override fun onItemClick(position: Int, holder: View) {
        if (blocksAreSelected) {
            /*If item is already added to deleteList we want to deselect it*/
            if (selectedItemsList.contains(itemList[position])) {
                itemList[position].dragImage = R.drawable.ic_drag_dots
                selectedItemsList.remove(itemList[position])
                adapter.notifyItemChanged(position)
                /* holder.card_drag_drop.setCardBackgroundColor(Color.WHITE)
                 holder.card_image_drag_dots.setImageResource(R.drawable.ic_drag_dots)
                 deleteList.remove(itemList[position])*/
                if (selectedItemsList.isEmpty()) {
                    blocksAreSelected = false
                    showUnselectedButtonsHideSelectedButtons()
                }
            } else {
                itemList[position].dragImage = R.drawable.ic_baseline_check_circle_24
                selectedItemsList.add(itemList[position])
                adapter.notifyItemChanged(position)
                /*holder.card_drag_drop.setCardBackgroundColor(Color.parseColor("#AABBCC"))
                holder.card_image_drag_dots.setImageResource(R.drawable.ic_baseline_delete_24)
                deleteList[itemList[position]] = holder*/
            }
        }
    }

    override fun onParameterButtonClicked(position: Int) {
        changeItemParameterDialog(position)
    }

    /*LongClick activates selection for deletion*/
    override fun onLongClick(position: Int, view: View) {
        /*No need to activate if already activated*/
        if (!blocksAreSelected) {
            showSelectedButtonsHideUnselectedButtons()
            itemList[position].dragImage = R.drawable.ic_baseline_check_circle_24
            selectedItemsList.add(itemList[position])
            adapter.notifyItemChanged(position)
            /*view.card_drag_drop.setCardBackgroundColor(Color.parseColor("#AABBCC"))
            view.card_image_drag_dots.setImageResource(R.drawable.ic_baseline_delete_24)
            deleteList[itemList[position]] =
                view   //Map (DragDropBlock at position) with accompanying recycler item view*/
            blocksAreSelected = true
        }
    }

    /*Shows an input dialog for changing an items parameter.*/
    private fun changeItemParameterDialog(position: Int, savedInputText: String? = null) {
        alertParameterPosition = position

        openDialog = OpenDialog.DIALOG_CHANGE_PARAMETER

        val builder = AlertDialog.Builder(this).create()
        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.input_dialog_layout, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.input_dialog_text_in)

        if(savedInputText != null){
            editText.setText(savedInputText)
        }
        else{
            editText.setText(itemList[position].parameter.toString())
        }

        builder.setTitle(getString(R.string.change_parameter))
        builder.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.okay)) { dialog, which ->
            updateItemValue(position, editText.text.toString().toDouble())
            alertParameterPosition = -1
            openDialog = OpenDialog.NONE
        }
        builder.setButton(
            AlertDialog.BUTTON_NEGATIVE,
            getString(R.string.cancel)
        ) { dialog, which ->
            alertParameterPosition = -1
            openDialog = OpenDialog.NONE
        }
        builder.setView(dialogLayout)
        builder.setCancelable(false)
        builder.show()

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                /*Button is enabled if no non numeric chars in editText*/
                builder.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !s.isNullOrBlank()
                inputText = dialogLayout.input_dialog_text_in.text.toString()
            }
        })
    }

    private fun updateItemValue(position: Int, value: Double) {
        itemList[position].parameter = value
        itemList[position].displayParameter = value
        adapter.notifyDataSetChanged()
    }

    private suspend fun traverseList() {

        if (Utils.UtilsObject.isBluetoothConnectionThreadActive()) {
            val recycler = findViewById<RecyclerView>(R.id.programming_recycle_view)
            val tenthOfSecondInMS: Long = 100
            val secondInMS: Long = 1000
            traversingList = true

            itemList.forEachIndexed { index, item ->

                if(traversingList){
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
                                    //Utils.UtilsObject.showUpdatedToast("72$parameter", this.baseContext)
                                    delay(tenthOfSecondInMS) //Will finish current 'delayTimeMillis' period before pause
                                    parameter--


                                    /*Works with Locale but crashes without? Only for Andreas*/
                                    item.displayParameter =
                                        String.format(
                                            Locale.ENGLISH,
                                            "%.1f",
                                            item.displayParameter - 0.1
                                        )
                                            .toDouble()

                                    adapter.notifyDataSetChanged()
                                }
                                RunState.PAUSE -> {
                                    sem.acquire() //Button pause takes the semaphore. This coroutine will wait for it
                                }
                                else -> {
                                    state = RunState.IDLE
                                } //So we go to a known state if something would go wrong
                            }
                        }
                    } else {
                        when (state) {  //State machine
                            RunState.RUNNING -> {
                                Utils.UtilsObject.bluetoothSendString(
                                    "7${item.command}1",
                                    this.baseContext
                                )
                                //Utils.UtilsObject.showUpdatedToast("72$parameter", this.baseContext)
                                delay(tenthOfSecondInMS) //Will finish current 'delayTimeMillis' period before pause

                                adapter.notifyDataSetChanged()
                            }
                            RunState.PAUSE -> {
                                sem.acquire() //Button pause takes the semaphore. This coroutine will wait for it
                            }
                            else -> {
                                state = RunState.IDLE
                            } //So we go to a known state if something would go wrong
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
        }
        else
            Utils.UtilsObject.showUpdatedToast(getString(R.string.bluetooth_client_not_active), this)
    }

    /*Resetting list to its original state*/
    private fun resetListTraverse() {
        if (sem.availablePermits == 0) {
            sem.release()
        }
        state = RunState.IDLE
        for (i in itemList)
            i.displayParameter = i.parameter
        adapter.notifyDataSetChanged()
        programming_play_or_pause_button.setImageResource(R.drawable.ic_play_arrow)
    }

    private fun displayDialogNameBlankWarning(intent : Intent? = null){
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

    private fun displayDialogNameExistsWarning(inputNameThatExists : String, intent : Intent? = null){
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
                            itemList,
                            true,
                            this
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

                        if(intent != null){
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

    override fun onDestroy() {
        super.onDestroy()
        if(job != null){
            job!!.cancel()
        }
    }
}