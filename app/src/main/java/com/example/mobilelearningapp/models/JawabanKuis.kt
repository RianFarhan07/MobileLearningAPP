    package com.example.mobilelearningapp.models

    import android.os.Parcelable
    import kotlinx.android.parcel.Parcelize

    @Parcelize
    data class JawabanKuis(
        var id: String = "",
        var createdBy: String = "",
    //    val selectedAnswer: Int,
        var nilai: String = "",
    ) : Parcelable