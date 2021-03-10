package se.anad19ps.student.turtle

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import kotlin.collections.ArrayList

class SaveFilesManager(con : Context) {

    companion object{
        private const val projectNamesFile = "projectNames.txt"
        private const val lastOpenProjectFile = "lastOpenProject.txt"
        private const val startOfProjectSaveFilesName = "save_project_file_"
        private var  lastOpenProject : String ?= null
        private lateinit var context : Context
        private var arrayWithProjectNames = arrayListOf<String>()
    }

    /*
   TODO IN THIS FILE
    - Check if names is logical
    - Remove static strings and link to strings file instead
    - Maybe it's dumb to send context in this way? Might send it as a parameter to each function? In that case, it would be possible to use the same instance of SaveFileManager in different activities
    */

    init {
        context = con

        //Check if projectNames.txt, otherwise it is probably the first time opening the app (or local storage has been cleaned) and projectNames.txt then need to be created. If it already exists, we load all names from the file
        if(!File(context.filesDir, projectNamesFile).isFile){

            File(context.filesDir, projectNamesFile).writeText("")

            if(File(context.filesDir, projectNamesFile).isFile){
                Log.e("FILE_LOG", "projectNames.txt was successfully created")
            }
            else{
                Log.e("FILE_LOG", "projectNames.txt could not be created")
            }
        }
        else{
            Log.e("FILE_LOG", "projectNames.txt was already created")
            loadNamesOfProjects()
        }

        //Check if lastOpenProject.txt, otherwise it is probably the first time opening the app (or local storage has been cleaned) and lastOpenProject.txt then need to be created, If it already exists, we load the last open project from the file
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
            setLastOpenedProject(getNameOfLastOpenedProjectFromFile())
        }
    }

    fun createNewEmptyProject(name : String, allowOverWriting: Boolean) : Boolean{
        return saveProject(name, ArrayList<DragDropBlock>(), allowOverWriting)
    }

    fun saveProject(projectName: String, saveDataList: MutableList<DragDropBlock>, allowOverWriting : Boolean) : Boolean{
        Log.e("FILE_LOG", "Request to save: $projectName")

        //Tries to add new project name, if it is not possible to add the new name (due to name already exists) and allowOverWriting is not allowed then saveProject will return false indicating that it could not save project
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

        //Creates a file to save project in with the standard start of name + same name as the project
        File(context.filesDir, "$startOfProjectSaveFilesName$projectName.txt").createNewFile()

        val fwProjectSaveFile = FileWriter(File(context.filesDir, "$startOfProjectSaveFilesName$projectName.txt"), false)

        //Loop through all blocks in saveDataList, on each line in the save file, save a attribute for a DragDropBlock
        for (data: DragDropBlock in saveDataList) {
            fwProjectSaveFile.write(data.command + "\n")
            fwProjectSaveFile.write(data.directionImage.toString() + "\n")
            fwProjectSaveFile.write(data.displayParameter.toString() + "\n")
            fwProjectSaveFile.write(data.dragImage.toString() + "\n")
            fwProjectSaveFile.write(data.parameter.toString() + "\n")
            fwProjectSaveFile.write(data.text + "\n")
            fwProjectSaveFile.write(data.type.toString() + "\n")
            fwProjectSaveFile.write(data.parameterEnabled.toString() + "\n")
            Log.e("FILE_LOG", "Saved a DragDropBlock in: $projectName")
        }
        fwProjectSaveFile.flush()
        fwProjectSaveFile.close()
    }

    private fun projectNameExist(name : String) : Boolean{
        return arrayWithProjectNames.contains(name)
    }

    private fun addNewName(projectName: String) : Boolean{
        if(!projectNameExist(projectName)){
            arrayWithProjectNames.add(projectName)
            updateNameFile()
            return true
        }
        return false
    }

    private fun updateNameFile(){
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
            if(File(context.filesDir, "$startOfProjectSaveFilesName$projectName.txt").delete()){
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

    private fun loadNamesOfProjects() {
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
        return arrayWithProjectNames
    }

    fun loadProject(projectName: String): ArrayList<DragDropBlock> {
        var count = 0;
        var projectItemsList = ArrayList<DragDropBlock>()

        //null would be a better init, but don't know if i can change DragDropBlock to accept null?
        var commandReadFromFile: String = ""
        var directionImageReadFromFile: Int = -1
        var displayParameterReadFromFile: Double = -1.0
        var dragImageReadFromFile: Int = -1
        var parameterReadFromFile: Double = -1.0
        var textReadFromFile: String = ""
        var type: DragDropBlock.e_type = DragDropBlock.e_type.CUSTOM
        var parameterEnabled: Boolean = false
        var idNumber: Long = 0

        //Loop through every line in the file. Reading in the same order as we are writing to file in saveProjectToFile.
        // We know that a DragDropBlock contains 8 attributes. Therefore we know on the 8th iteration that we have read a
        // complete DragDropBlock and can now add this block to the array
        File(context.filesDir,"$startOfProjectSaveFilesName$projectName.txt").useLines { lines ->
            lines.forEach {
                when (count) {
                    0 -> commandReadFromFile = it
                    1 -> directionImageReadFromFile = it.toInt()
                    2 -> displayParameterReadFromFile = it.toDouble()
                    3 -> dragImageReadFromFile = it.toInt()
                    4 -> parameterReadFromFile = it.toDouble()
                    5 -> textReadFromFile = it
                    6 -> type = DragDropBlock.e_type.valueOf(it.toString())
                    7 -> parameterEnabled = it.toBoolean()
                    8 -> idNumber = it.toLong()
                }
                if (count < 7) {
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
                            type,
                            parameterEnabled,
                            idNumber
                        )
                    )
                }
            }

            setLastOpenedProject(projectName)
            return projectItemsList
        }
    }

    private fun setLastOpenedProject(projectName : String?) : Boolean{
        val fwLastOpenedProject = FileWriter(File(context.filesDir, lastOpenProjectFile), false)

        if(projectName == null){
            fwLastOpenedProject.write("")
        }
        else if(!getArrayWithNames().contains(projectName)){
            return false
        }
        else {
            fwLastOpenedProject.write(projectName)
        }

        lastOpenProject = projectName
        fwLastOpenedProject.flush()
        fwLastOpenedProject.close()
        return true
    }

    private fun getNameOfLastOpenedProjectFromFile() : String?{
        if(File(context.filesDir, lastOpenProjectFile).isFile) {
            File(context.filesDir, lastOpenProjectFile).bufferedReader().use {
                val projectName =  it.readLine()
                if(getArrayWithNames().contains(projectName)){
                    Log.e("FILE_LOG", "Last opened project was: $projectName")
                    return projectName
                }
                else{
                    return null
                }
            }
        }
        return null
    }

    fun getNameOfLastOpenedProject() : String?{
        return lastOpenProject
    }
}