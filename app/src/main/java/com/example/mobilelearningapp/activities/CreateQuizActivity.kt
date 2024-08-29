package com.example.mobilelearningapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mobilelearningapp.databinding.ActivityCreateQuizBinding
import com.example.mobilelearningapp.models.Kuis
import com.example.mobilelearningapp.models.Question
import java.util.*
import kotlin.collections.ArrayList

class CreateQuizActivity : AppCompatActivity() {

    private var binding : ActivityCreateQuizBinding? = null
    private val questions = ArrayList<Question>()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateQuizBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)
    }

//    private fun setupRecyclerView() {
//        questionAdapter = QuestionAdapter(questions)
//        rvQuestions.apply {
//            layoutManager = LinearLayoutManager(this@CreateQuizActivity)
//            adapter = questionAdapter
//        }
//    }

    private fun setupListeners() {
        binding?.btnTambahPertanyaan?.setOnClickListener {
            addNewQuestion()
        }

        binding?.btnSimpanKuis?.setOnClickListener {
            saveKuis()
        }
    }

//    private fun addNewQuestion() {
//        val newQuestion = Question(
//            id = questions.size + 1,
//            question = "",
//            image = 0,
//            optionOne = "",
//            optionTwo = "",
//            optionThree = "",
//            optionFour = "",
//            correctAnswer = 0
//        )
//        questions.add(newQuestion)
//        questionAdapter.notifyItemInserted(questions.size - 1)
//    }

    private fun saveKuis() {
        val namaKuis = binding?.etNamaKuis?.text.toString()
        val deskripsi = binding?.etDeskripsi?.text.toString()
        val dueDateString = binding?.etDueDate?.text.toString()

        if (namaKuis.isEmpty() || deskripsi.isEmpty() || dueDateString.isEmpty() || questions.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert dueDate string to Long timestamp
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, dueDateString.substring(0, 4).toInt())
            set(Calendar.MONTH, dueDateString.substring(5, 7).toInt() - 1)
            set(Calendar.DAY_OF_MONTH, dueDateString.substring(8, 10).toInt())
        }.timeInMillis

        val kuis = Kuis(
            id = UUID.randomUUID().toString(),
            namaKuis = namaKuis,
            createdBy = "current_user_id", // Replace with actual user ID
            desc = deskripsi,
            dueDate = dueDate,
            question = ArrayList(questions)
        )

        // TODO: Save kuis to database or send to server

        Toast.makeText(this, "Kuis berhasil disimpan", Toast.LENGTH_SHORT).show()
        finish()
    }
}