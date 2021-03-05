package se.anad19ps.student.turtle

import java.io.Serializable

data class ProgrammingSpinnerItem(
    val image: Int,
    val text: String,
    val type: DragDropBlock.e_type
)