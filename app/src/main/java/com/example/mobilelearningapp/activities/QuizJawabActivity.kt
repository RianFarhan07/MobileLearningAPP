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
import com.example.mobilelearningapp.adapters.QuestionItemsAdapter
import com.example.mobilelearningapp.databinding.ActivityQuizJawabBinding
import com.example.mobilelearningapp.models.JawabanKuis
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Kuis
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class QuizJawabActivity : AppCompatActivity() {

    private var binding : ActivityQuizJawabBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var quizAdapter: JawabKuisItemsAdapter
    private lateinit var submitButton: Button
    private lateinit var kuis: Kuis

    private lateinit var mKelasDetails : Kelas
    lateinit var mKelasDocumentId : String
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
        submitButton = findViewById(R.id.btn_Submit_kuis)

        getIntentData()
        kuis = mKelasDetails.materiList[mMateriListPosition].kuis[mQuizListPosition]
        setupRecyclerView()
        setupSubmitButton()
        setupActionBar()


    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarJawabanListActivity)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            supportActionBar?.title = "Quiz ${mKelasDetails.materiList[mMateriListPosition].kuis[mQuizListPosition].namaKuis}"

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
            Log.e("MATERI_ITEM_POSITION", mMateriListPosition.toString())
        }
        if (intent.hasExtra(Constants.QUIZ_LIST_ITEM_POSITION)) {
            mQuizListPosition = intent.getIntExtra(Constants.QUIZ_LIST_ITEM_POSITION, -1)
            Log.e("QUIZ_ITEM_POSITION", mQuizListPosition.toString())
        }
        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mKelasDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
            Log.e("document", "document $mKelasDocumentId")
        }
        if (intent.hasExtra(Constants.IS_UPDATE)) {
            isUpdate = intent.getBooleanExtra(Constants.IS_UPDATE, false)
            Log.e("TOCOURSE ", isUpdate.toString())
        }
    }

    private fun setupRecyclerView() {
        quizAdapter = JawabKuisItemsAdapter(kuis.question) { questionId, selectedAnswer ->
            // This lambda is called when an answer is selected
            // You can use this to update UI or perform any other action
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = quizAdapter
    }

    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            submitQuiz()
        }
    }

    private fun submitQuiz() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val answers = quizAdapter.getAnswers()

        if (answers.size != kuis.question.size) {
            Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show()
            return
        }

        val quizResult = hashMapOf(
            "userId" to userId,
            "quizId" to kuis.id,
            "answers" to answers
        )

        firestore.collection("quizResults").add(quizResult)
            .addOnSuccessListener {
                Toast.makeText(this, "Quiz submitted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to submit quiz: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}