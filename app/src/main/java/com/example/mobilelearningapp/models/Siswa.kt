package com.example.mobilelearningapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Siswa(
        val id: String = "",
        val Name: String = "",
        val email: String = "",
        val classes: String = "",
        val image: String = "",
        val role : String = "siswa"
) : Parcelable