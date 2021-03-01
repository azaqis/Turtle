package se.anad19ps.student.turtle

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.util.*
import kotlin.collections.ArrayList

class SaveFilesManager(con : Context) {

    companion object{
        private const val projectNamesFile = "projectNames.txt"
        private const val lastOpenProjectFile = "lastOpenProject.txt"
        private var  lastOpenProject : String ?= null
        private lateinit var context : Context
        private var arrayWithProjectNames = arrayListOf<String>()
    }

    /*
   TODO IN THIS FILE
    - Look over what should be private and not private
    - Delete test code
    - Check if names is logical
    - Comment code
    - Remove static strings and link to strings file instead
    - Maybe it's dumb to send context in this way? Might send it as a parameter to each function? In that case, it would be possible to use the same instance of SaveFileManager in different activities
    - arrayWithProjectNames should be null if no name is available
    */

    init {
        context = con

        if(!File(context.filesDir, projectNamesFile).isFile){

            File(context.filesDir, projectNamesFile).writeText("")

            if(File(context.filesDir, projectNamesFile).isFile){
                Log.e("FILE_LOG", "projectNames.txt was successfully created")

                //TEST CODE BEGIN, This code only adds test projects on first opening of the app, after first time opening, these projects should be opened by reading from the save file
                var itemList = mutableListOf<DragDropBlock>()
                val num = 5

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
                        "Insert text test $i",
                        "Garbage command",
                        1,
                        1,
                        DragDropBlock.e_type.DRIVE
                    )
                    itemList.add(item)
                }

                saveProject("MyProject1:::::" + (Calendar.getInstance().get((Calendar.SECOND)).toString()), itemList, false)
                saveProject("MyProject2:::::" + (Calendar.getInstance().get((Calendar.SECOND)).toString()), itemList, false)
                saveProject("MyProject3:::::" + (Calendar.getInstance().get((Calendar.SECOND)).toString()), itemList, false)
                saveProject("MyProject4:::::" + (Calendar.getInstance().get((Calendar.SECOND)).toString()), itemList, false)
                saveProject("MyProject5:::::" + (Calendar.getInstance().get((Calendar.SECOND)).toString()), itemList, false)
                saveProject("MyProject6:::::" + (Calendar.getInstance().get((Calendar.SECOND)).toString()), itemList, false)
                saveProject("MyProject7:::::" + (Calendar.getInstance().get((Calendar.SECOND)).toString()), itemList, false)
                //TEST CODE END
            }
            else{
                Log.e("FILE_LOG", "projectNames.txt could not be created")
            }
        }
        else{
            Log.e("FILE_LOG", "projectNames.txt was already created")
            loadNamesOfProjects()
        }

        if(!File(context.filesDir, lastOpenProjectFile).isFile){
            File(context.filesDir, lastOpenProjectFile).writeText("")
            if(File(context.filesDir, lastOpenProjectFile).isFile){
                Log.e("FILE_LOG", "lastOpenProject.txt was successfully created")
            }
            else{
                Log.e("FILE_LOG", "lastOpenProject.txt could not be created")
            }
        }
        else{
            setLastOpenedProject(loadLastOpenedProjectFromFile())
        }
    }

    fun saveProject(projectName: String, saveDataList: MutableList<DragDropBlock>, allowOverWriting : Boolean) : Boolean{
        Log.e("FILE_LOG", "Request to save: $projectName")
        if(!addNewName(projectName) && !allowOverWriting){
            Log.e("FILE_LOG", "Response that name exists $projectName")
            return false
        }

        setLastOpenedProject(projectName)
        saveProjectToFile(projectName, saveDataList)
        return true
    }

    private fun saveProjectToFile(projectName: String, saveDataList: MutableList<DragDropBlock>){
        Log.e("FILE_LOG", "Saving: $projectName")

        //If append is changed to false this code should not be needed, this code is to clear file from text
        if (File(context.filesDir, "$projectName.txt").isFile) {
            File(context.filesDir, "$projectName.txt").delete()
        }

        //Save DragDropBlock info, append: true should maybe be changed to false and code over might then not be needed
        File(context.filesDir, "$projectName.txt").createNewFile()
        val fwProjectSaveFile = FileWriter(File(context.filesDir, "$projectName.txt"), true)
        for (data: DragDropBlock in saveDataList) {
            fwProjectSaveFile.write(data.command + "\n")
            fwProjectSaveFile.write(data.directionImage.toString() + "\n")
            fwProjectSaveFile.write(data.displayParameter.toString() + "\n")
            fwProjectSaveFile.write(data.dragImage.toString() + "\n")
            fwProjectSaveFile.write(data.parameter.toString() + "\n")
            fwProjectSaveFile.write(data.text + "\n")
            fwProjectSaveFile.write(data.type.toString() + "\n")
            Log.e("FILE_LOG", "Saved a DragDropBlock in: $projectName")
        }
        fwProjectSaveFile.flush()
        fwProjectSaveFile.close()
    }

    fun projectNameExist(name : String) : Boolean{
        //Change to return on same line, written this way for dubuging
        val nameExist = arrayWithProjectNames.contains(name)
        return nameExist
    }

    fun addNewName(projectName: String) : Boolean{
        if(!projectNameExist(projectName)){
            arrayWithProjectNames.add(projectName)
            updateNameFile()
            return true
        }
        return false
    }

    fun updateNameFile(){
        val fwProjectsNamesFile = FileWriter(File(context.filesDir, projectNamesFile), false)
        for(name : String  in arrayWithProjectNames){
            fwProjectsNamesFile.write(name + "\n")
            Log.e("FILE_LOG", "Saved name to file:  $name")
        }
        fwProjectsNamesFile.flush()
        fwProjectsNamesFile.close()
    }

    fun deleteProject(projectName : String) : Boolean{
        if(projectNameExist(projectName)){
            if(File(context.filesDir, "$projectName.txt").delete()){
                arrayWithProjectNames.remove(projectName)
                Log.e("FILE_LOG", "Deleted: " + projectName)
                updateNameFile()
                setLastOpenedProject(null)
                return true
            }
        }
        Log.e("FILE_LOG", "Could not delete: " + projectName)
        return false
    }

    fun loadNamesOfProjects() {
        arrayWithProjectNames.clear()
        Log.e("FILE_LOG", "Loading names requested")
        if(File(context.filesDir, projectNamesFile).isFile) {
            File(context.filesDir, projectNamesFile).useLines { lines ->
                lines.forEach {
                    val readVal = it
                    Log.e("FILE_LOG", "Loaded from saved names file $readVal")
                    arrayWithProjectNames.add(readVal)
                }
            }
        }
    }

    fun getArrayWithNames() : ArrayList<String>{
        for(name : String in arrayWithProjectNames){
            Log.e("FILE_LOG", "Returning array with names, array contaned name: $name")
        }
        return arrayWithProjectNames
    }

    fun loadProject(projectName: String): ArrayList<DragDropBlock> {
        var count = 0;
        var projectItemsList = ArrayList<DragDropBlock>()

        //null would be a better init, but don't know if i can change DragDropBlock to accept null?
        var commandReadFromFile: String = ""
        var directionImageReadFromFile: Int = -1
        var displayParameterReadFromFile: Int = -1
        var dragImageReadFromFile: Int = -1
        var parameterReadFromFile: Int = -1
        var textReadFromFile: String = ""
        var type: DragDropBlock.e_type = DragDropBlock.e_type.CUSTOM

        File(context.filesDir,"$projectName.txt").useLines { lines ->
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
                    Log.e("FILE_LOG", "Type read was: $type")
                    count = 0
                    projectItemsList.add(
                        DragDropBlock(
                            dragImageReadFromFile,
                            directionImageReadFromFile,
                            textReadFromFile,
                            commandReadFromFile,
                            parameterReadFromFile,
                            displayParameterReadFromFile,
                            type
                        )
                    )
                }
            }

            setLastOpenedProject(projectName)
            return projectItemsList
        }
    }

    private fun setLastOpenedProject(projectName : String?) : Boolean{
        if(!getArrayWithNames().contains(projectName)){
            return false
        }

        val fwLastOpenedProject = FileWriter(File(context.filesDir, lastOpenProjectFile), false)

        if(projectName == null){
            fwLastOpenedProject.write("")
        }
        else if(projectNameExist(projectName!!)){
            fwLastOpenedProject.write(projectName)
        }

        lastOpenProject = projectName
        fwLastOpenedProject.flush()
        fwLastOpenedProject.close()
        return true
    }

    private fun loadLastOpenedProjectFromFile() : String?{
        if(File(context.filesDir, lastOpenProjectFile).isFile) {
            File(context.filesDir, lastOpenProjectFile).bufferedReader().use {
                val projectName =  it.readLine()
                if(getArrayWithNames().contains(projectName)){
                    Log.e("FILE_LOG", "Last opened project was: $projectName")
                    return projectName
                }
            }
        }
        return null
    }

    fun getNameOfLastOpenedProject() : String?{
        return lastOpenProject
    }
}