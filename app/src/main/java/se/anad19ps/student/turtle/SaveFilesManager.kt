package se.anad19ps.student.turtle

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter

class SaveFilesManager(con : Context) {

    companion object{
        private val projectNamesFile = "projectNames.txt"
        private lateinit var context : Context
        private var arrayWithProjectNames = arrayListOf<String>()
    }

    init {
        context = con

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
    }

    fun saveProject(projectName: String, saveDataList: MutableList<DragDropBlock>) {
        addNewName(projectName)

        //If append is changed to false this code should not be needed, this code is to clear file from text
        if(File(context.filesDir,"$projectName.txt").isFile){
            File(context.filesDir,"$projectName.txt").delete()
        }

        //Save DragDropBlock info, append: true should maybe be changed to false and code over might then not be needed
        File(context.filesDir,"$projectName.txt").createNewFile()
        val fwProjectSaveFile = FileWriter(File(context.filesDir, "$projectName.txt"), true)
        for (data: DragDropBlock in saveDataList){
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

        /*
        File(this.filesDir,"$projectName.txt").bufferedWriter().use { out ->
            for (data: DragDropBlock in saveDataList) {
                out.write(data.command)
                out.write("\n")
                out.write(data.directionImage)
                out.write("\n")
                out.write(data.displayParameter)
                out.write("\n")
                out.write(data.dragImage)
                out.write("\n")
                out.write(data.parameter)
                out.write("\n")
                out.write(data.text)
                out.write("\n")
            }
        }
         */
    }

    fun projectNameExits(name : String) : Boolean{
        return arrayWithProjectNames.contains(name)
    }

    fun addNewName(projectName: String) : Boolean{
        if(!projectNameExits(projectName)){
            arrayWithProjectNames.add(projectName)
            updateNameFile()
            return true
        }
        return false
    }

    fun updateNameFile(){
        for(projectName : String in arrayWithProjectNames){
            //Add new name to projectNamesFile.txt, should check if name already exists
            val fwProjectsNamesFile = FileWriter(File(context.filesDir, projectNamesFile), true)
            fwProjectsNamesFile.write(projectName + "\n")
            Log.e("FILE_LOG", "Saved name:  $projectName")
            fwProjectsNamesFile.flush()
            fwProjectsNamesFile.close()
        }
    }

    fun deleteProject(projectName : String) : Boolean{
        if(projectNameExits(projectName)){
            if(File(context.filesDir, "$projectName.txt").delete()){
                arrayWithProjectNames.remove(projectName)
                Log.e("FILE_LOG", "Deleted: " + projectName)
                updateNameFile()
                return true
            }
        }
        Log.e("FILE_LOG", "Could not delete: " + projectName)
        return false
    }

    fun loadNamesOfProjects() {
        if(File(context.filesDir, projectNamesFile).isFile){
            File(context.filesDir, projectNamesFile).bufferedReader().forEachLine {
                addNewName(it)
            }
        }
    }

    fun getArrayWithNames() : ArrayList<String>{
        return arrayWithProjectNames
    }

    fun loadProject(projectName: String): ArrayList<DragDropBlock>? {
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
            return projectItemsList
        }
    }
}