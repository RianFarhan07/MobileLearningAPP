package com.example.mobilelearningapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
data class JawabanTugas(
    var id: String = "",
    var createdBy: String = "",
    var namaPenjawab : String = "",
    var namaTugas : String = "",
    var jawaban : String = "",
    var imageJawaban: String = "",
    val pdfUrl: String = "",
    val pdfUrlName: String = "",
    var uploadedDate: Long = 0,
    val assignedTo: ArrayList<String> = ArrayList(),
    var nilai: String = "",

//    var hasil : ArrayList<HasilTugas> = ArrayList(),
) : Parcelable