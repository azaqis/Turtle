package se.anad19ps.student.turtle

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter

class SaveCustomDragDropBlockManager(con: Context) {
    companion object {
        private const val customDragDropBlockSaveFile = "customDragDropBlockSaveFile.txt"
        private lateinit var context: Context
        private var arrayWithDragDropBlocks = arrayListOf<DragDropBlock>()
        private var arrayWithDragDropBlockNames = arrayListOf<String>()
    }

    init {
        context = con

        if (!File(context.filesDir, customDragDropBlockSaveFile).isFile) {

            File(context.filesDir, customDragDropBlockSaveFile).writeText("")

            if (File(context.filesDir, customDragDropBlockSaveFile).isFile) {
                Log.d("CUSTOM_LOG", "customDragDropBlockSaveFile.txt was successfully created")
            } else {
                Log.e("CUSTOM_LOG", "customDragDropBlockSaveFile.txt could not be created")
            }
        } else {
            Log.d("CUSTOM_LOG", "customDragDropBlockSaveFile.txt was already created")
            loadCustomDragDropBlocks()
        }
    }

    fun saveDragDropBlock(dragDropBlock: DragDropBlock, allowOverwriting: Boolean): Boolean {
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
            saveToFile()
            return true
        }
        //Name does not exist, save as a new block
        else {
            Log.d("CUSTOM_LOG", "Saving: $dragAndDropBlockName")
            arrayWithDragDropBlockNames.add(dragAndDropBlockName)
            arrayWithDragDropBlocks.add(dragDropBlock)
            saveToFile()
            return true
        }
    }

    fun editDragDropBlock(oldName: String, updatedDragDropBlock: DragDropBlock): Boolean {
        val newName = updatedDragDropBlock.text
        return if (dragAndDropBlockNameExist(newName) && oldName != newName) {
            false
        } else {
            val index = arrayWithDragDropBlockNames.indexOf(oldName)

            if (index != -1) {
                arrayWithDragDropBlockNames[index] = updatedDragDropBlock.text
                arrayWithDragDropBlocks[index] = updatedDragDropBlock
            }

            saveToFile()
            true
        }
    }

    private fun saveToFile() {
        File(context.filesDir, customDragDropBlockSaveFile).createNewFile()
        val fwCustomDragDropBlockSaveFile =
            FileWriter(File(context.filesDir, customDragDropBlockSaveFile), false)
        for (dragDropBlock: DragDropBlock in arrayWithDragDropBlocks) {
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.command + "\n")
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.directionImage.toString() + "\n")
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.displayParameter.toString() + "\n")
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.dragImage.toString() + "\n")
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.parameter.toString() + "\n")
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.text + "\n")
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.type.toString() + "\n")
            fwCustomDragDropBlockSaveFile.write(dragDropBlock.parameterEnabled.toString() + "\n")
            Log.d("CUSTOM_LOG", "Saved a DragDropBlock in: $customDragDropBlockSaveFile")
        }
        fwCustomDragDropBlockSaveFile.flush()
        fwCustomDragDropBlockSaveFile.close()
    }

    private fun dragAndDropBlockNameExist(name: String): Boolean {
        return arrayWithDragDropBlockNames.contains(name)
    }

    fun loadCustomDragDropBlocks() {
        arrayWithDragDropBlocks.clear()

        var count = 0

        //null would be a better init, but don't know if i can change DragDropBlock to accept null?
        var commandReadFromFile = ""
        var directionImageReadFromFile: Int = -1
        var displayParameterReadFromFile: Double = -1.0
        var dragImageReadFromFile: Int = -1
        var parameterReadFromFile: Double = -1.0
        var textReadFromFile = ""
        var typeReadFromFile: DragDropBlock.e_type = DragDropBlock.e_type.CUSTOM
        var parameterEnableReadFromFile = false
        var idNumberReadFromFile: Long = 0

        File(context.filesDir, customDragDropBlockSaveFile).useLines { lines ->
            lines.forEach {
                when (count) {
                    0 -> commandReadFromFile = it
                    1 -> directionImageReadFromFile = it.toInt()
                    2 -> displayParameterReadFromFile = it.toDouble()
                    3 -> dragImageReadFromFile = it.toInt()
                    4 -> parameterReadFromFile = it.toDouble()
                    5 -> textReadFromFile = it
                    6 -> typeReadFromFile = DragDropBlock.e_type.valueOf(it)
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

    fun deleteCustomDragDropBlock(name: String): Boolean {
        if (dragAndDropBlockNameExist(name)) {
            val index = arrayWithDragDropBlockNames.indexOf(name)

            arrayWithDragDropBlockNames.remove(name)
            arrayWithDragDropBlocks.removeAt(index)

            saveToFile()
            return true
        }
        return false
    }
}