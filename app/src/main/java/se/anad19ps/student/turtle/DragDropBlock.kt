package se.anad19ps.student.turtle

import android.os.Parcel
import android.os.Parcelable

import java.io.Serializable

data class DragDropBlock(
    var dragImage: Int,
    val directionImage: Int,
    var text: String,
    val command: String,
    var parameter: Double,
    var displayParameter: Double,
    val type: e_type,
    val parameterEnabled : Boolean,
    var idNumber : Long
) : Parcelable, Serializable{
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readSerializable() as e_type,
		parcel.readString()!!.toBoolean(),
        parcel.readLong()
    )
    enum class e_type{
        DRIVE,
        MODULE,
        CUSTOM
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(dragImage)
        parcel.writeInt(directionImage)
        parcel.writeString(text)
        parcel.writeString(command)
        parcel.writeDouble(parameter)
        parcel.writeDouble(displayParameter)
        parcel.writeSerializable(type)
        parcel.writeString(parameterEnabled.toString()) //MAY CAUSE PROBLEMS DUE TO BOOLEAN TYPE!!!!!
        parcel.writeLong(idNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DragDropBlock> {
        override fun createFromParcel(parcel: Parcel): DragDropBlock {
            return DragDropBlock(parcel)
        }

        override fun newArray(size: Int): Array<DragDropBlock?> {
            return arrayOfNulls(size)
        }
    }
}
