package com.example.mobilelearningapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlin.collections.ArrayList

@Parcelize
data class Kuis(
    var id: String = "",
    var namaKuis: String = "",
    var namaMateri : String = "",
    var namaKelas : String = "",
    var namaMataPelajaran : String = "",
    var createdBy: String = "",
    var desc : String = "",
    val dueDate: Long = 0,
    var question : ArrayList<Question> = ArrayList(),
    var jawab :  ArrayList<JawabanKuis> = ArrayList()
) : Parcelable