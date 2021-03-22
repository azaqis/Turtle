package se.anad19ps.student.turtle

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter

class SaveCustomDragDropBlockManager(con: Context) {
    companion object {
        private const val CUSTOM_DRAG_DROP_BLOCK_SAVE_FILE = "customDragDropBlockSaveFile.txt"
        private const val EMPTY_STRING = ""
        private const val NEW_LINE_STRING = "\n"

        private var arrayWithDragDropBlocks = arrayListOf<DragDropBlock>()
        private var arrayWithDragDropBlockNames = arrayListOf<String>()
    }

    init {
        if (!File(con.filesDir, CUSTOM_DRAG_DROP_BLOCK_SAVE_FILE).isFile) {

            File(con.filesDir, CUSTOM_DRAG_DROP_BLOCK_SAVE_FILE).writeText("")

            if (File(con.filesDir, CUSTOM_DRAG_DROP_BLOCK_SAVE_FILE).isFile) {
                Log.d("CUSTOM_LOG", "customDragDropBlockSaveFile.txt was successfully created")
            } else {
                Log.e("CUSTOM_LOG", "customDragDropBlockSaveFile.txt could not be created")
            }
        } else {
            Log.d("CUSTOM_LOG", "customDragDropBlockSaveFile.txt was already created")
            loadCustomDragDropBlocks(con)
        }
    }

    fun saveDragDropBlock(dragDropBlock: DragDropBlock, allowOverwriting: Boolean, context : Context): Boolean {
        val dragAndDropBlockName = dragDropBlock.text

        Log.d("CUSTOM_LOG", "Request to save: $dragAndDropBlockName")
        //Checks if block already exists, if it exists return false if allowOverwriting is set to false
        if (dragAndDropBlockNameExist(dragAndDropBlockName) && !allowOverwriting) {
            Log.e(
                "CUSTOM_LOG",
                "Could not save: $dragAndDropBlockName, name already exists and overwriting was set to false"
            )
            return false
        }
        //If name exists and allowOverwriting is set to true, overwrite the existing block
        else if (dragAndDropBlockNameExist(dragAndDropBlockName)) {
            Log.d("CUSTOM_LOG", "Overwriting: $dragAndDropBlockName")
            val index = arrayWithDragDropBlockNames.indexOf(dragAndDropBlockName)
            arrayWithDragDropBlocks[index] = dragDropBlock
            saveToFile(context)
            return true
        }
        //Name does not exist, save as a new block
        else {
            Log.d("CUSTOM_LOG", "Saving: $dragAndDropBlockName")
            arrayWithDragDropBlockNames.add(dragAndDropBlockName)
            arrayWithDragDropBlocks.add(dragDropBlock)
            saveToFile(context)
            return true
        }
    }

    fun editDragDropBlock(oldName: String, updatedDragDropBlock: DragDropBlock, context: Context): Boolean {
        val newName = updatedDragDropBlock.text
        return if (dragAndDropBlockNameExist(newName) && oldName != newName) {
            false
        } else {
            val index = arrayWithDragDropBlockNames.indexOf(oldName)

            if (index != -1) {
                arrayWithDragDropBlockNames[index].replace(oldName, newName)
                arrayWithDragDropBlocks[index] = updatedDragDropBlock
            }

            saveToFile(context)
            true
        }
    }

    private fun saveToFile(context : Context) {
        File(context.filesDir, CUSTOM_DRAG_DROP_BLOCK_SAVE_FILE).createNewFile()
        val fwCustomDragDropBlockSaveFile =
            FileWriter(File(context.filesDir, CUSTOM_DRAG_DROP_BLOCK_SAVE_FILE), false)
        for (dragDropBlock: DragDropBlock in arrayWithDragDropBlocks) {
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.command + NEW_LINE_STRING)
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.directionImage.toString() + NEW_LINE_STRING)
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.displayParameter.toString() + NEW_LINE_STRING)
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.dragImage.toString() + NEW_LINE_STRING)
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.parameter.toString() + NEW_LINE_STRING)
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.text + NEW_LINE_STRING)
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.type.toString() + NEW_LINE_STRING)
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.parameterEnabled.toString() + NEW_LINE_STRING)
            Log.d("CUSTOM_LOG", "Saved a DragDropBlock in: $CUSTOM_DRAG_DROP_BLOCK_SAVE_FILE")
        }
        fwCustomDragDropBlockSaveFile.flush()
        fwCustomDragDropBlockSaveFile.close()
    }

    private fun dragAndDropBlockNameExist(name: String): Boolean {
        return arrayWithDragDropBlockNames.contains(name)
    }

    fun loadCustomDragDropBlocks(context : Context) {
        arrayWithDragDropBlocks.clear()
        arrayWithDragDropBlockNames.clear()

        var count = 0

        //null would be a better init, but don't know if i can change DragDropBlock to accept null?
        var commandReadFromFile = EMPTY_STRING
        var directionImageReadFromFile: Int = -1
        var displayParameterReadFromFile: Double = -1.0
        var dragImageReadFromFile: Int = -1
        var parameterReadFromFile: Double = -1.0
        var textReadFromFile = EMPTY_STRING
        var typeReadFromFile: DragDropBlock.BlockType = DragDropBlock.BlockType.CUSTOM
        var parameterEnableReadFromFile = false
        var idNumberReadFromFile: Long = 0

        File(context.filesDir, CUSTOM_DRAG_DROP_BLOCK_SAVE_FILE).useLines { lines ->
            lines.forEach {
                when (count) {
                    0 -> commandReadFromFile = it
                    1 -> directionImageReadFromFile = it.toInt()
                    2 -> displayParameterReadFromFile = it.toDouble()
                    3 -> dragImageReadFromFile = it.toInt()
                    4 -> parameterReadFromFile = it.toDouble()
                    5 -> textReadFromFile = it
                    6 -> typeReadFromFile = DragDropBlock.BlockType.valueOf(it)
                    7 -> parameterEnableReadFromFile = it.toBoolean()
                    8 -> idNumberReadFromFile = it.toLong()
                }
                if (count < 7) {
                    count++
                } else {
                    Log.d("CUSTOM_LOG", "Loaded: $textReadFromFile")
                    count = 0
                    arrayWithDragDropBlocks.add(
                        DragDropBlock(
                            dragImageReadFromFile,
                            directionImageReadFromFile,
                            textReadFromFile,
                            commandReadFromFile,
                            parameterReadFromFile,
                            displayParameterReadFromFile,
                            typeReadFromFile,
                            parameterEnableReadFromFile,
                            idNumberReadFromFile
                        )
                    )
                    arrayWithDragDropBlockNames.add(textReadFromFile)
                }
            }
        }
    }

    fun getArrayWithCustomDragDropBlocks(): ArrayList<DragDropBlock> {
        return arrayWithDragDropBlocks
    }

    fun getDragDropBlockByName(name: String): DragDropBlock? {
        return if (dragAndDropBlockNameExist(name)) {
            val index = arrayWithDragDropBlockNames.indexOf(name)
            arrayWithDragDropBlocks[index]
        } else {
            null
        }
    }

    fun deleteCustomDragDropBlock(name: String, context : Context): Boolean {
        if (dragAndDropBlockNameExist(name)) {
            val index = arrayWithDragDropBlockNames.indexOf(name)

            arrayWithDragDropBlockNames.remove(name)
            arrayWithDragDropBlocks.removeAt(index)

            saveToFile(context)
            return true
        }
        return false
    }
}