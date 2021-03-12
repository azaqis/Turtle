package se.anad19ps.student.turtle

import android.content.Context
import android.provider.Settings.Global.getString
import android.util.ArrayMap
import android.util.Log
import android.util.SparseArray
import java.io.File
import java.io.FileWriter
import java.io.Serializable
import kotlin.collections.ArrayList

class SaveFilesManager(contextForInit: Context) : Serializable {

    companion object {
        private const val projectNamesFile = "projectNames.txt"
        private const val lastOpenProjectFile = "lastOpenProject.txt"
        private const val startOfProjectSaveFilesName = "save_project_file_"
        private var lastOpenProject: String? = null

    }
    private var arrayWithProjectNames = arrayListOf<String>()
    private var hashMapWithProjects: HashMap<String, ArrayList<DragDropBlock>> = HashMap()

    init {

        //Check if projectNames.txt, otherwise it is probably the first time opening the app (or local storage has been cleaned) and projectNames.txt then need to be created. If it already exists, we load all names from the file
        if (!File(contextForInit.filesDir, projectNamesFile).isFile) {

            File(contextForInit.filesDir, projectNamesFile).writeText("")

            if (File(contextForInit.filesDir, projectNamesFile).isFile) {
                Log.e("FILE_LOG", "projectNames.txt was successfully created")
            } else {
                Log.e("FILE_LOG", "projectNames.txt could not be created")
            }
        } else {
            Log.e("FILE_LOG", "projectNames.txt was already created")
            loadNamesOfProjects(contextForInit)
            loadAllProjectsFromSaveFile(contextForInit)
        }

        //Check if lastOpenProject.txt, otherwise it is probably the first time opening the app (or local storage has been cleaned) and lastOpenProject.txt then need to be created, If it already exists, we load the last open project from the file
        if (!File(contextForInit.filesDir, lastOpenProjectFile).isFile) {
            File(contextForInit.filesDir, lastOpenProjectFile).writeText("")
            if (File(contextForInit.filesDir, lastOpenProjectFile).isFile) {
                Log.e("FILE_LOG", "lastOpenProject.txt was successfully created")
            } else {
                Log.e("FILE_LOG", "lastOpenProject.txt could not be created")
            }
        } else {
            setLastOpenedProject(getNameOfLastOpenedProjectFromFile(contextForInit), contextForInit)
        }
    }

    fun createNewEmptyProject(
        name: String,
        allowOverWriting: Boolean,
        currentContext: Context
    ): Boolean {
        val testArray = arrayListOf<DragDropBlock>()
        return saveProject(name, testArray, allowOverWriting, currentContext)
    }

    fun saveProject(
        projectName: String,
        saveDataList: MutableList<DragDropBlock>,
        allowOverWriting: Boolean,
        currentContext: Context
    ): Boolean {
        Log.d("FILE_LOG", "Request to save: $projectName")

        //Tries to add new project name, if it is not possible to add the new name (due to name already exists) and allowOverWriting is not allowed then saveProject will return false indicating that it could not save project
        if (!addNewName(projectName, currentContext) && !allowOverWriting) {
            Log.d("FILE_LOG", "Response that name exists $projectName")
            return false
        }

        setLastOpenedProject(projectName, currentContext)
        hashMapWithProjects[projectName] = saveDataList as ArrayList<DragDropBlock>
        saveProjectToFile(projectName, saveDataList, currentContext)
        return true
    }

    private fun saveProjectToFile(
        projectName: String,
        saveDataList: MutableList<DragDropBlock>,
        currentContext: Context
    ) {
        //Creates a file to save project in with the standard start of name + same name as the project
        File(
            currentContext.filesDir,
            "$startOfProjectSaveFilesName$projectName.txt"
        ).createNewFile()

        val fwProjectSaveFile = FileWriter(
            File(
                currentContext.filesDir,
                "$startOfProjectSaveFilesName$projectName.txt"
            ), false
        )

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

    private fun projectNameExist(name: String): Boolean {
        return arrayWithProjectNames.contains(name)
    }

    private fun addNewName(projectName: String, currentContext: Context): Boolean {
        if (!projectNameExist(projectName)) {
            arrayWithProjectNames.add(projectName)
            updateNameFile(currentContext)
            return true
        }
        return false
    }

    private fun updateNameFile(currentContext: Context) {
        val fwProjectsNamesFile = FileWriter(File(currentContext.filesDir, projectNamesFile), false)
        for (name: String in arrayWithProjectNames) {
            fwProjectsNamesFile.write(name + "\n")
            Log.d("FILE_LOG", "Saved name to file:  $name")
        }
        fwProjectsNamesFile.flush()
        fwProjectsNamesFile.close()
    }

    fun deleteProject(projectName: String, currentContext: Context): Boolean {
        if (projectNameExist(projectName)) {
            if (File(
                    currentContext.filesDir,
                    "$startOfProjectSaveFilesName$projectName.txt"
                ).delete()
            ) {
                arrayWithProjectNames.remove(projectName)
                Log.d("FILE_LOG", "Deleted: $projectName")
                updateNameFile(currentContext)
                setLastOpenedProject(null, currentContext)
                return true
            }
        }
        Log.d("FILE_LOG", "Could not delete: $projectName")
        return false
    }

    private fun loadNamesOfProjects(currentContext: Context) {
        arrayWithProjectNames.clear()
        Log.e("FILE_LOG", "Loading names requested")
        if (File(currentContext.filesDir, projectNamesFile).isFile) {
            File(currentContext.filesDir, projectNamesFile).useLines { lines ->
                lines.forEach {
                    val readVal = it
                    Log.e("FILE_LOG", "Loaded from saved names file $readVal")
                    arrayWithProjectNames.add(readVal)
                }
            }
        }
    }

    fun getArrayWithNames(): ArrayList<String> {
        return arrayWithProjectNames
    }

    fun getProject(projectName: String, currentContext: Context): ArrayList<DragDropBlock>? {
        Log.e("FILE_LOG", "Request to get: $projectName")

        for(keys in hashMapWithProjects){
            Log.e("FILE_LOG", "Hashmap contains" + keys.component1())
        }

         if (hashMapWithProjects.containsKey(projectName)) {
            setLastOpenedProject(projectName, currentContext)
            return  hashMapWithProjects[projectName]!!
        } else {
            return null
        }
    }

    private fun loadAllProjectsFromSaveFile(currentContext: Context) {
        for (projectName: String in arrayWithProjectNames) {
            var count = 0
            val projectItemsList = ArrayList<DragDropBlock>()

            var commandReadFromFile = ""
            var directionImageReadFromFile: Int = -1
            var displayParameterReadFromFile: Double = -1.0
            var dragImageReadFromFile: Int = -1
            var parameterReadFromFile: Double = -1.0
            var textReadFromFile = ""
            var typeReadFromFile: DragDropBlock.e_type = DragDropBlock.e_type.CUSTOM
            var parameterEnabledReadFromFile = false
            var idNumber: Long = 0

            //Loop through every line in the file. Reading in the same order as we are writing to file in saveProjectToFile.
            // We know that a DragDropBlock has 8 saved attributes. Therefore we know on the 8th iteration that we have read a
            // complete DragDropBlock and can now add this block to the array
            File(
                currentContext.filesDir,
                "$startOfProjectSaveFilesName$projectName.txt"
            ).useLines { lines ->
                lines.forEach {
                    when (count) {
                        0 -> commandReadFromFile = it
                        1 -> directionImageReadFromFile = it.toInt()
                        2 -> displayParameterReadFromFile = it.toDouble()
                        3 -> dragImageReadFromFile = it.toInt()
                        4 -> parameterReadFromFile = it.toDouble()
                        5 -> textReadFromFile = it
                        6 -> typeReadFromFile = DragDropBlock.e_type.valueOf(it)
                        7 -> parameterEnabledReadFromFile = it.toBoolean()
                    }
                    if (count < 7) {
                        count++
                    } else {
                        Log.e("FILE_LOG", "Type read was: $typeReadFromFile")
                        count = 0
                        projectItemsList.add(
                            DragDropBlock(
                                dragImageReadFromFile,
                                directionImageReadFromFile,
                                textReadFromFile,
                                commandReadFromFile,
                                parameterReadFromFile,
                                displayParameterReadFromFile,
                                typeReadFromFile,
                                parameterEnabledReadFromFile,
                                idNumber
                            )
                        )
                    }
                    hashMapWithProjects[projectName] = projectItemsList
                    idNumber++
                }
            }
        }
    }

    fun updateCustomDragDropBlocksInAllProjects(
        oldCustomDragDropBlock: DragDropBlock,
        updatedCustomDragDropBlock: DragDropBlock,
        currentContext: Context
    ) {
        for (project in hashMapWithProjects) {
            var projectModified = false
            for (dragDropBlock in project.component2()) {
                if (dragDropBlock.text == oldCustomDragDropBlock.text) {
                    dragDropBlock.text = updatedCustomDragDropBlock.text
                    dragDropBlock.parameterEnabled = updatedCustomDragDropBlock.parameterEnabled
                    dragDropBlock.command = updatedCustomDragDropBlock.command
                    projectModified = true
                }
            }
            if (projectModified) {
                saveProject(project.component1(), project.component2(), true, currentContext)
            }
        }

    }

    private fun setLastOpenedProject(projectName: String?, currentContext: Context): Boolean {
        val fwLastOpenedProject =
            FileWriter(File(currentContext.filesDir, lastOpenProjectFile), false)
        Log.e("FILE_LOG", "Request to set as last opened project: $projectName")

        if (projectName == null) {
            Log.e("FILE_LOG", "Set last opened project to null")
            fwLastOpenedProject.write("")
        } else if (!getArrayWithNames().contains(projectName)) {
            Log.e("FILE_LOG", "Can not set last project because name does not exist")
            return false
        } else {
            Log.e("FILE_LOG", "Set last opened project to: $projectName")
            fwLastOpenedProject.write(projectName)
        }

        lastOpenProject = projectName
        fwLastOpenedProject.flush()
        fwLastOpenedProject.close()
        return true
    }

    private fun getNameOfLastOpenedProjectFromFile(currentContext: Context): String? {
        Log.e("FILE_LOG", "Request to last opened project")
        if (File(currentContext.filesDir, lastOpenProjectFile).isFile) {
            File(currentContext.filesDir, lastOpenProjectFile).bufferedReader().use {
                val projectName = it.readLine()
                if (getArrayWithNames().contains(projectName)) {
                    Log.e("FILE_LOG", "Returning as last opened project: $projectName")
                    return projectName
                } else {
                    Log.e("FILE_LOG", "Returning null as last opened project")
                    return null
                }
            }
        }
        Log.e("FILE_LOG", "Returning null as last opened project")
        return null
    }

    fun getNameOfLastOpenedProject(): String? {
        return lastOpenProject
    }
}