package com.example.mobilelearningapp.models

import java.util.*

data class MateriFile(
    var id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val url: String = "",
    val fileType: String = ""
)
