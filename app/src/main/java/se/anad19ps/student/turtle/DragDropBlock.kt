package se.anad19ps.student.turtle

import java.io.Serializable

/*Holds data. Make sure to add data for parameter time and command*/
data class DragDropBlock(
    val dragImage: Int,
    val directionImage: Int,
    val text: String,
    val command: String,
    var parameter: Double,
    var displayParameter: Double,
    val type: e_type,
    val parameterEnabled : Boolean
) : Serializable{
    enum class e_type{
        DRIVE,
        MODULE,
        CUSTOM
    }
}
