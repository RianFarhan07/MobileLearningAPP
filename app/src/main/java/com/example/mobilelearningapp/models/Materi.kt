package com.example.mobilelearningapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Materi(
    var id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val topic: String = "",
    val desc : String = "",
//    val kuis
    val url: String = "",
    val fileType: String = ""

    ) : Parcelable