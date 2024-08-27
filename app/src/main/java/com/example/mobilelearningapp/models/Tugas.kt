package com.example.mobilelearningapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
data class Tugas(
    var id: String = "",
    var namaTugas : String = "",
    var createdBy: String = "",
    var soal : String = "",
    var imageSoal: String = "",
    val pdfUrl: String = "",
    val pdfUrlName: String = "",
    val dueDate: Long = 0,
    var jawab :  ArrayList<JawabanTugas> = ArrayList(),

//    var jawaban : ArrayList<File> = ArrayList(),
//    var imageJawaban: String = "",
//    var fileJawaban: String = "",
//    var nilai: String = ""
//    var hasil : ArrayList<HasilTugas> = ArrayList(),
) : Parcelable