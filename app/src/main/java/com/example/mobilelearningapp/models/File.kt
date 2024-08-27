package com.example.mobilelearningapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class File(
    var id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val url: String = "",
    val fileType: String = ""
) :Parcelable
