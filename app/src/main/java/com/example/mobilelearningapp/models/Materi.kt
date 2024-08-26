package com.example.mobilelearningapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*

@Parcelize
data class Materi(
    var id: String = "",
    var nama: String = "",
    var createdBy: String = "",
    var desc : String = "",
    var file : @RawValue ArrayList<MateriFile> = ArrayList(),
    var image: String = "",
    var url: String = "",
    var fileType: String = ""
) : Parcelable