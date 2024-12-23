package com.example.mobilelearningapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.InputType
import android.text.Spannable
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityJawabBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.JawabanTugas
import com.example.mobilelearningapp.models.Materi
import com.example.mobilelearningapp.utils.Constants
import com.example.mobilelearningapp.utils.FormattedTextHandler
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.*

class JawabActivity : BaseActivity() {

    private var binding : ActivityJawabBinding? = null
    private lateinit var mMateriDetails : Materi
    lateinit var mKelasDocumentId : String
    private var mMateriListPosition = -1
    private var mTugasListPosition = -1
    private lateinit var mJawabanTugasId: String
    private var mJawabListPosition = -1
        get() = findTugasIndexById(mJawabanTugasId)
    private var isUpdate = false

    private var mUloadedJawaban : Long = 0
    private var mSelectedImageFileUri : Uri? = null
    private var mMateriImageURL: String = ""

    private var mSelectedVideoFileUri : Uri? = null
    private var mMateriVideoURL: String = ""

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

        val currentUserID = FirestoreClass().getCurrentUserID()
        if (currentUserID.isNotEmpty()) {
            FirestoreClass().getUserRole(currentUserID) { role ->
                if (role == "siswa") {
                    if (isUpdate){
                        binding?.btnKumpulTugas?.text = "Update Jawaban"
                        setUpDataJawaban()
                    }else{
                        binding?.btnKumpulTugas?.text = "Kumpul Tugas"
                    }
                    binding?.etNilai?.inputType = InputType.TYPE_NULL

                }else{
                    binding?.btnKumpulTugas?.text = "Beri Nilai"
                    binding?.tvJawaban?.visibility = View.VISIBLE
                    try {
                        val formattedText = FormattedTextHandler.fromJson( mMateriDetails.tugas[mTugasListPosition].jawab[mJawabListPosition].jawaban)
                        val spannableString = FormattedTextHandler.toSpannableString(formattedText)
                        binding?.tvJawaban?.text = spannableString
                    } catch (e: Exception) {
                        binding?.tvJawaban?.text =  mMateriDetails.tugas[mTugasListPosition].jawab[mJawabListPosition].jawaban
                        Log.e("JawabActivity", "Error parsing formatted text for tvSoal: ${e.message}")
                    }
                    binding?.etJawab?.visibility = View.GONE
                    binding?.etNamaPenjawab?.inputType = InputType.TYPE_NULL
                    binding?.llInput?.visibility = View.GONE
                    setUpDataJawaban()
                }
            }
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

        binding?.btnUploadVideo?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
                Constants.showVideoChooser(this)
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        binding?.btnDeleteImage?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hapus Foto")
                .setMessage("Apakah Anda yakin ingin menghapus foto ini?")
                .setPositiveButton("Ya") { dialog, _ ->
                    // Logika untuk menghapus file dari tampilan dan database
                    binding?.llImageJawab?.visibility = View.GONE
                    deleteJawabImage()
                    dialog.dismiss()
                }
                .setNegativeButton("Tidak") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding?.btnDeleteVideo?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hapus Video")
                .setMessage("Apakah Anda yakin ingin menghapus video ini?")
                .setPositiveButton("Ya") { dialog, _ ->
                    // Logika untuk menghapus file dari tampilan dan database
                    binding?.llVideoMateri?.visibility = View.GONE
                    deleteJawabVideo()
                    dialog.dismiss()
                }
                .setNegativeButton("Tidak") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding?.btnUploadFile?.setOnClickListener {
            selectFile()
        }

        binding?.btnKumpulTugas?.setOnClickListener {

            if (currentUserID.isNotEmpty()) {
                FirestoreClass().getUserRole(currentUserID) { role ->
                    if (role == "siswa") {
                        if (isUpdate){
                            updateJawaban()
                        }else{
                            createJawaban()
                        }
                    }else {
                        updateJawaban()
                    }
                }
            }
        }
        try {
            val formattedText = FormattedTextHandler.fromJson(mMateriDetails.tugas[mTugasListPosition].soal)
            val spannableString = FormattedTextHandler.toSpannableString(formattedText)
            binding?.tvSoal?.text = spannableString
        } catch (e: Exception) {
            binding?.tvSoal?.text =mMateriDetails.tugas[mTugasListPosition].soal
            Log.e("JawabActivity", "Error parsing formatted text for tvSoal: ${e.message}")
        }

        if (mMateriDetails.tugas[mTugasListPosition].imageSoal.isNotEmpty()) {
            binding?.llImageSoal?.visibility = View.VISIBLE
            Glide
                .with(this@JawabActivity)
                .load(mMateriDetails.tugas[mTugasListPosition].imageSoal)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(binding?.ivImageSoal!!)
        } else {
            binding?.llImageSoal?.visibility = View.GONE
        }

        binding?.llVideoMateri?.setOnClickListener {
            if (mMateriDetails.tugas[mTugasListPosition].jawab[mJawabListPosition].videoJawaban.isNotEmpty()) {
                val intent = Intent(this, VideoPlayerActivity::class.java)
                intent.putExtra("VIDEO_URL", mMateriDetails.tugas[mTugasListPosition].jawab[mJawabListPosition].videoJawaban)
                startActivity(intent)
            }
        }

    }

    private fun findTugasIndexById(id: String): Int {
        return mMateriDetails.tugas[mTugasListPosition].jawab.indexOfFirst { it.id == id }
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.MATERI_DETAIL)) {
            mMateriDetails = intent.getParcelableExtra(Constants.MATERI_DETAIL)!!
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
        if (intent.hasExtra(Constants.JAWABAN_TUGAS_ID)) {
            mJawabanTugasId = intent.getStringExtra(Constants.JAWABAN_TUGAS_ID)!!
            Log.e("MATERI_ID", mJawabanTugasId)
        }
        if (intent.hasExtra(Constants.IS_UPDATE)) {
            isUpdate = intent.getBooleanExtra(Constants.IS_UPDATE, false)
            Log.e("TOCOURSE ", isUpdate.toString())
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

    private fun setUpDataJawaban() {


        val currentJawaban = mMateriDetails.tugas[mTugasListPosition].jawab[mJawabListPosition]
        val currentUserID = FirestoreClass().getCurrentUserID()


        binding?.etNamaPenjawab?.setText(currentJawaban.namaPenjawab)
        binding?.etNilai?.setText(currentJawaban.nilai)
        mUloadedJawaban = currentJawaban.uploadedDate

        val jawaban = currentJawaban.jawaban
        try {
            val formattedText = FormattedTextHandler.fromJson(jawaban)
            val spannableString = FormattedTextHandler.toSpannableString(formattedText)
            binding?.etJawab?.setText(spannableString)
        } catch (e: Exception) {
            // Jika terjadi kesalahan, tampilkan teks asli tanpa formatting
            binding?.etJawab?.setText(jawaban)
            Log.e("JawabActivity", "Error parsing formatted text: ${e.message}")
        }



        // Load existing PDF information
        if (currentJawaban.pdfUrl.isNotEmpty()) {
            mDatabasePdf = currentJawaban.pdfUrl.toUri()
            selectedPdfFileName = currentJawaban.pdfUrlName
            binding?.tvNamaFile?.text = selectedPdfFileName
            binding?.cvFile?.visibility = View.VISIBLE

            binding?.cvFile?.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = mDatabasePdf
                startActivity(intent)
            }
        }

        if (currentJawaban.imageJawaban.isNotEmpty()) {
            binding?.llImageJawab?.visibility = View.VISIBLE
            Glide
                .with(this@JawabActivity)
                .load(currentJawaban.imageJawaban)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(binding?.ivImageJawab!!)
        } else {
            binding?.llImageJawab?.visibility = View.GONE
        }
        if (currentJawaban.videoJawaban.isNotEmpty()) {
            binding?.llVideoMateri?.visibility = View.VISIBLE
            Glide
                .with(this@JawabActivity)
                .load(currentJawaban.videoJawaban)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(binding?.ivVideoMateri!!)
        } else {
            binding?.llVideoMateri?.visibility = View.GONE
        }
    }


    private fun applyStyle(style: Int) {
        val start = binding?.etJawab?.selectionStart ?: 0
        val end = binding?.etJawab?.selectionEnd ?: 0
        val spannable = binding?.etJawab?.text as? Spannable ?: return
        FormattedTextHandler.toggleStyle(spannable, start, end, style)
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
                uploadJawabanImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_VIDEO_REQUEST_CODE
            && data!!.data != null
        ) {

            mSelectedVideoFileUri = data.data

            try {
                Glide
                    .with(this@JawabActivity)
                    .load(mSelectedVideoFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding?.ivVideoMateri!!)

                binding?.llVideoMateri?.visibility = View.VISIBLE
                binding?.llUploadProgress?.visibility = View.VISIBLE
                uploadJawabanVideo()
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

    private fun uploadJawabanImage() {

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

    private fun uploadJawabanVideo() {

        if (mSelectedVideoFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "JAWABAN_VIDEO" + System.currentTimeMillis() + "."
                        + Constants.getFileExtension(this, mSelectedVideoFileUri!!)
            )

            sRef.putFile(mSelectedVideoFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.e(
                    "Firebase Video URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    binding?.llUploadProgress?.visibility = View.GONE
                    Log.e("Downloadable Video URL", uri.toString())
                    mMateriVideoURL = uri.toString()

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

            }.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                binding?.progressBarUploadVideo?.progress = progress.toInt()
                binding?.textProgress?.text = "${progress.toString()} %"
            }
        }
    }

    private fun deleteJawabImage() {

        if (isUpdate){
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mMateriDetails.tugas[mTugasListPosition].jawab[mJawabListPosition].imageJawaban)

            // Delete the image from Firebase Storage
            storageRef.delete().addOnSuccessListener {
                // Image deleted successfully from Storage, now update Firestore
                mMateriDetails.tugas[mTugasListPosition].jawab[mJawabListPosition].imageJawaban = ""

            }.addOnFailureListener { exception ->
                hideProgressDialog()
                Toast.makeText(
                    this@JawabActivity,
                    "Error deleting image: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }else{
            binding?.llImageJawab?.visibility = View.GONE
            mMateriImageURL = ""
        }

        // Get the storage reference of the image

    }

    private fun deleteJawabVideo() {

        if (isUpdate){
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mMateriDetails.tugas[mTugasListPosition].jawab[mJawabListPosition].videoJawaban)

            // Delete the image from Firebase Storage
            storageRef.delete().addOnSuccessListener {
                // Image deleted successfully from Storage, now update Firestore
                mMateriDetails.tugas[mTugasListPosition].jawab[mJawabListPosition].videoJawaban = ""

            }.addOnFailureListener { exception ->
                hideProgressDialog()
                Toast.makeText(
                    this@JawabActivity,
                    "Error deleting video: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }else{
            binding?.llVideoMateri?.visibility = View.GONE
            mMateriVideoURL = ""
        }

        // Get the storage reference of the image

    }

    private fun createJawaban() {

        val namaPenjawab = binding?.etNamaPenjawab?.text.toString().trim()
        val deskripsiTugas = binding?.etJawab?.text.toString().trim()
        val spannable = binding?.etJawab?.text as? Spannable
        val formattedTextJson = if (spannable != null) {
            val spans = FormattedTextHandler.getExistingSpans(spannable)
            FormattedTextHandler.toJson(deskripsiTugas, spans)
        } else {
            deskripsiTugas
        }
        val PdfUrl = if (mUploadedPdfUri != null) mUploadedPdfUri.toString() else ""
        val assignedUserArrayList: ArrayList<String> = ArrayList()
        assignedUserArrayList.add(FirestoreClass().getCurrentUserID())


        if (namaPenjawab.isEmpty()) {
            Toast.makeText(this, "Mohon masukkan nama anda", Toast.LENGTH_SHORT).show()
            return
        }else{
            val jawab = JawabanTugas(

                id = UUID.randomUUID().toString(),
                namaTugas = mMateriDetails.tugas[mTugasListPosition].namaTugas,
                namaPenjawab = namaPenjawab,
                namaMateri  = mMateriDetails.nama,
                namaKelas = mMateriDetails.kelas,
                jawaban = formattedTextJson,
                imageJawaban = mMateriImageURL,
                videoJawaban = mMateriVideoURL,
                uploadedDate =  System.currentTimeMillis(),
                createdBy = FirestoreClass().getCurrentUserID(),
                pdfUrl =  PdfUrl,
                assignedTo =  assignedUserArrayList,
                pdfUrlName= selectedPdfFileName,
            )

            showProgressDialog(resources.getString(R.string.mohon_tunggu))
            mMateriDetails.tugas[mTugasListPosition].jawab.add(jawab)


            FirestoreClass().updateSingleMateriInKelas(this, mKelasDocumentId,mMateriDetails)
        }
//



//        FirestoreClass().updateMateriDetail(this@TugasActivity, mKelasDocumentId, mKelasDetails.materiList[mMateriListPosition])

    }

    fun addUpdateMateriListSuccess(){
        hideProgressDialog()
        setResult(RESULT_OK)
        Toast.makeText(this, " jawaban berhasil ditambah", Toast.LENGTH_SHORT).show()
        finish()

    }

    private fun updateJawaban() {
        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        val namaPenjawab = binding?.etNamaPenjawab?.text.toString().trim()
        val deskripsiJawaban = binding?.etJawab?.text.toString().trim()
        val nilai = binding?.etNilai?.text.toString().trim()
        val spannable = binding?.etJawab?.text as? Spannable
        val formattedTextJson = if (spannable != null) {
            val spans = FormattedTextHandler.getExistingSpans(spannable)
            FormattedTextHandler.toJson(deskripsiJawaban, spans)
        } else {
            deskripsiJawaban
        }

        val PdfUrl = if (mUploadedPdfUri != null) mUploadedPdfUri.toString() else ""
        val assignedUserArrayList: ArrayList<String> = ArrayList()
        assignedUserArrayList.add(FirestoreClass().getCurrentUserID())

        if (namaPenjawab.isEmpty()) {
            Toast.makeText(this, "Mohon masukkan nama anda", Toast.LENGTH_SHORT).show()
            return
        }

        showProgressDialog(resources.getString(R.string.mohon_tunggu))

        // Preserve existing PDF information if not changed
        val currentJawaban = mMateriDetails.tugas[mTugasListPosition].jawab[mJawabListPosition]
        val updatedPdfUrl = if (mUploadedPdfUri != null) mUploadedPdfUri.toString() else currentJawaban.pdfUrl
        val updatedPdfUrlName = if (selectedPdfFileName.isNotEmpty()) selectedPdfFileName else currentJawaban.pdfUrlName

        val updatedJawaban = JawabanTugas(
            id = currentJawaban.id,
            namaPenjawab = namaPenjawab,
            namaTugas = mMateriDetails.tugas[mTugasListPosition].namaTugas,
            namaMateri  = mMateriDetails.nama,
            namaKelas = mMateriDetails.kelas,
            jawaban = formattedTextJson,
            imageJawaban = if (mMateriImageURL.isNotEmpty()) mMateriImageURL else currentJawaban.imageJawaban,
            videoJawaban = if (mMateriVideoURL.isNotEmpty()) mMateriVideoURL else currentJawaban.videoJawaban,
            uploadedDate = mUloadedJawaban,
            createdBy = FirestoreClass().getCurrentUserID(),
            pdfUrl = updatedPdfUrl,
            pdfUrlName = updatedPdfUrlName,
            assignedTo = currentJawaban.assignedTo,
            nilai = nilai
        )

        // Update the tugas in the mKelasDetails
        mMateriDetails.tugas[mTugasListPosition].jawab[mJawabListPosition] = updatedJawaban

        // Update in Firestore
        FirestoreClass().updateJawabanTugasInMateri(
            this,
            mKelasDocumentId,
            mMateriDetails,
            mTugasListPosition,
            mJawabListPosition,
            updatedJawaban
        )

    }

    fun jawabUpdateSuccess(){
        hideProgressDialog()
        FirestoreClass().updateSingleMateriInKelas(this, mKelasDocumentId,mMateriDetails)
    }


}