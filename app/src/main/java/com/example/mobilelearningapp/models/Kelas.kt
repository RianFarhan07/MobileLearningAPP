package com.example.mobilelearningapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Kelas (
    val nama: String ="",
    val createdBy: String? = "",
    var materiList : ArrayList<Materi> = ArrayList(),
    var documentId: String? = "",
) :Parcelable