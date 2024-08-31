package com.example.mobilelearningapp.activities

import JawabKuisItemsAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityQuizJawabBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.JawabanKuis
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Kuis
import com.example.mobilelearningapp.models.Question
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class QuizJawabActivity : AppCompatActivity() {

    private var binding: ActivityQuizJawabBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var quizAdapter: JawabKuisItemsAdapter
    private lateinit var kuis: Kuis

    private lateinit var mKelasDetails: Kelas
    lateinit var mKelasDocumentId: String
    private var mMateriListPosition = -1
    private var mQuizListPosition = -1
    private var isUpdate = false

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityQuizJawabBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        recyclerView = findViewById(R.id.rv_kuis_jawab)

        getIntentData()
        kuis = mKelasDetails.materiList[mMateriListPosition].kuis[mQuizListPosition]
        setupRecyclerView()
        setupSubmitButton()
        setupActionBar()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarJawabanListActivity)
        val toolbar = supportActionBar
        if (toolbar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Quiz ${kuis.namaKuis}"
        }
        binding?.toolbarJawabanListActivity?.setNavigationOnClickListener {
            onBackPressed()
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
        if (intent.hasExtra(Constants.IS_UPDATE)) {
            isUpdate = intent.getBooleanExtra(Constants.IS_UPDATE, false)
        }
    }

    private fun setupRecyclerView() {
        quizAdapter = JawabKuisItemsAdapter(kuis.question) { questionId, selectedAnswer ->
//            updateAnswer(questionId, selectedAnswer)
            val question = kuis.question.find { it.id == questionId }
            question?.selectedAnswer = selectedAnswer
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = quizAdapter
    }

    private fun updateAnswer(questionId: Int, selectedAnswer: Int) {
        val question = kuis.question.find { it.id == questionId }
        question?.let {
            it.selectedAnswer = selectedAnswer
        }
    }

    private fun setupSubmitButton() {
        binding?.btnSubmitKuis?.setOnClickListener {
            submitQuiz()
        }
    }

    private fun submitQuiz() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

//        if (kuis.question.any { it.selectedAnswer == -1 }) {
//            Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show()
//            return
//        }

        if (kuis.question.any { it.selectedAnswer == -1 }) {
            // Log the state of each question
            kuis.question.forEachIndexed { index, question ->
                Log.d("QuizSubmit", "Question $index: selectedAnswer = ${question.selectedAnswer}")
            }
            Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show()
            return
        }

        var correctAnswers = 0

        for (question in kuis.question) {
            if (question.selectedAnswer == question.correctAnswer) {
                correctAnswers++
            }
        }

        val score = (correctAnswers.toFloat() / kuis.question.size) * 100

        val quizResult = JawabanKuis(
            id = UUID.randomUUID().toString(),
            createdBy = FirestoreClass().getCurrentUserID(),
            nilai = score.toString()
        )

        kuis.jawab.add(quizResult)

        firestore.collection("kelas").document(mKelasDocumentId)
            .update("materiList.${mMateriListPosition}.kuis.${mQuizListPosition}", kuis)
            .addOnSuccessListener {
                Toast.makeText(this, "Quiz submitted successfully. Your score: $score%", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to submit quiz: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}