package com.iqsoft.my96chat.models

import android.os.Parcel
import android.os.Parcelable

data class Message (
    val text: String = "",
    val sender: String = ""
        ):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeString(sender)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }
}