package se.anad19ps.student.turtle

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter

class SaveCustomDragDropBlockManager(con: Context) {
    companion object {
        private val customDragDropBlockSaveFile = "customDragDropBlockSaveFile.txt"
        private lateinit var context: Context
        private var arrayWithDragDropBlockNames = arrayListOf<String>()
        private var arrayWithDragDropBlockDescription = arrayListOf<String>()
        private var arrayWithDragDropBlockImage = arrayListOf<Int>()
    }

    init {
        context = con

        if(!File(context.filesDir, customDragDropBlockSaveFile).isFile){

            File(context.filesDir, customDragDropBlockSaveFile).writeText("")

            if(File(context.filesDir, customDragDropBlockSaveFile).isFile){
                Log.e("CUSTOM_LOG", "customDragDropBlockSaveFile.txt was successfully created")
            }
            else{
                Log.e("CUSTOM_LOG", "customDragDropBlockSaveFile.txt could not be created")
            }
        }
        else{
            Log.e("CUSTOM_LOG", "customDragDropBlockSaveFile.txt was already created")
            loadNamesOfCustomDragDropBlocks()
        }
    }

    fun saveDragDropBlock(dragAndDropBlockName: String, dragDropBlock: DragDropBlock) {
        addNewName(dragAndDropBlockName)

        File(context.filesDir, "$dragAndDropBlockName.txt").createNewFile()
        val fwProjectSaveFile =
            FileWriter(File(context.filesDir, "$dragAndDropBlockName.txt"), true)

        fwProjectSaveFile.write(dragDropBlock.command + "\n")
        fwProjectSaveFile.write(dragDropBlock.directionImage.toString() + "\n")
        fwProjectSaveFile.write(dragDropBlock.displayParameter.toString() + "\n")
        fwProjectSaveFile.write(dragDropBlock.dragImage.toString() + "\n")
        fwProjectSaveFile.write(dragDropBlock.parameter.toString() + "\n")
        fwProjectSaveFile.write(dragDropBlock.text + "\n")
        fwProjectSaveFile.write(dragDropBlock.type.toString() + "\n")
        Log.e("CUSTOM_LOG", "Saved a DragDropBlock with name: $dragDropBlock")

        fwProjectSaveFile.flush()
        fwProjectSaveFile.close()
    }

    fun addNewName(name: String): Boolean {
        if (!dragAndDropBlockNameExist(name)) {
            arrayWithDragDropBlockNames.add(name)
            updateFileWithDragDropBlockNames()
            return false
        }
        return false
    }

    fun dragAndDropBlockNameExist(name: String): Boolean {
        return arrayWithDragDropBlockNames.contains(name)
    }

    fun updateFileWithDragDropBlockNames() {
        for (dragDropBlockName: String in arrayWithDragDropBlockNames) {
            //Add new name to projectNamesFile.txt, should check if name already exists
            val fwProjectsNamesFile = FileWriter(File(context.filesDir, customDragDropBlockSaveFile), true)
            fwProjectsNamesFile.write(dragDropBlockName + "\n")
            Log.e("CUSTOM_LOG", "Saved name:  $dragDropBlockName")
            fwProjectsNamesFile.flush()
            fwProjectsNamesFile.close()
        }
    }

    fun loadCustomDragDropBlock(dragDropBlockName: String): DragDropBlock? {
        var dragDropBlock: DragDropBlock? = null
        var count = 0

        //null would be a better init, but don't know if i can change DragDropBlock to accept null?
        var commandReadFromFile: String = ""
        var directionImageReadFromFile: Int = -1
        var displayParameterReadFromFile: Int = -1
        var dragImageReadFromFile: Int = -1
        var parameterReadFromFile: Int = -1
        var textReadFromFile: String = ""
        var type: DragDropBlock.e_type = DragDropBlock.e_type.CUSTOM

        File(context.filesDir, "$dragDropBlockName.txt").useLines { lines ->
            lines.forEach {
                when (count) {
                    0 -> commandReadFromFile = it
                    1 -> directionImageReadFromFile = it.toInt()
                    2 -> displayParameterReadFromFile = it.toInt()
                    3 -> dragImageReadFromFile = it.toInt()
                    4 -> parameterReadFromFile = it.toInt()
                    5 -> textReadFromFile = it
                    6 -> type = DragDropBlock.e_type.valueOf(it.toString())
                }
                if (count < 6) {
                    count++
                } else {
                    Log.e("CUSTOM_LOG", "Type read was: $type")
                    count = 0
                    dragDropBlock = DragDropBlock(
                        dragImageReadFromFile,
                        directionImageReadFromFile,
                        textReadFromFile,
                        commandReadFromFile,
                        parameterReadFromFile,
                        displayParameterReadFromFile,
                        type
                    )
                }
            }
            return dragDropBlock
        }
    }

    fun loadNamesOfCustomDragDropBlocks() {
        if(File(context.filesDir, customDragDropBlockSaveFile).isFile){
            File(context.filesDir, customDragDropBlockSaveFile).bufferedReader().forEachLine {
                arrayWithDragDropBlockNames.add(it)
            }
        }
    }

    fun getArrayWithNamesOfCustomDragDropBlocks() : ArrayList<String>{
        return arrayWithDragDropBlockNames
    }


}