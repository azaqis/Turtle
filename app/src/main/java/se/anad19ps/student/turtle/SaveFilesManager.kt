package se.anad19ps.student.turtle

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import kotlin.collections.ArrayList

@Suppress("UNCHECKED_CAST")
class SaveFilesManager(con: Context) {

    companion object {
        private const val PROJECTS_NAME_FILE = "projectNames.txt"
        private const val LAST_OPEN_PROJECT_FILE = "lastOpenProject.txt"
        private const val START_OF_PROJECT_SAVE_FILE_NAME = "save_project_file_"
        private const val EMPTY_STRING = ""
        private const val NEW_LINE_STRING = "\n"

        private var lastOpenProject: String? = null
        private var arrayWithProjectNames = arrayListOf<String>()
        private var arrayWithProjects = arrayListOf<ArrayList<DragDropBlock>>()
    }

    init {
        //Check if projectNames.txt, otherwise it is probably the first time opening the app (or local storage has been cleaned) and projectNames.txt then need to be created. If it already exists, we load all names from the file
        if (!File(con.filesDir, PROJECTS_NAME_FILE).isFile) {

            File(con.filesDir, PROJECTS_NAME_FILE).writeText("")

            if (File(con.filesDir, PROJECTS_NAME_FILE).isFile) {
                Log.d("FILE_LOG", "projectNames.txt was successfully created")
            } else {
                Log.d("FILE_LOG", "projectNames.txt could not be created")
            }
        } else {
            Log.d("FILE_LOG", "projectNames.txt was already created")
            loadNamesOfProjects(con)
            loadAllProjects(con)
        }

        //Check if lastOpenProject.txt, otherwise it is probably the first time opening the app (or local storage has been cleaned) and lastOpenProject.txt then need to be created, If it already exists, we load the last open project from the file
        if (!File(con.filesDir, LAST_OPEN_PROJECT_FILE).isFile) {
            File(con.filesDir, LAST_OPEN_PROJECT_FILE).writeText("")
            if (File(con.filesDir, LAST_OPEN_PROJECT_FILE).isFile) {
                Log.d("FILE_LOG", "lastOpenProject.txt was successfully created")
            } else {
                Log.d("FILE_LOG", "lastOpenProject.txt could not be created")
            }
        } else {
            setLastOpenedProject(getNameOfLastOpenedProjectFromFile(con), con)
        }
    }

    fun createNewEmptyProject(projectName: String, allowOverWriting: Boolean, context: Context): Boolean {
        return saveProject(projectName, ArrayList(), allowOverWriting, context)
    }

    fun saveProject(
        projectName: String,
        arrayWithDragDropBlocks: MutableList<DragDropBlock>,
        allowOverWriting: Boolean,
        context: Context
    ): Boolean {
        Log.d("FILE_LOG", "Request to save: $projectName")

        //Tries to add new project name, if it is not possible to add the new name (due to name already exists) and allowOverWriting is not allowed then saveProject will return false indicating that it could not save project
        if (!addNewName(projectName, context) && !allowOverWriting) {
            Log.d("FILE_LOG", "Response that name exists $projectName")
            return false
        }

        setLastOpenedProject(projectName, context)
        val indexOfProject = arrayWithProjectNames.indexOf(projectName)
        arrayWithProjects.add(indexOfProject, arrayWithDragDropBlocks as ArrayList<DragDropBlock>)
        saveProjectToFile(projectName, arrayWithDragDropBlocks, context)
        return true
    }

    private fun saveProjectToFile(
        projectName: String,
        arrayWithDragDropBlocks: MutableList<DragDropBlock>,
        context : Context
    ) {
        Log.d("FILE_LOG", "Saving: $projectName")

        //Creates a fileWriter that saves in a file with the startOfProjectSaveFilesName + same name as the project
        val fwProjectSaveFile = FileWriter(
            File(context.filesDir, "$START_OF_PROJECT_SAVE_FILE_NAME$projectName.txt"),
            false
        )

        //Loop through all blocks in arrayWithDragDropBlocks, on each line in the save file, save a attribute for a DragDropBlock
        for (data: DragDropBlock in arrayWithDragDropBlocks) {
            fwProjectSaveFile.write(data.command + NEW_LINE_STRING)
            fwProjectSaveFile.write(data.directionImage.toString() + NEW_LINE_STRING)
            fwProjectSaveFile.write(data.displayParameter.toString() + NEW_LINE_STRING)
            fwProjectSaveFile.write(data.dragImage.toString() + NEW_LINE_STRING)
            fwProjectSaveFile.write(data.parameter.toString() + NEW_LINE_STRING)
            fwProjectSaveFile.write(data.text + NEW_LINE_STRING)
            fwProjectSaveFile.write(data.type.toString() + NEW_LINE_STRING)
            fwProjectSaveFile.write(data.parameterEnabled.toString() + NEW_LINE_STRING)
            Log.e("FILE_LOG", "Saved a DragDropBlock in: $projectName")
        }
        fwProjectSaveFile.flush()
        fwProjectSaveFile.close()
    }

    private fun saveAllProjectsToFile(context: Context) {
        for (projectName in arrayWithProjectNames) {
            val index = arrayWithProjectNames.indexOf(projectName)
            saveProjectToFile(projectName, arrayWithProjects[index], context)
        }
    }

    private fun projectNameExist(projectName: String): Boolean {
        return arrayWithProjectNames.contains(projectName)
    }

    private fun addNewName(projectName: String, context: Context): Boolean {
        if (!projectNameExist(projectName)) {
            arrayWithProjectNames.add(projectName)
            updateNameFile(context)
            return true
        }
        return false
    }

    private fun updateNameFile(context: Context) {
        val fwProjectsNamesFile = FileWriter(File(context.filesDir, PROJECTS_NAME_FILE), false)
        for (name: String in arrayWithProjectNames) {
            fwProjectsNamesFile.write(name + "\n")
            Log.e("FILE_LOG", "Saved name to file:  $name")
        }
        fwProjectsNamesFile.flush()
        fwProjectsNamesFile.close()
    }

    fun deleteProject(projectName: String, context: Context): Boolean {
        if (projectNameExist(projectName)) {
            if (File(
                    context.filesDir,
                    "$START_OF_PROJECT_SAVE_FILE_NAME$projectName.txt"
                ).delete()
            ) {
                arrayWithProjectNames.remove(projectName)
                Log.e("FILE_LOG", "Deleted: $projectName")
                updateNameFile(context)
                setLastOpenedProject(null, context)
                return true
            }
        }
        Log.e("FILE_LOG", "Could not delete: $projectName")
        return false
    }

    private fun loadNamesOfProjects(context: Context) {
        arrayWithProjectNames.clear()
        Log.e("FILE_LOG", "Loading names requested")
        if (File(context.filesDir, PROJECTS_NAME_FILE).isFile) {
            File(context.filesDir, PROJECTS_NAME_FILE).useLines { lines ->
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

    private fun loadAllProjects(context: Context) {
        for (projectName in arrayWithProjectNames) {
            var count = 0
            val projectItemsList = ArrayList<DragDropBlock>()

            var commandReadFromFile = EMPTY_STRING
            var directionImageReadFromFile: Int = -1
            var displayParameterReadFromFile: Double = -1.0
            var dragImageReadFromFile: Int = -1
            var parameterReadFromFile: Double = -1.0
            var textReadFromFile = EMPTY_STRING
            var type: DragDropBlock.BlockType = DragDropBlock.BlockType.CUSTOM
            var parameterEnabled = false
            var idNumber: Long = 0

            //Loop through every line in the file. Reading in the same order as we are writing to file in saveProjectToFile.
            // We know that a DragDropBlock contains 8 attributes. Therefore we know on the 8th iteration that we have read a
            // complete DragDropBlock and can now add this block to the array
            File(
                context.filesDir,
                "$START_OF_PROJECT_SAVE_FILE_NAME$projectName.txt"
            ).useLines { lines ->
                lines.forEach {
                    when (count) {
                        0 -> commandReadFromFile = it
                        1 -> directionImageReadFromFile = it.toInt()
                        2 -> displayParameterReadFromFile = it.toDouble()
                        3 -> dragImageReadFromFile = it.toInt()
                        4 -> parameterReadFromFile = it.toDouble()
                        5 -> textReadFromFile = it
                        6 -> type = DragDropBlock.BlockType.valueOf(it)
                        7 -> parameterEnabled = it.toBoolean()
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
                        idNumber++
                    }
                }
                val index = arrayWithProjectNames.indexOf(projectName)
                arrayWithProjects.add(index, projectItemsList)
            }
        }
    }

    fun getProject(projectName: String, context: Context): ArrayList<DragDropBlock> {
        setLastOpenedProject(projectName, context)
        val index = arrayWithProjectNames.indexOf(projectName)

        return if (index == -1)
            ArrayList()
        else
            arrayWithProjects[index].clone() as ArrayList<DragDropBlock>
    }

    private fun setLastOpenedProject(projectName: String?, context: Context): Boolean {
        val fwLastOpenedProject = FileWriter(File(context.filesDir, LAST_OPEN_PROJECT_FILE), false)

        if (projectName == null) {
            fwLastOpenedProject.write("")
        } else if (!getArrayWithNames().contains(projectName)) {
            return false
        } else {
            fwLastOpenedProject.write(projectName)
        }

        lastOpenProject = projectName
        fwLastOpenedProject.flush()
        fwLastOpenedProject.close()
        return true
    }

    private fun getNameOfLastOpenedProjectFromFile(context: Context): String? {
        if (File(context.filesDir, LAST_OPEN_PROJECT_FILE).isFile) {
            File(context.filesDir, LAST_OPEN_PROJECT_FILE).bufferedReader().use {
                val projectName = it.readLine()
                return if (getArrayWithNames().contains(projectName)) {
                    Log.e("FILE_LOG", "Last opened project was: $projectName")
                    projectName
                } else {
                    null
                }
            }
        }
        return null
    }

    fun getNameOfLastOpenedProject(): String? {
        return lastOpenProject
    }

    fun updateCustomDragDropBlocksInAllProjects(
        oldDragDropBlock: DragDropBlock,
        updatedDragDropBlock: DragDropBlock,
        context: Context
    ) {
        var hasDoneModification = false
        for (projectName in arrayWithProjectNames) {
            val indexOfProject = arrayWithProjectNames.indexOf(projectName)
            for (dragDropBlock in arrayWithProjects[indexOfProject]) {
                if (dragDropBlock.text == oldDragDropBlock.text) {
                    dragDropBlock.text = updatedDragDropBlock.text
                    dragDropBlock.parameterEnabled = updatedDragDropBlock.parameterEnabled
                    dragDropBlock.command = updatedDragDropBlock.command
                    hasDoneModification = true
                }
            }
        }
        if (hasDoneModification) {
            saveAllProjectsToFile(context)
        }
    }
}