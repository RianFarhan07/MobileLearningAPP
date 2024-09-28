package com.example.mobilelearningapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityQuizJawabBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.*
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class QuizJawabActivity : AppCompatActivity(), View.OnClickListener {

    private var binding: ActivityQuizJawabBinding? = null
    private lateinit var kuis: Kuis
    private lateinit var mMateriDetails: Materi
    private lateinit var mKelasDocumentId: String
    private var mQuizListPosition = -1
    private var isUpdate = false
    private lateinit var mUsername : String
    private lateinit var mScore : String
    private var isQuizFinished = false

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var mCurrentPosition: Int = 1
    private var mSelectedOptionPosition: Int = 0
    private var mCorrectAnswers: Int = 0
    private var isAnswerSubmitted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizJawabBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        getIntentData()
        isQuizFinished = false
        kuis = mMateriDetails.kuis[mQuizListPosition]
        setQuestion()

        binding?.tvOptionOne?.setOnClickListener(this)
        binding?.tvOptionTwo?.setOnClickListener(this)
        binding?.tvOptionThree?.setOnClickListener(this)
        binding?.tvOptionFour?.setOnClickListener(this)
        binding?.btnSubmitKuis?.setOnClickListener(this)
    }

    override fun onBackPressed() {
        if (isQuizFinished) {
            super.onBackPressed()
        } else {
            Toast.makeText(this, "Anda harus menyelesaikan kuis terlebih dahulu!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.MATERI_DETAIL)) {
            mMateriDetails = intent.getParcelableExtra(Constants.MATERI_DETAIL)!!
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
        FirestoreClass().getUsername { username ->
            if (username != null) {
                mUsername = username
            } else {
                println("Failed to retrieve username")
            }
        }
    }

    private fun setQuestion() {
        defaultOptionView()
        isAnswerSubmitted = false

        val question: Question = kuis.question[mCurrentPosition - 1]

        val progress = (mCurrentPosition * 100 / kuis.question.size)
        binding?.progressBar?.progress = progress
        binding?.tvProgress?.text = "$mCurrentPosition/${kuis.question.size}"

        binding?.tvQuestion?.text = question.question
        if (question.image.isNotEmpty()) {
            Glide.with(this).load(question.image).into(binding?.ivImage!!)
            binding?.ivImage?.visibility = View.VISIBLE
        } else {
            binding?.ivImage?.visibility = View.GONE
        }
        binding?.tvOptionOne?.text = question.optionOne
        binding?.tvOptionTwo?.text = question.optionTwo
        binding?.tvOptionThree?.text = question.optionThree
        binding?.tvOptionFour?.text = question.optionFour

        if (mCurrentPosition == kuis.question.size) {
            binding?.btnSubmitKuis?.text = "SELESAI"
        } else {
            binding?.btnSubmitKuis?.text = "MASUKKAN JAWABAN"
        }
    }

    private fun defaultOptionView() {
        val options = ArrayList<View>()
        binding?.tvOptionOne?.let { options.add(it) }
        binding?.tvOptionTwo?.let { options.add(it) }
        binding?.tvOptionThree?.let { options.add(it) }
        binding?.tvOptionFour?.let { options.add(it) }

        for (option in options) {
            option.setBackgroundResource(R.drawable.default_option_border_bg)
            option.isClickable = true
        }
    }

    private fun selectedOptionView(view: View, selectedOptionNum: Int) {
        if (!isAnswerSubmitted) {
            defaultOptionView()
            mSelectedOptionPosition = selectedOptionNum
            view.setBackgroundResource(R.drawable.selected_option_border_bg)
        }
    }

    override fun onClick(view: View?) {
        if (!isAnswerSubmitted) {
            when (view?.id) {
                R.id.tv_option_one -> selectedOptionView(binding?.tvOptionOne!!, 1)
                R.id.tv_option_two -> selectedOptionView(binding?.tvOptionTwo!!, 2)
                R.id.tv_option_three -> selectedOptionView(binding?.tvOptionThree!!, 3)
                R.id.tv_option_four -> selectedOptionView(binding?.tvOptionFour!!, 4)
                R.id.btn_Submit_kuis -> {
                    if (mSelectedOptionPosition == 0) {
                        Toast.makeText(this, "Tolong Masukkan Jawaban", Toast.LENGTH_SHORT).show()
                    } else {
                        checkAnswer()
                    }
                }
            }
        } else if (view?.id == R.id.btn_Submit_kuis) {
            mCurrentPosition++
            when {
                mCurrentPosition <= kuis.question.size -> setQuestion()
                else -> submitQuiz()
            }
        }
    }

    private fun answerView(answer: Int, drawableView: Int) {
        when (answer) {
            1 -> binding?.tvOptionOne?.background = ContextCompat.getDrawable(this, drawableView)
            2 -> binding?.tvOptionTwo?.background = ContextCompat.getDrawable(this, drawableView)
            3 -> binding?.tvOptionThree?.background = ContextCompat.getDrawable(this, drawableView)
            4 -> binding?.tvOptionFour?.background = ContextCompat.getDrawable(this, drawableView)
        }
    }

    private fun checkAnswer() {
        isAnswerSubmitted = true
        val question = kuis.question[mCurrentPosition - 1]
        if (question.correctAnswer != mSelectedOptionPosition) {
            answerView(mSelectedOptionPosition, R.drawable.wrong_option_border_bg)
        } else {
            mCorrectAnswers++
        }
        answerView(question.correctAnswer, R.drawable.correct_option_border_bg)

        if (mCurrentPosition == kuis.question.size) {
            binding?.btnSubmitKuis?.text = "SELESAI"
        } else {
            binding?.btnSubmitKuis?.text = "PERTANYAAN BERIKUTNYA"
        }
        question.selectedAnswer = mSelectedOptionPosition

        // Disable clicking on options after submitting
        binding?.tvOptionOne?.isClickable = false
        binding?.tvOptionTwo?.isClickable = false
        binding?.tvOptionThree?.isClickable = false
        binding?.tvOptionFour?.isClickable = false
    }

    private fun submitQuiz() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val score = (mCorrectAnswers.toFloat() / kuis.question.size) * 100
        mScore = score.toString()



        val quizResult = JawabanKuis(
            id = UUID.randomUUID().toString(),
            createdBy = FirestoreClass().getCurrentUserID(),
            nilai = score.toString(),
            namaPenjawab = mUsername,
            namaKuis = mMateriDetails.kuis[mQuizListPosition].namaKuis,
            namaKelas = mMateriDetails.kelas,
            namaMateri = mMateriDetails.nama
        )

        kuis.jawab.add(quizResult)

        FirestoreClass().updateSingleMateriInKelas(this, mKelasDocumentId,mMateriDetails)
    }

    fun addUpdateMateriListSuccess(){
        setResult(RESULT_OK)
        Toast.makeText(this, " kuis berhasil ditambah", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ResultKuisActivity::class.java)
        isQuizFinished = true
        intent.putExtra(Constants.USER_NAME, mUsername)
        intent.putExtra(Constants.CORRECT_ANSWER, mCorrectAnswers)
        intent.putExtra(Constants.TOTAL_QUESTION, kuis.question.size)
        intent.putExtra(Constants.SCORE, mScore)
        startActivity(intent)
        finish()

    }
}