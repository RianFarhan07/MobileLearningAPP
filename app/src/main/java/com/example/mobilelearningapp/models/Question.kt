package com.example.mobilelearningapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Question(
    var id: Int = 0,
    var question: String = "",
    var image: String = "",
    var optionOne: String = "",
    var optionTwo: String = "",
    var optionThree: String = "",
    var optionFour: String = "",
    val correctAnswer : Int = -1,
    var selectedAnswer: Int = -1,
//    var jawab :  ArrayList<JawabanKuis> = ArrayList(),
) : Parcelable
