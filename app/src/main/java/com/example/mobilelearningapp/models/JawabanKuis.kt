package com.example.mobilelearningapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class JawabanKuis(
    val questionId: Int,
    val selectedAnswer: Int
) : Parcelable