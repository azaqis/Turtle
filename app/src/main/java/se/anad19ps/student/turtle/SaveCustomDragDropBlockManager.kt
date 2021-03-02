package se.anad19ps.student.turtle

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter

class SaveCustomDragDropBlockManager(con : Context) {
    companion object {
        private const val customDragDropBlockSaveFile = "customDragDropBlockSaveFile.txt"
        private lateinit var context : Context
        private var arrayWithDragDropBlock = arrayListOf<DragDropBlock>()
        private var arrayWithDragDropBlockNames = arrayListOf<String>()
    }

    /*
      TODO IN THIS FILE
       - Look over what should be private and not private
       - Delete test code
       - Check if names is logical
       - Comment code
       - Remove static strings and link to strings file instead
       - Maybe it's dumb to send context in this way? Might send it as a parameter to each function? In that case, it would be possible to use the same instance of SaveFileManager in different activities, or is it even needed?
       - arrayWithProjectNames should be null if no name is available
       - Check if name already exist when saving, add parameter for overwriting
       - Maybe add a fun for editing? Or is that needed?
    */

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
            loadCustomDragDropBlocks()
            loadNamesOfCustomDragDropBlocks()
        }
    }

    fun saveDragDropBlock(dragAndDropBlockName: String, dragDropBlock: DragDropBlock, allowOverwriting: Boolean) : Boolean{
        Log.e("CUSTOM_LOG", "Request to save: $dragAndDropBlockName")
        if(!dragAndDropBlockNameExist(dragAndDropBlockName) && !allowOverwriting){
            Log.e("CUSTOM_LOG", "Could not save: $dragAndDropBlockName, name already exists and overwriting was set to false")
            return false
        }
        else if(dragAndDropBlockNameExist(dragAndDropBlockName)){
            var index = arrayWithDragDropBlockNames.indexOf(dragAndDropBlockName)
            arrayWithDragDropBlock[index] = dragDropBlock
            return true
        }
        else{
            arrayWithDragDropBlockNames.add(dragAndDropBlockName)
            arrayWithDragDropBlock.add(dragDropBlock)
            return true
        }
    }

    fun dragAndDropBlockNameExist(name: String): Boolean {
        return arrayWithDragDropBlockNames.contains(name)
    }

    fun loadCustomDragDropBlocks(){
        Log.e("CUSTOM_LOG", "Request to load all DADB")

        var count = 0

        //null would be a better init, but don't know if i can change DragDropBlock to accept null?
        var commandReadFromFile: String = ""
        var directionImageReadFromFile: Int = -1
        var displayParameterReadFromFile: Int = -1
        var dragImageReadFromFile: Int = -1
        var parameterReadFromFile: Int = -1
        var textReadFromFile: String = ""
        var type: DragDropBlock.e_type = DragDropBlock.e_type.CUSTOM

        File(context.filesDir, customDragDropBlockSaveFile).useLines { lines ->
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
                    Log.e("CUSTOM_LOG", "Loaded: $textReadFromFile")
                    count = 0
                    arrayWithDragDropBlock.add(DragDropBlock(dragImageReadFromFile, directionImageReadFromFile, textReadFromFile, commandReadFromFile, parameterReadFromFile, displayParameterReadFromFile, type))
                }
            }
        }
    }

    //Could merge into the same function as loadImagesOfCustomDragDropBlocks and have file and array as parameter
    fun loadNamesOfCustomDragDropBlocks() {
        var index = 0;
        for(dragDropBlock : DragDropBlock in arrayWithDragDropBlock){
            arrayWithDragDropBlockNames.add(arrayWithDragDropBlock[index].text)
            index++;
        }
    }

    fun getArrayWithNamesOfCustomDragDropBlocks() : ArrayList<String>{
        return arrayWithDragDropBlockNames
    }

    fun getArrayWithCustomDragDropBlocks() : ArrayList<DragDropBlock>{
        return arrayWithDragDropBlock
    }
}