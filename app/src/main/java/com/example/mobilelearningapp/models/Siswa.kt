package com.example.mobilelearningapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Siswa(
        val id: String = "",
        val name: String = "",
        val email: String = "",
        val classes: String = "",
        val image: String = "",
//      SIMPAN TODO TUGAS YANG SUDAH DIBUAT
        val role : String = "siswa"
) : Parcelable