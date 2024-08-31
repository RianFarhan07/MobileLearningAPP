package com.example.mobilelearningapp.activities

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
import com.example.mobilelearningapp.models.JawabanKuis
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Kuis
import com.example.mobilelearningapp.models.Question
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class QuizJawabActivity : AppCompatActivity(), View.OnClickListener {

    private var binding: ActivityQuizJawabBinding? = null
    private lateinit var kuis: Kuis
    private lateinit var mKelasDetails: Kelas
    private lateinit var mKelasDocumentId: String
    private var mMateriListPosition = -1
    private var mQuizListPosition = -1
    private var isUpdate = false

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var mCurrentPosition: Int = 1
    private var mSelectedOptionPosition: Int = 0
    private var mCorrectAnswers: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizJawabBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        getIntentData()
        kuis = mKelasDetails.materiList[mMateriListPosition].kuis[mQuizListPosition]
        setQuestion()

        binding?.tvOptionOne?.setOnClickListener(this)
        binding?.tvOptionTwo?.setOnClickListener(this)
        binding?.tvOptionThree?.setOnClickListener(this)
        binding?.tvOptionFour?.setOnClickListener(this)
        binding?.btnSubmitKuis?.setOnClickListener(this)
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

    private fun setQuestion() {
        defaultOptionView()

        val question: Question = kuis.question[mCurrentPosition - 1]

        binding?.progressBar?.progress = mCurrentPosition
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
            binding?.btnSubmitKuis?.text = "FINISH"
        } else {
            binding?.btnSubmitKuis?.text = "SUBMIT"
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
//            option.setTextColor(resources.getColor(R.color.text_secondary))
        }
    }

    private fun selectedOptionView(view: View, selectedOptionNum: Int) {
        defaultOptionView()
        mSelectedOptionPosition = selectedOptionNum
        view.setBackgroundResource(R.drawable.selected_option_border_bg)
//        view.setTextColor(resources.getColor(R.color.text_primary))
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tv_option_one -> selectedOptionView(binding?.tvOptionOne!!, 1)
            R.id.tv_option_two -> selectedOptionView(binding?.tvOptionTwo!!, 2)
            R.id.tv_option_three -> selectedOptionView(binding?.tvOptionThree!!, 3)
            R.id.tv_option_four -> selectedOptionView(binding?.tvOptionFour!!, 4)
            R.id.btn_Submit_kuis
            -> {
                if (mSelectedOptionPosition == 0) {
                    mCurrentPosition++
                    when {
                        mCurrentPosition <= kuis.question.size -> setQuestion()
                        else -> submitQuiz()
                    }
                } else {
                    val question = kuis.question[mCurrentPosition - 1]
                    if (question.correctAnswer != mSelectedOptionPosition) {
                        answerView(mSelectedOptionPosition, R.drawable.wrong_option_border_bg)
                    } else {
                        mCorrectAnswers++
                    }
                    answerView(question.correctAnswer, R.drawable.correct_option_border_bg)

                    if (mCurrentPosition == kuis.question.size) {
                        binding?.btnSubmitKuis?.text = "FINISH"
                    } else {
                        binding?.btnSubmitKuis?.text = "GO TO NEXT QUESTION"
                    }
                    question.selectedAnswer = mSelectedOptionPosition
                    mSelectedOptionPosition = 0
                }
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

    private fun submitQuiz() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val score = (mCorrectAnswers.toFloat() / kuis.question.size) * 100

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