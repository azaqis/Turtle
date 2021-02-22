package se.anad19ps.student.turtle

/*Holds data. Make sure to add data for parameter time and command*/
data class ProgrammingItemDataBlock(
    val dragImage: Int,
    val directionImage: Int,
    val text: String,
    val command: String,
    var parameter: Int,
    var displayParameter: Int,
    val type: e_type
) {
        enum class e_type{
            DRIVE,
            MODULE,
            CUSTOM
        }
}