package com.example.mobilelearningapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.adapters.MateriFileItemsAdapter
import com.example.mobilelearningapp.databinding.ActivityMainGuruBinding
import com.example.mobilelearningapp.databinding.ActivityTugasBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.File
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Tugas
import com.example.mobilelearningapp.utils.Constants
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
    private lateinit var mFileList: ArrayList<File>
    private var isUpdate = false

    private var mSelectedDueDateMilliSeconds : Long = 0
    private var mSelectedImageFileUri : Uri? = null
    private var mMateriImageURL: String = ""

    private var mSelectedFileUri: Uri? = null
    private var mFileType: String? = ""
    private var mFileName: String? = ""
    private var mDatabasePdf: Uri? = null
    private var mUploadedPdfUri: Uri? = null
    private var uploadedPdfFileName: String = ""
    private var selectedPdfFileName: String = ""
    private lateinit var mStorageReference: StorageReference

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
        private const val REQUEST_FILE_PICK = 2
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
                }else{
                    binding?.btnKumpulTugas?.text = "Buat/Update Tugas"
                }
            }
        }

        if (isUpdate){
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
                    createTugas()
                }
            }
        }

    }

    private fun setUpDataTugas(){
        mSelectedDueDateMilliSeconds =
            mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition].dueDate

        if (mSelectedDueDateMilliSeconds > 0 ){
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            binding?.llDueDate?.visibility = View.VISIBLE
            binding?.tvSelectDueDate?.text = selectedDate
        }

        if (mDatabasePdf!= null) {
            selectedPdfFileName = mDatabasePdf.toString()
            binding?.tvNamaFile?.text =
                mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition].namaTugas
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
        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mKelasDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
            Log.e("document", "document $mKelasDocumentId")
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarTugas)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp)
            supportActionBar?.title = "Tugas ${mKelasDetails.materiList[mMateriListPosition].tugas}"
        }
        binding?.toolbarTugas?.setNavigationOnClickListener {
            val currentUserID = FirestoreClass().getCurrentUserID()
            if (currentUserID.isNotEmpty()) {
                FirestoreClass().getUserRole(currentUserID) { role ->
                    if (role == "siswa") {
                        val intent = Intent(this,MainActivitySiswa::class.java)
                        startActivity(intent)
                    }else{
                        val intent = Intent(this,MainGuruActivity::class.java)
                        startActivity(intent)
                    }
                }
            }

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
        if (isUpdate){
            val originalPdfUrl = mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition].pdfUrl
            val pdfUriString = mUploadedPdfUri?.toString() ?: originalPdfUrl
        }


        if (namaTugas.isEmpty()) {
            Toast.makeText(this, "Mohon masukkan nama tugas", Toast.LENGTH_SHORT).show()
            return
        }

        showProgressDialog(resources.getString(R.string.mohon_tunggu))

        val tugas = Tugas(
            id = UUID.randomUUID().toString(),
            namaTugas = namaTugas,
            soal = deskripsiTugas,
            dueDate = mSelectedDueDateMilliSeconds,
            createdBy = FirestoreClass().getCurrentUserID(),
            pdfUrl =  mMateriImageURL,
            pdfUrlName= selectedPdfFileName,
        )

        mKelasDetails.materiList[mMateriListPosition].tugas.add(tugas)

        FirestoreClass().updateMateriDetail(this@TugasActivity, mKelasDocumentId, mKelasDetails.materiList[mMateriListPosition])
        finish()
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
                uploadMateriImage()
            } catch (e: IOException) {
                e.printStackTrace()
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
                "MATERI_IMAGE" + System.currentTimeMillis() + "."
                        + Constants.getFileExtension(this, mSelectedImageFileUri!!)
            )

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.e(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.e("Downloadable Image URL", uri.toString())
                    mMateriImageURL = uri.toString()

                    mKelasDetails.materiList[mMateriListPosition].image = mMateriImageURL
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

}