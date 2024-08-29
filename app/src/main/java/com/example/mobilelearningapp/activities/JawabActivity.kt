package com.example.mobilelearningapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import com.example.mobilelearningapp.adapters.JawabItemsAdapter
import com.example.mobilelearningapp.adapters.MateriFileItemsAdapter
import com.example.mobilelearningapp.databinding.ActivityJawabBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.File
import com.example.mobilelearningapp.models.JawabanTugas
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Tugas
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.*

class JawabActivity : BaseActivity() {

    private var binding : ActivityJawabBinding? = null
    private lateinit var mKelasDetails : Kelas
    lateinit var mKelasDocumentId : String
    private var mMateriListPosition = -1
    private var mTugasListPosition = -1
    private var mJawabListPosition = -1

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

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityJawabBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        setupActionBar()
        getIntentData()
        mStorageReference = FirebaseStorage.getInstance().reference

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

        binding?.btnKumpulTugas?.setOnClickListener {
            createJawaban()
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
        if (intent.hasExtra(Constants.TUGAS_LIST_ITEM_POSITION)) {
            mTugasListPosition = intent.getIntExtra(Constants.TUGAS_LIST_ITEM_POSITION, -1)
            Log.e("TUGAS_ITEM_POSITION", mTugasListPosition.toString())
        }
        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mKelasDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
            Log.e("document", "document $mKelasDocumentId")
        }
        if (intent.hasExtra(Constants.JAWAB_LIST_ITEM_POSITION)) {
            mJawabListPosition = intent.getIntExtra(Constants.JAWAB_LIST_ITEM_POSITION, -1)
            Log.e("JAWAB_ITEM_POSITION", mJawabListPosition.toString())
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarJawab)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

//            supportActionBar?.title = "Tugas ${
//                mKelasDetails.
//                materiList[mMateriListPosition].
//                tugas[mTugasListPosition].
//                jawab[mJawabListPosition].createdBy}"


        }
        binding?.toolbarJawab?.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    private fun applyStyle(style: Int) {
        val start = binding?.etJawab?.selectionStart
        val end = binding?.etJawab?.selectionEnd
        val spannableString = SpannableStringBuilder(binding?.etJawab?.text)
        if (start != null) {
            if (end != null) {
                spannableString.setSpan(StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        binding?.etJawab?.setText(spannableString)
        if (end != null) {
            binding?.etJawab?.setSelection(end)
        }
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
                    mStorageReference.child("jawab/$pdfFileName")
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
                    .with(this@JawabActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding?.ivImageJawab!!)

                binding?.llImageJawab?.visibility = View.VISIBLE

                // Panggil fungsi untuk upload gambar
                showProgressDialog("Uploading Image")
                uploadMateriImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, Constants.PICK_FILE_REQUEST_CODE)
    }

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
                "JAWABAN_IMAGE" + System.currentTimeMillis() + "."
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

//                    //TODO GANTI INI KALAU IMAGE TIDAK MAU MUNCUL SAAT BUAT TUGAS PERTAMA KALI HILANGKAN ISUPDATE
//                    if (isUpdate){
//                        mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition].imageSoal = mMateriImageURL
//                    }
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

    private fun createJawaban() {
        val namaPenjawab = binding?.etNamaPenjawab?.text.toString().trim()
        val deskripsiTugas = binding?.etJawab?.text.toString().trim()
        val PdfUrl = if (mUploadedPdfUri != null) mUploadedPdfUri.toString() else ""
        val assignedUserArrayList: ArrayList<String> = ArrayList()
        assignedUserArrayList.add(FirestoreClass().getCurrentUserID())

        if (namaPenjawab.isEmpty()) {
            Toast.makeText(this, "Mohon masukkan nama anda", Toast.LENGTH_SHORT).show()
            return
        }

        //TODO UPDATE TUGAS BELUM
//        showProgressDialog(resources.getString(R.string.mohon_tunggu))

        val jawab = JawabanTugas(

            id = UUID.randomUUID().toString(),
            namaPenjawab = namaPenjawab,
            jawaban = deskripsiTugas,
            imageJawaban = mMateriImageURL,
            uploadedDate =  System.currentTimeMillis(),
            createdBy = FirestoreClass().getCurrentUserID(),
            pdfUrl =  PdfUrl,
            assignedTo =  assignedUserArrayList,
            pdfUrlName= selectedPdfFileName,
        )


        mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition].jawab.add(jawab)


        FirestoreClass().addUpdateMateriList(this, mKelasDetails)

//        FirestoreClass().updateMateriDetail(this@TugasActivity, mKelasDocumentId, mKelasDetails.materiList[mMateriListPosition])

    }

    fun addUpdateMateriListSuccess(){
        setResult(RESULT_OK)
        Toast.makeText(this, " tugas berhasil ditambah", Toast.LENGTH_SHORT).show()
        finish()

    }


}