package com.example.mobilelearningapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.JawabTugasItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.adapters.JawabItemsAdapter
import com.example.mobilelearningapp.adapters.MateriFileItemsAdapter
import com.example.mobilelearningapp.databinding.ActivityMainGuruBinding
import com.example.mobilelearningapp.databinding.ActivityTugasBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.File
import com.example.mobilelearningapp.models.JawabanTugas
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Tugas
import com.example.mobilelearningapp.utils.Constants
import com.example.mobilelearningapp.utils.SwipeToDeleteCallback
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TugasActivity : BaseActivity() {

    private var binding : ActivityTugasBinding? = null
    private lateinit var mKelasDetails : Kelas
    lateinit var mKelasDocumentId : String
    private var mMateriListPosition = -1
    private var mTugasListPosition = -1
    private var isUpdate = false

    private var mSelectedDueDateMilliSeconds : Long = 0
    private var mSelectedImageFileUri : Uri? = null
    private var mMateriImageURL: String = ""

    private lateinit var mJawabList: java.util.ArrayList<JawabanTugas>

    private var mSelectedFileUri: Uri? = null
    private var mFileType: String? = ""
    private var mFileName: String? = ""
    private var mDatabasePdf: Uri? = null
    private var mUploadedPdfUri: Uri? = null
    private var uploadedPdfFileName: String = ""
    private var selectedPdfFileName: String = ""
    private lateinit var mStorageReference: StorageReference

    companion object {
        const val REQUEST_CODE_JAWAB_DETAILS = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTugasBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        getIntentData()
        setupActionBar()
        mStorageReference = FirebaseStorage.getInstance().reference


        val currentUserID = FirestoreClass().getCurrentUserID()
        if (currentUserID.isNotEmpty()) {
            FirestoreClass().getUserRole(currentUserID) { role ->
                if (role == "siswa") {
                    binding?.btnLihatHasilTugas?.visibility = View.GONE
                    binding?.btnKumpulTugas?.setOnClickListener {

                        val intent = Intent(this@TugasActivity,JawabActivity::class.java)
                        intent.putExtra(Constants.MATERI_LIST_ITEM_POSITION,mMateriListPosition)
                        intent.putExtra(Constants.TUGAS_LIST_ITEM_POSITION,mTugasListPosition)
                        intent.putExtra(Constants.KELAS_DETAIL,mKelasDetails)
                        intent.putExtra(Constants.DOCUMENT_ID, mKelasDocumentId)
                        startActivityForResult(intent, REQUEST_CODE_JAWAB_DETAILS)
                    }
                }else{
                    if (isUpdate){
                        binding?.btnKumpulTugas?.text = "Update Tugas"
                    }else{
                        binding?.btnKumpulTugas?.text = "Buat Tugas"
                    }
                }
            }
        }

        if (isUpdate){
            showProgressDialog(resources.getString(R.string.mohon_tunggu))
            setUpDataTugas()
        }


        binding?.btnBold?.setOnClickListener { applyStyle(Typeface.BOLD) }
        binding?.btnItalic?.setOnClickListener { applyStyle(Typeface.ITALIC)}

        binding?.btnUploadImage?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        binding?.btnUploadFile?.setOnClickListener {
           selectFile()
        }



        binding?.btnDueDate?.setOnClickListener {
            showDatePicker()
        }

        binding?.btnKumpulTugas?.setOnClickListener {
            FirestoreClass().getUserRole(currentUserID) { role ->
                if (role == "siswa") {
//                    val intent = Intent(this,ActivityJawabTugas ::class.java)
                } else {
                    if (isUpdate){
                        updateTugas()
                    }else{
                        createTugas()
                    }
                }
            }
        }

        binding?.btnLihatHasilTugas?.setOnClickListener {
            val intent = Intent(this,JawabanListActivity::class.java)
            intent.putExtra(Constants.MATERI_LIST_ITEM_POSITION,mMateriListPosition)
            intent.putExtra(Constants.TUGAS_LIST_ITEM_POSITION,mTugasListPosition)
            intent.putExtra(Constants.KELAS_DETAIL,mKelasDetails)
            intent.putExtra(Constants.DOCUMENT_ID, mKelasDocumentId)
            startActivityForResult(intent,REQUEST_CODE_JAWAB_DETAILS)
        }

    }

    private fun setUpDataTugas() {

        val currentTugas = mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition]
        populateJawabListToUI(mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition].jawab)
        val currentUserID = FirestoreClass().getCurrentUserID()
        if (currentUserID.isNotEmpty()) {
            FirestoreClass().getUserRole(currentUserID) { role ->
                if (role == "siswa") {
                    binding?.etSoal?.visibility = View.GONE
                    binding?.tvSoal?.visibility = View.VISIBLE
                    binding?.etNamaSoal?.inputType = InputType.TYPE_NULL
                    binding?.llInput?.visibility = View.GONE
                    binding?.btnDueDate?.visibility = View.INVISIBLE
                }else{
                    binding?.etNamaSoal?.setText(currentTugas.namaTugas)
                    binding?.etSoal?.setText(currentTugas.soal)
                }
            }
        }

        mSelectedDueDateMilliSeconds = currentTugas.dueDate

        if (mSelectedDueDateMilliSeconds > 0) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            binding?.llDueDate?.visibility = View.VISIBLE
            binding?.tvSelectDueDate?.text = selectedDate
        }

        // Load existing PDF information
        if (currentTugas.pdfUrl.isNotEmpty()) {
            mDatabasePdf = currentTugas.pdfUrl.toUri()
            selectedPdfFileName = currentTugas.pdfUrlName
            binding?.tvNamaFile?.text = selectedPdfFileName
            binding?.cvFile?.visibility = View.VISIBLE

            binding?.cvFile?.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = mDatabasePdf
                startActivity(intent)
            }
        }

        if (currentTugas.imageSoal.isNotEmpty()) {
            binding?.llImageSoal?.visibility = View.VISIBLE
            Glide
                .with(this@TugasActivity)
                .load(currentTugas.imageSoal)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(binding?.ivImageMateri!!)
        } else {
            binding?.llImageSoal?.visibility = View.GONE
        }
        hideProgressDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_tugas, menu)
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
                                Toast.makeText(this@TugasActivity,
                                    "siswa tidak bisa menghapus tugas",
                                    Toast.LENGTH_LONG
                                ).show()
                            )
                        }else{
                            showAlertDialogToDeleteTugas(
                                mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition].namaTugas!!)
                            }

                        }
                    }
                }
            }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.KELAS_DETAIL)) {
            mKelasDetails = intent.getParcelableExtra(Constants.KELAS_DETAIL)!!

        }
        if (intent.hasExtra(Constants.MATERI_LIST_ITEM_POSITION)) {
            mMateriListPosition = intent.getIntExtra(Constants.MATERI_LIST_ITEM_POSITION, -1)
            Log.e("MATERI_ITEM_POSITION", mMateriListPosition.toString())
        }
        if (intent.hasExtra(Constants.TUGAS_LIST_ITEM_POSITION)) {
            mTugasListPosition = intent.getIntExtra(Constants.TUGAS_LIST_ITEM_POSITION, -1)
            Log.e("TUGAS_ITEM_POSITION", mTugasListPosition.toString())
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

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarTugas)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            if (isUpdate){
                supportActionBar?.title = "Tugas ${mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition].namaTugas}"
            }else{
                supportActionBar?.title = "Buat Tugas"
            }

        }
        binding?.toolbarTugas?.setNavigationOnClickListener {
            onBackPressed()
        }
    }



    private fun applyStyle(style: Int) {
        val start = binding?.etSoal?.selectionStart
        val end = binding?.etSoal?.selectionEnd
        val spannableString = SpannableStringBuilder(binding?.etSoal?.text)
        if (start != null) {
            if (end != null) {
                spannableString.setSpan(StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        binding?.etSoal?.setText(spannableString)
        if (end != null) {
            binding?.etSoal?.setSelection(end)
        }
    }

    private fun createTugas() {
        val namaTugas = binding?.etNamaSoal?.text.toString().trim()
        val deskripsiTugas = binding?.etSoal?.text.toString().trim()
        val PdfUrl = if (mUploadedPdfUri != null) mUploadedPdfUri.toString() else ""

        if (namaTugas.isEmpty()) {
            Toast.makeText(this, "Mohon masukkan nama tugas", Toast.LENGTH_SHORT).show()
            return
        }

        //TODO UPDATE TUGAS BELUM
//        showProgressDialog(resources.getString(R.string.mohon_tunggu))

        val tugas = Tugas(
            id = UUID.randomUUID().toString(),
            namaTugas = namaTugas,
            soal = deskripsiTugas,
            imageSoal = mMateriImageURL,
            dueDate = mSelectedDueDateMilliSeconds,
            createdBy = FirestoreClass().getCurrentUserID(),
            pdfUrl =  PdfUrl,
            pdfUrlName= selectedPdfFileName,
        )


        mKelasDetails.materiList[mMateriListPosition].tugas.add(tugas)


        FirestoreClass().addUpdateMateriList(this, mKelasDetails)

//        FirestoreClass().updateMateriDetail(this@TugasActivity, mKelasDocumentId, mKelasDetails.materiList[mMateriListPosition])

    }

    private fun updateTugas() {
        val namaTugas = binding?.etNamaSoal?.text.toString().trim()
        val deskripsiTugas = binding?.etSoal?.text.toString().trim()

        if (namaTugas.isEmpty()) {
            Toast.makeText(this, "Mohon masukkan nama tugas", Toast.LENGTH_SHORT).show()
            return
        }

        showProgressDialog(resources.getString(R.string.mohon_tunggu))

        // Preserve existing PDF information if not changed
        val currentTugas = mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition]
        val updatedPdfUrl = if (mUploadedPdfUri != null) mUploadedPdfUri.toString() else currentTugas.pdfUrl
        val updatedPdfUrlName = if (selectedPdfFileName.isNotEmpty()) selectedPdfFileName else currentTugas.pdfUrlName

        val updatedTugas = Tugas(
            id = currentTugas.id,
            namaTugas = namaTugas,
            soal = deskripsiTugas,
            imageSoal = if (mMateriImageURL.isNotEmpty()) mMateriImageURL else currentTugas.imageSoal,
            dueDate = mSelectedDueDateMilliSeconds,
            createdBy = FirestoreClass().getCurrentUserID(),
            pdfUrl = updatedPdfUrl,
            pdfUrlName = updatedPdfUrlName,
            jawab = currentTugas.jawab // Preserve existing jawab data
        )

        // Update the tugas in the mKelasDetails
        mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition] = updatedTugas

        // Update in Firestore
        FirestoreClass().updateTugasInMateri(
            this,
            mKelasDocumentId,
            mMateriListPosition,
            mTugasListPosition,
            updatedTugas
        )
    }



    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, Constants.PICK_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {

                showProgressDialog(resources.getString(R.string.mohon_tunggu))
                val selectedPdfUri: Uri? = data.data
                mFileType = getFileType(mSelectedFileUri)

                // Mendapatkan nama file dari URI
                val pdfFileName: String? = getFileName(selectedPdfUri)

                // Menyimpan file PDF ke Firebase Storage
                val pdfStorageReference: StorageReference =
                    mStorageReference.child("soal/$pdfFileName")
                pdfStorageReference.putFile(selectedPdfUri!!)
                    .addOnSuccessListener { taskSnapshot ->
                        // File berhasil diunggah
                        pdfStorageReference.downloadUrl.addOnCompleteListener { uriTask ->
                            if (uriTask.isSuccessful) {
                                binding?.cvFile?.visibility = View.VISIBLE
                                when (mFileType) {
                                    "pdf" -> binding?.imageViewPdfLogo?.setImageResource(R.drawable.pdf)
                                    "doc", "docx" -> binding?.imageViewPdfLogo?.setImageResource(R.drawable.word)
                                    "ppt", "pptx" -> binding?.imageViewPdfLogo?.setImageResource(R.drawable.ppt)
                                    else -> binding?.imageViewPdfLogo?.setImageResource(R.drawable.pdf)
                                }

                                selectedPdfFileName = pdfFileName ?: ""
                                uploadedPdfFileName = selectedPdfFileName
                                mUploadedPdfUri = uriTask.result
                                binding?.tvNamaFile?.text =
                                    "$selectedPdfFileName"
                                hideProgressDialog()

                                // Simpan informasi file PDF ke SharedPreferences atau ViewModel
                                val sharedPref =
                                    getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                                val editor = sharedPref.edit()
                                editor.putString("pdf_file_name", selectedPdfFileName)
                                editor.putString("pdf_uri", mUploadedPdfUri.toString())
                                editor.apply()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        hideProgressDialog()
                    }
            }
        }
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this@TugasActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding?.ivImageMateri!!)

                binding?.llImageSoal?.visibility = View.VISIBLE

                // Panggil fungsi untuk upload gambar
                showProgressDialog("Uploading Image")
                uploadMateriImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (requestCode == REQUEST_CODE_JAWAB_DETAILS && resultCode == RESULT_OK) {
            showProgressDialog(resources.getString(R.string.mohon_tunggu))

            FirestoreClass().getKelasDetails(this, mKelasDocumentId)

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
                binding?.btnDueDate?.text = selectedDate

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

//    private fun uploadFileToFirebase(fileUri: Uri) {
//        showProgressDialog("Uploading File...")
//        val storageRef = FirebaseStorage.getInstance().reference
//        val fileName = UUID.randomUUID().toString()
//        val fileRef = storageRef.child("TUGAS_FILES").child(fileName)
//
//        fileRef.putFile(fileUri)
//            .addOnSuccessListener { taskSnapshot ->
//                Toast.makeText(this, "Upload Success", Toast.LENGTH_LONG).show()
//
//                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
//                    val downloadUrl = downloadUri.toString()
//                    val file = File(
//                        name = mFileName.toString(),
//                        url = downloadUrl,
//                        fileType = mFileType.toString()
//                    )
//
//                    // Assuming the last created tugas is the current one
//                    val currentTugas = mKelasDetails.materiList[mMateriListPosition].tugas.last()
//                    currentTugas.fileSoal.add(file)
//
//                    FirestoreClass().updateMateriDetail(this, mKelasDocumentId, mKelasDetails.materiList[mMateriListPosition])
//                }
//            }
//            .addOnFailureListener { exception ->
//                Toast.makeText(this, "Failed to upload file: ${exception.message}", Toast.LENGTH_LONG).show()
//            }
//    }

    private fun getFileType(uri: Uri?): String? {
        return if (uri == null) {
            null
        } else {
            val contentResolver: ContentResolver = this.contentResolver
            val mimeTypeMap = MimeTypeMap.getSingleton()
            mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
        }
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri?): String? {
        var result: String? = null
        if (uri?.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri?.lastPathSegment
        }
        return result
    }

    private fun uploadMateriImage() {

        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "SOAL_IMAGE" + System.currentTimeMillis() + "."
                        + Constants.getFileExtension(this, mSelectedImageFileUri!!)
            )

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.e(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    hideProgressDialog()
                    Log.e("Downloadable Image URL", uri.toString())
                    mMateriImageURL = uri.toString()

                    //TODO GANTI INI KALAU IMAGE TIDAK MAU MUNCUL SAAT BUAT TUGAS PERTAMA KALI HILANGKAN ISUPDATE
                    if (isUpdate){
                        mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition].imageSoal = mMateriImageURL
                    }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                this,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()
                hideProgressDialog()
            }
        }
    }

    fun materiUpdateSuccess() {
        setResult(RESULT_OK)
        Toast.makeText(this, "Deskripsi tugas berhasil diperbarui", Toast.LENGTH_SHORT).show()

    }



    fun kelasDetails(kelas: Kelas){
        mKelasDetails = kelas
        populateJawabListToUI(mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition].jawab)
        hideProgressDialog()

    }

    fun addUpdateMateriListSuccess(){
        setResult(RESULT_OK)
        Toast.makeText(this, " tugas berhasil ditambah", Toast.LENGTH_SHORT).show()
        finish()

    }

    fun tugasUpdateSuccess(){
        FirestoreClass().addUpdateMateriList(this, mKelasDetails)
    }

    fun populateJawabListToUI(jawabList: ArrayList<JawabanTugas>) {
        mJawabList = jawabList

        //hanya jawaban yang dibuat yang muncul
        val filteredJawabanList = ArrayList<JawabanTugas>()

        val currentUserID = FirestoreClass().getCurrentUserID()

        for (jawaban in jawabList) {
            if (jawaban.assignedTo.contains(currentUserID)) {
                filteredJawabanList.add(jawaban)
            }
        }

        val rvJawabList: RecyclerView = findViewById(R.id.rv_jawaban)
        val btnKumpul : Button = findViewById(R.id.btn_kumpulTugas)

        if (filteredJawabanList.isNotEmpty()) {
            rvJawabList.visibility = View.VISIBLE
            btnKumpul.visibility = View.GONE
            rvJawabList.layoutManager = LinearLayoutManager(this)
            rvJawabList.setHasFixedSize(true)

            val adapter = JawabTugasItemsAdapter(this, filteredJawabanList)
            rvJawabList.adapter = adapter

            val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val jawabToDelete = jawabList[position]

                    val dialogView = LayoutInflater.from(this@TugasActivity).inflate(R.layout.dialog_confirm_delete, null)
                    val dialog = AlertDialog.Builder(this@TugasActivity)
                        .setView(dialogView)
                        .create()

                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()

                    val tvYa = dialogView.findViewById<TextView>(R.id.tv_ya)
                    val tvTidak = dialogView.findViewById<TextView>(R.id.tv_tidak)

                    tvYa.setOnClickListener {
                        showProgressDialog(resources.getString(R.string.mohon_tunggu))
                        FirestoreClass().deleteJawabTugas(
                            this@TugasActivity,
                            mKelasDocumentId,
                            mMateriListPosition,
                            mTugasListPosition,
                            jawabToDelete.id
                        )
                        dialog.dismiss()
                    }

                    tvTidak.setOnClickListener {
                        dialog.dismiss()
                    }

                }
            }

            val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
            deleteItemTouchHelper.attachToRecyclerView(rvJawabList)

            adapter.setOnClickListener(object: JawabTugasItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: JawabanTugas) {
                    jawabanDetails(position)
                }
            })

        } else {
            rvJawabList.visibility = View.GONE
            btnKumpul.visibility = View.VISIBLE
        }
    }

    fun jawabTugasDeleteSuccess() {
        hideProgressDialog()
        Toast.makeText(this, "Jawaban tugas berhasil dihapus", Toast.LENGTH_SHORT).show()
        FirestoreClass().getKelasDetails(this, mKelasDocumentId) // Refresh data
    }

    fun jawabanDetails(jawabanPosition: Int){
        val intent = Intent(this, JawabActivity::class.java)
        intent.putExtra(Constants.MATERI_LIST_ITEM_POSITION,mMateriListPosition)
        intent.putExtra(Constants.TUGAS_LIST_ITEM_POSITION,mTugasListPosition)
        intent.putExtra(Constants.JAWAB_LIST_ITEM_POSITION,jawabanPosition)
        intent.putExtra(Constants.KELAS_DETAIL,mKelasDetails)
        intent.putExtra(Constants.IS_UPDATE, true)
        intent.putExtra(Constants.DOCUMENT_ID, mKelasDocumentId)
        startActivityForResult(intent, REQUEST_CODE_JAWAB_DETAILS)

    }

    private fun showAlertDialogToDeleteTugas(tugasName: String) {

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("DELETE")
        builder.setMessage("Apakah anda yakin ingin menghapus tugas $tugasName")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Iya") { dialogInterface, _ ->
            showProgressDialog(resources.getString(R.string.mohon_tunggu))
            deleteTugas()
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("Tidak") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: androidx.appcompat.app.AlertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun deleteTugas() {
        val tugasList: ArrayList<Tugas> = mKelasDetails.materiList[mMateriListPosition].tugas
        tugasList.removeAt(mTugasListPosition)

        mKelasDetails.materiList[mMateriListPosition].tugas = tugasList

        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        FirestoreClass().addUpdateMateriList(this, mKelasDetails)
    }

}



//    private fun deleteJawaban() {
//        val kelompokList: ArrayList<JawabanTugas> = mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition].jawab
//        kelompokList.removeAt(mKelompokListPosition)
//
//        // Remove the topic only if there are no more kelompok in it
////        if (kelompokList.isEmpty()) {
////            mCourseDetails.topicList.removeAt(mTopicListPosition)
////        } else {
//        // Update the kelompok list for the specific topic
//        mCourseDetails.topicList[mTopicListPosition].kelompok = kelompokList
////        }
//
//        showProgressDialog(resources.getString(R.string.mohon_tunggu))
//        FirestoreClass().addUpdateTopicList(this, mCourseDetails)
//    }
//
//}