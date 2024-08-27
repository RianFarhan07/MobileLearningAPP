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
    var namaTugas : String = "",
    var jawaban : ArrayList<File> = ArrayList(),
    var imageJawaban: String = "",
    var fileJawaban: String = "",
    var uploadedDate: Long = 0,
    var nilai: String = "",

//    var hasil : ArrayList<HasilTugas> = ArrayList(),
) : Parcelable