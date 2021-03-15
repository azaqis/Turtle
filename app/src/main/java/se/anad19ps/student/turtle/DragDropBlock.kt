package se.anad19ps.student.turtle

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DragDropBlock(
    var dragImage: Int,
    val directionImage: Int,
    var text: String,
    var command: String,
    var parameter: Double,
    var displayParameter: Double,
    val type: BlockType,
    var parameterEnabled: Boolean,
    var idNumber: Long
) : Parcelable {
    enum class BlockType {
        DRIVE,
        MODULE,
        CUSTOM
    }
}