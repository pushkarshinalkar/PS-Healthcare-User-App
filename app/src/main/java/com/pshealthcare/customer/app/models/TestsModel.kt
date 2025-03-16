package com.pshealthcare.customer.app.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class TestsModel(
    var name: String = "",
    var price: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(price)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TestsModel> {
        override fun createFromParcel(parcel: Parcel): TestsModel {
            return TestsModel(parcel)
        }

        override fun newArray(size: Int): Array<TestsModel?> {
            return arrayOfNulls(size)
        }

        fun List<TestsModel>.toJson(): String {
            return Gson().toJson(this)
        }

        fun String.toTestsModelList(): List<TestsModel> {
            val listType = object : TypeToken<List<TestsModel>>() {}.type
            return Gson().fromJson(this, listType)
        }
    }
}
