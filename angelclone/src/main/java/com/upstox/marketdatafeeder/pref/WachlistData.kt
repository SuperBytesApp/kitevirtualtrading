package com.upstox.marketdatafeeder.pref

import android.os.Parcel
import android.os.Parcelable

data class WachlistData(
    val firstName: String,
    val lastName: String,
    val nse: String,
    val marketId: String,
    val type: String,
    val marketId2: String,
    val lot: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(nse)
        parcel.writeString(marketId)
        parcel.writeString(type)
        parcel.writeString(marketId2)
        parcel.writeString(lot)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WachlistData> {
        override fun createFromParcel(parcel: Parcel): WachlistData {
            return WachlistData(parcel)
        }

        override fun newArray(size: Int): Array<WachlistData?> {
            return arrayOfNulls(size)
        }
    }
}
