package com.example.mobilelearningapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mobilelearningapp.databinding.ActivityResultKuisBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ResultKuisActivity : AppCompatActivity() {

    private var binding : ActivityResultKuisBinding? = null
    private lateinit var mKelasDetails: Kelas
    private lateinit var mKelasDocumentId: String
    private var mMateriListPosition = -1
    private var mQuizListPosition = -1
    private var isUpdate = false
    private lateinit var mUsername : String
    private lateinit var mScore : String
    private var mCorrectAnswers: Int = 0
    private var mTotalQuestion: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityResultKuisBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)
        getIntentData()

        if (isUpdate){
            val currentUserId = FirestoreClass().getCurrentUserID()
            val currentKuis = mKelasDetails.materiList[mMateriListPosition].kuis[mQuizListPosition]
            val userJawaban = currentKuis.jawab.find { it.createdBy == currentUserId }
            binding?.tvScore?.text = "Nilai anda  ${userJawaban?.nilai.toString()}"
            binding?.tvName?.text = userJawaban?.namaPenjawab
        }else{
            binding?.tvScore?.text = "Nilai Kamu  $mScore ($mCorrectAnswers benar dari $mTotalQuestion soal)"
            binding?.tvName?.text = mUsername
        }

        binding?.btnFinish?.setOnClickListener {
            Finish()
        }

    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.KELAS_DETAIL)) {
            mKelasDetails = intent.getParcelableExtra(Constants.KELAS_DETAIL)!!
        }
        if (intent.hasExtra(Constants.MATERI_LIST_ITEM_POSITION)) {
            mMateriListPosition = intent.getIntExtra(Constants.MATERI_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.QUIZ_LIST_ITEM_POSITION)) {
            mQuizListPosition = intent.getIntExtra(Constants.QUIZ_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mKelasDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }
        if (intent.hasExtra(Constants.USER_NAME)) {
            mUsername = intent.getStringExtra(Constants.USER_NAME).toString()
        }
        if (intent.hasExtra(Constants.CORRECT_ANSWER)) {
            mCorrectAnswers = intent.getIntExtra(Constants.CORRECT_ANSWER,0)
        }
        if (intent.hasExtra(Constants.TOTAL_QUESTION)) {
            mTotalQuestion = intent.getIntExtra(Constants.TOTAL_QUESTION, 0)
        }
        if (intent.hasExtra(Constants.SCORE)) {
            mScore = intent.getStringExtra(Constants.SCORE).toString()
        }
        if (intent.hasExtra(Constants.IS_UPDATE)) {
            isUpdate = intent.getBooleanExtra(Constants.IS_UPDATE, false)
        }
    }

    fun Finish() {
        setResult(RESULT_OK)
        Toast.makeText(this, " kuis berhasil ditambah", Toast.LENGTH_SHORT).show()
        finish()
    }
}