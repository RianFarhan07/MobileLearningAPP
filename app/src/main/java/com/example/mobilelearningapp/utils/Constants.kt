package com.example.mobilelearningapp.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {

    const val SISWA: String = "siswa"
    const val GURU: String = "guru"
    const val KELAS: String = "kelas"
    const val MATERI: String = "materi"
    const val TUGAS: String = "tugas"
    const val JAWABAN_TUGAS: String = "jawabanTugas"
    const val KUIS: String = "kuis"
    const val MATERIFILE: String = "materiFile"



    const val IMAGE : String = "image"
    const val NAME : String = "name"
    const val EMAIL : String = "email"
    const val CLASSES : String = "classes"
    const val ASSIGNED_TO : String = "assignedTo"

    const val DOCUMENT_ID : String = "documentId"
    const val KELAS_DETAIL : String = "kelas_detail"
    const val MATERI_LIST : String = "materiList"

    const val IS_UPDATE : String = "isUpdate"
    const val QUESTION_SIZE : String = "questionSize"

    const val MATERI_LIST_ITEM_POSITION: String = "topic_list_item_position"
    const val TUGAS_LIST_ITEM_POSITION: String = "tugas_list_item_position"
    const val JAWAB_LIST_ITEM_POSITION: String = "jawab_list_item_position"
    const val QUIZ_LIST_ITEM_POSITION: String = "quiz_list_item_position"

    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val PICK_FILE_REQUEST_CODE = 3

    const val USER_NAME: String = "user_name"
    const val TOTAL_QUESTION: String = "total_question"
    const val CORRECT_ANSWER: String = "correct_answer"
    const val SCORE: String = "score"


    fun showImageChooser(activity : Activity){
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        activity.startActivityForResult(galleryIntent,PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri): String?{
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri))
    }
}