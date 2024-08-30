package com.example.mobilelearningapp.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilelearningapp.adapters.QuestionItemsAdapter
import com.example.mobilelearningapp.databinding.ActivityCreateQuizBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Kuis
import com.example.mobilelearningapp.models.Question
import com.example.mobilelearningapp.utils.Constants
import kotlinx.android.synthetic.main.activity_create_quiz.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CreateQuizActivity : AppCompatActivity() {

    private var binding : ActivityCreateQuizBinding? = null
    private val questions = ArrayList<Question>()
    private lateinit var questionAdapter: QuestionItemsAdapter
    private lateinit var mKelasDetails : Kelas
    lateinit var mKelasDocumentId : String
    private var mMateriListPosition = -1
    private var mQuizListPosition = -1
    private var isUpdate = false

    private var mSelectedDueDateMilliSeconds : Long = 0
    private var mSelectedImageFileUri : Uri? = null
    private var mMateriImageURL: String = ""

    companion object {
        private const val REQUEST_CREATE_QUESTION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateQuizBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        getIntentData()
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

        binding?.ivCalendarIcon?.setOnClickListener {
            showDatePicker()
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
            Log.e("TUGAS_ITEM_POSITION", mQuizListPosition.toString())
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
        val dueDateString = binding?.tvDueDate?.text.toString()

        if (namaKuis.isEmpty() || deskripsi.isEmpty() || dueDateString.isEmpty() || questions.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi semua field", Toast.LENGTH_SHORT).show()
            return
        }


        val kuis = Kuis(
            id = UUID.randomUUID().toString(),
            namaKuis = namaKuis,
            createdBy = FirestoreClass().getCurrentUserID(),
            desc = deskripsi,
            dueDate = mSelectedDueDateMilliSeconds,
            question = ArrayList(questions)
        )

        mKelasDetails.materiList[mMateriListPosition].kuis.add(kuis)
        FirestoreClass().addUpdateMateriList(this, mKelasDetails)

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

    private fun showDatePicker(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if (dayOfMonth < 0) "0$dayOfMonth" else "$dayOfMonth"
                val sMonthOfYear = if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                binding?.tvDueDate?.text = selectedDate

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)

                mSelectedDueDateMilliSeconds = theDate!!.time


            }       ,
            year,
            month,
            day
        )
        dpd.show()
    }

    fun addUpdateMateriListSuccess(){
        setResult(RESULT_OK)
        Toast.makeText(this, " kuis berhasil ditambah", Toast.LENGTH_SHORT).show()
        finish()

    }
}