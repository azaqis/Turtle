package se.anad19ps.student.turtle

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import java.io.File
import kotlinx.android.synthetic.main.activity_saved_projects.*

class SavedProjectsActivity : AppCompatActivity() {

    companion object {
        val projectNamesFile = "projectNames.txt"

        lateinit var arrayWithProjectNames: List<String>
        lateinit var savedProjectsListViewAdapter : ArrayAdapter<String>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_projects)

        loadNamesOfProjects()

        savedProjectsListViewAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayWithProjectNames)
        savedProjectsListView.adapter = savedProjectsListViewAdapter
    }

    fun saveProject(projectName: String, saveDataList: MutableList<DragDropBlock>) {
        File(projectNamesFile).bufferedWriter().use { out ->
            out.write("$projectName.txt")
            out.write("\n")
        }

        File("$projectName.txt").bufferedWriter().use { out ->
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
    }

    fun loadNamesOfProjects() {
        arrayWithProjectNames = File(projectNamesFile).readLines()
        savedProjectsListViewAdapter.notifyDataSetChanged()
    }

    fun loadProject(projectName: String): MutableList<DragDropBlock>? {
        var count = 0;
        var projectItemsList = mutableListOf<DragDropBlock>()

        //null would be a better init, but don't know if i can change DragDropBlock to accept null?
        var commandReadFromFile: String = ""
        var directionImageReadFromFile: Int = -1
        var displayParameterReadFromFile: Int = -1
        var dragImageReadFromFile: Int = -1
        var parameterReadFromFile: Int = -1
        var textReadFromFile: String = ""

        File("$projectName.txt").useLines { lines ->
            lines.forEach {
                when (count) {
                    0 -> commandReadFromFile = it
                    1 -> directionImageReadFromFile = it.toInt()
                    2 -> displayParameterReadFromFile = it.toInt()
                    3 -> dragImageReadFromFile = it.toInt()
                    4 -> parameterReadFromFile = it.toInt()
                    5 -> textReadFromFile = it
                }
                if (count < 5) {
                    count++
                } else {
                    count = 0
                    projectItemsList.add(
                        DragDropBlock(
                            dragImageReadFromFile,
                            directionImageReadFromFile,
                            textReadFromFile,
                            commandReadFromFile,
                            parameterReadFromFile,
                            displayParameterReadFromFile
                        )
                    )
                }
            }
            return projectItemsList
        }
    }

}