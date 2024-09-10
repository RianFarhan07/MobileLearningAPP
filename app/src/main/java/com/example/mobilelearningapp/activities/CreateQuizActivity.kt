package com.example.mobilelearningapp.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.adapters.QuestionItemsAdapter
import com.example.mobilelearningapp.databinding.ActivityCreateQuizBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.*
import com.example.mobilelearningapp.utils.Constants
import com.example.mobilelearningapp.utils.SwipeToDeleteCallback
import kotlinx.android.synthetic.main.activity_create_quiz.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CreateQuizActivity : BaseActivity() {

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

    private lateinit var mQuestionList: ArrayList<Question>

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

        if (isUpdate){
            showProgressDialog(resources.getString(R.string.mohon_tunggu))
            setUpDataQuiz()
        }
    }

    private fun setUpDataQuiz() {

        val currentKuis = mKelasDetails.materiList[mMateriListPosition].kuis[mQuizListPosition]
        populateQuestionListToUI(currentKuis.question)
        setupRecyclerView()

        binding?.etNamaKuis?.setText(currentKuis.namaKuis)
        binding?.etDeskripsi?.setText(currentKuis.desc)

        mSelectedDueDateMilliSeconds = currentKuis.dueDate

        if (mSelectedDueDateMilliSeconds > 0) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            binding?.tvDueDate?.text = selectedDate
        }

        hideProgressDialog()
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
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        if (isUpdate){
            supportActionBar?.title = "Update Quiz ${mKelasDetails.materiList[mMateriListPosition].kuis[mQuizListPosition].namaKuis}"
        }
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
        if (isUpdate){
            binding?.btnSimpanKuis?.text = "Update Kuis"
        }else{
            binding?.btnLiatJawaban?.visibility = View.GONE
            binding?.btnSimpanKuis?.text = "Buat Kuis"
        }

        binding?.btnTambahPertanyaan?.setOnClickListener {
            val intent = Intent(this, CreateQuestionActivity::class.java)
            intent.putExtra(Constants.QUESTION_SIZE,questions.size)
            startActivityForResult(intent, REQUEST_CREATE_QUESTION)
        }

        binding?.btnSimpanKuis?.setOnClickListener {
            if(isUpdate){
                updateKuis()
            }else{
                saveKuis()
            }
        }

        binding?.btnLiatJawaban?.setOnClickListener {
            val intent = Intent(this,JawabanKuisListActivity::class.java)
            intent.putExtra(Constants.MATERI_LIST_ITEM_POSITION,mMateriListPosition)
            intent.putExtra(Constants.QUIZ_LIST_ITEM_POSITION,mQuizListPosition)
            intent.putExtra(Constants.KELAS_DETAIL,mKelasDetails)
            intent.putExtra(Constants.DOCUMENT_ID, mKelasDocumentId)
            startActivity(intent)
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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card -> {
                val currentUserID = FirestoreClass().getCurrentUserID()
                if (currentUserID.isNotEmpty()) {
                    FirestoreClass().getUserRole(currentUserID) { role ->
                        if (role == "siswa") {
                            sequenceOf(
                                Toast.makeText(this@CreateQuizActivity,
                                    "siswa tidak bisa menghapus tugas",
                                    Toast.LENGTH_LONG
                                ).show()
                            )
                        }else{
                            if (isUpdate){
                                showAlertDialogToDeleteKuis(
                                    mKelasDetails.materiList[mMateriListPosition].kuis[mQuizListPosition].namaKuis!!)
                            }else{
                                Toast.makeText(this@CreateQuizActivity,
                                "belum ada kuis",Toast.LENGTH_LONG
                                ).show()
                            }

                        }

                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

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
            namaKelas = mKelasDetails.nama,
            namaMataPelajaran = mKelasDetails.course,
            namaMateri = mKelasDetails.materiList[mMateriListPosition].nama,
            createdBy = FirestoreClass().getCurrentUserID(),
            desc = deskripsi,
            dueDate = mSelectedDueDateMilliSeconds,
            question = ArrayList(questions)
        )

        mKelasDetails.materiList[mMateriListPosition].kuis.add(kuis)
        FirestoreClass().addUpdateMateriList(this, mKelasDetails)

        Toast.makeText(this, "Kuis berhasil disimpan", Toast.LENGTH_SHORT).show()
//        finish()
    }

    private fun updateQuestionList() {
        // Refresh the RecyclerView
        questionAdapter.notifyDataSetChanged()

        // Update any other UI elements that show the question count
        // For example, if you have a TextView showing the number of questions:
        // binding?.tvQuestionCount?.text = "Total Questions: ${questions.size}"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_QUESTION && resultCode == RESULT_OK) {
            data?.getParcelableExtra<Question>("question")?.let { newQuestion ->
                questions.add(newQuestion)
                questionAdapter.notifyItemInserted(questions.size - 1)

                // Update the UI regardless of whether it's an update or new quiz
                updateQuestionList()


                // If it's an update, also update the mKelasDetails
                if (isUpdate) {
                    mKelasDetails.materiList[mMateriListPosition].kuis[mQuizListPosition].question = ArrayList(questions)
                    populateQuestionListToUI( mKelasDetails.materiList[mMateriListPosition].kuis[mQuizListPosition].question)
                }
            }
        }
    }

    // TODO ini oke diatas juga oke
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_CREATE_QUESTION && resultCode == Activity.RESULT_OK) {
//            val newQuestion = data?.getParcelableExtra<Question>("question")
//            if (newQuestion != null) {
//                questions.add(newQuestion)
//                // Update UI jika perlu, misalnya refresh RecyclerView
//                questionAdapter.notifyDataSetChanged()
//            }
//        }
//    }

    private fun updateKuis() {
        val namaKuis = binding?.etNamaKuis?.text.toString()
        val deskripsi = binding?.etDeskripsi?.text.toString()
        val dueDateString = binding?.tvDueDate?.text.toString()

        if (namaKuis.isEmpty() || deskripsi.isEmpty() || dueDateString.isEmpty() || questions.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        showProgressDialog(resources.getString(R.string.mohon_tunggu))

        // Preserve existing PDF information if not changed
        val currentKuis = mKelasDetails.materiList[mMateriListPosition].kuis[mQuizListPosition]

        val updatedKuis = Kuis(
            id = currentKuis.id,
            namaKuis = namaKuis,
            namaMateri = currentKuis.namaMateri,
            namaKelas = currentKuis.namaKelas,
            namaMataPelajaran = currentKuis.namaMataPelajaran,
            desc = deskripsi,
            dueDate = mSelectedDueDateMilliSeconds,
            createdBy = FirestoreClass().getCurrentUserID(),
            question = ArrayList(questions) // Use the current questions list
        )

        // Update the tugas in the mKelasDetails
        mKelasDetails.materiList[mMateriListPosition].kuis[mQuizListPosition] = updatedKuis

        // Update in Firestore
        FirestoreClass().updateKuisInMateri(
            this,
            mKelasDocumentId,
            mMateriListPosition,
            mQuizListPosition,
            updatedKuis
        )

        updateQuestionList()
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

    private fun populateQuestionListToUI(questionList: ArrayList<Question>) {
        questions.clear()
        questions.addAll(questionList)

        val rvQuestionList: RecyclerView = findViewById(R.id.rvQuestions)

        if (questions.isNotEmpty()) {
            rvQuestionList.visibility = View.VISIBLE
            rvQuestionList.layoutManager = LinearLayoutManager(this)
            rvQuestionList.setHasFixedSize(true)

            questionAdapter = QuestionItemsAdapter(questions) { question ->
                // Handle click on existing question if needed
                // For example, you could open an edit question activity:
                // val intent = Intent(this, EditQuestionActivity::class.java)
                // intent.putExtra("QUESTION", question)
                // startActivityForResult(intent, REQUEST_EDIT_QUESTION)
            }
            rvQuestionList.adapter = questionAdapter

            val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val questionToDelete = questions[position]

                    val dialogView = LayoutInflater.from(this@CreateQuizActivity).inflate(R.layout.dialog_confirm_delete, null)
                    val dialog = AlertDialog.Builder(this@CreateQuizActivity)
                        .setView(dialogView)
                        .create()

                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()

                    val tvYa = dialogView.findViewById<TextView>(R.id.tv_ya)
                    val tvTidak = dialogView.findViewById<TextView>(R.id.tv_tidak)

                    tvYa.setOnClickListener {
                        showProgressDialog(resources.getString(R.string.mohon_tunggu))
                        FirestoreClass().deleteQuestion(
                            this@CreateQuizActivity,
                            mKelasDocumentId,
                            mMateriListPosition,
                            mQuizListPosition,
                            questionToDelete.id.toString()
                        )
                        dialog.dismiss()
                    }

                    tvTidak.setOnClickListener {
                        dialog.dismiss()
                        // Restore the item if the user cancels the delete action
                        questionAdapter.notifyItemChanged(position)
                    }
                }
            }

            val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
            deleteItemTouchHelper.attachToRecyclerView(rvQuestionList)

        } else {
            rvQuestionList.visibility = View.GONE
        }
    }

    fun jawabKuisDeleteSuccess() {
        setResult(RESULT_OK)
        hideProgressDialog()
        Toast.makeText(this, "Jawaban tugas berhasil dihapus", Toast.LENGTH_SHORT).show()
        FirestoreClass().getKelasDetails(this, mKelasDocumentId) // Refresh data
    }

    fun kuisUpdateSuccess() {
        hideProgressDialog()
        FirestoreClass().addUpdateMateriList(this, mKelasDetails)
        updateQuestionList() // Update the UI after successful Firestore update
    }

    fun kelasDetails(kelas: Kelas){
        mKelasDetails = kelas

        setupActionBar()
        populateQuestionListToUI(mKelasDetails.materiList[mMateriListPosition].kuis[mQuizListPosition].question)
        hideProgressDialog()
    }

    private fun showAlertDialogToDeleteKuis(kuisName: String) {

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("DELETE")
        builder.setMessage("Apakah anda yakin ingin menghapus kuis $kuisName")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Iya") { dialogInterface, _ ->
            showProgressDialog(resources.getString(R.string.mohon_tunggu))
            deleteKuis()
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("Tidak") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: androidx.appcompat.app.AlertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun deleteKuis() {
        val kuisList: ArrayList<Kuis> = mKelasDetails.materiList[mMateriListPosition].kuis
        kuisList.removeAt(mQuizListPosition)

        mKelasDetails.materiList[mMateriListPosition].kuis = kuisList

        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        FirestoreClass().addUpdateMateriList(this, mKelasDetails)
    }

}