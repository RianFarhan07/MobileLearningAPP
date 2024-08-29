package com.example.mobilelearningapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilelearningapp.adapters.QuestionItemsAdapter
import com.example.mobilelearningapp.databinding.ActivityCreateQuizBinding
import com.example.mobilelearningapp.models.Kuis
import com.example.mobilelearningapp.models.Question
import com.example.mobilelearningapp.utils.Constants
import kotlinx.android.synthetic.main.activity_create_quiz.*
import java.util.*
import kotlin.collections.ArrayList

class CreateQuizActivity : AppCompatActivity() {

    private var binding : ActivityCreateQuizBinding? = null
    private val questions = ArrayList<Question>()
    private lateinit var questionAdapter: QuestionItemsAdapter

    companion object {
        private const val REQUEST_CREATE_QUESTION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateQuizBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        setupListeners()
        setupActionBar()
        setupRecyclerView()
    }

//    private fun setupRecyclerView() {
//        questionAdapter = QuestionAdapter(questions)
//        rvQuestions.apply {
//            layoutManager = LinearLayoutManager(this@CreateQuizActivity)
//            adapter = questionAdapter
//        }
//    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarCreateQuiz)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)


        }
        binding?.toolbarCreateQuiz?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        questionAdapter = QuestionItemsAdapter(questions) { question ->
            // Handle click on existing question if needed
        }
        rvQuestions.apply {
            layoutManager = LinearLayoutManager(this@CreateQuizActivity)
            adapter = questionAdapter
        }
    }

    private fun setupListeners() {
        binding?.btnTambahPertanyaan?.setOnClickListener {
            val intent = Intent(this, CreateQuestionActivity::class.java)
            intent.putExtra(Constants.QUESTION_SIZE,questions.size)
            startActivityForResult(intent, REQUEST_CREATE_QUESTION)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_QUESTION && resultCode == RESULT_OK) {
            data?.getParcelableExtra<Question>("question")?.let { newQuestion ->
                questions.add(newQuestion)
                questionAdapter.notifyItemInserted(questions.size - 1)
            }
        }
    }
}