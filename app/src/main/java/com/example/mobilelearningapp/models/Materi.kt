package com.example.mobilelearningapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlin.collections.ArrayList

@Parcelize
data class Materi(
    var id: String = "",
    var nama: String = "",
    var createdBy: String = "",
    var desc : String = "",
    var file : ArrayList<File> = ArrayList(),
    var image: String = "",
    var video: String = "",
    var tugas : ArrayList<Tugas> = ArrayList(),
    var kuis : ArrayList<Kuis> = ArrayList()
) : Parcelable