    package com.example.mobilelearningapp.models

    import android.os.Parcelable
    import kotlinx.android.parcel.Parcelize

    @Parcelize
    data class JawabanKuis(
        var id: String = "",
        var createdBy: String = "",
        var namaPenjawab: String = "",
        var namaMateri: String = "",
        var namaKelas: String = "",
        var namaKuis : String = "",
        var nilai: String = "",
    ) : Parcelable