package com.example.mobilelearningapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.*
import android.text.style.StyleSpan
import android.util.Log
import android.view.Menu
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.adapters.MateriFileItemsAdapter
import com.example.mobilelearningapp.databinding.ActivityGuruProfileBinding
import com.example.mobilelearningapp.databinding.ActivityMateriDetailsBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.MateriFile
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_materi_details.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MateriDetailsActivity : BaseActivity() {

    private var binding : ActivityMateriDetailsBinding? = null
    private lateinit var mKelasDetails : Kelas
    lateinit var mKelasDocumentId : String
    private var mMateriListPosition = -1
    private lateinit var mMateriFileList: ArrayList<MateriFile>

    private var mSelectedImageFileUri : Uri? = null
    private var mMateriImageURL: String = ""

    private var mSelectedFileUri: Uri? = null
    private var mFileType: String? = ""
    private var mFileName: String? = ""
    private var mStorageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMateriDetailsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)


        getIntentData()
        setupActionBar()
        FirestoreClass().getKelasDetails(this,mKelasDocumentId)
        populateMateriDesc()

        val currentUserID = FirestoreClass().getCurrentUserID()
        if (currentUserID.isNotEmpty()) {
            FirestoreClass().getUserRole(currentUserID) { role ->
                if (role == "siswa") {
                    binding?.etMateri?.visibility = View.GONE
//                    binding?.etMateri?.inputType = InputType.TYPE_NULL
                    binding?.btnBold?.visibility = View.GONE
                    binding?.btnItalic?.visibility = View.GONE
                    binding?.btnUploadImage?.visibility = View.GONE
                    binding?.btnUpdateText?.visibility = View.GONE
                    binding?.btnUploadFile?.visibility = View.GONE
                    binding?.tvMateri?.visibility = View.VISIBLE

                    binding?.tvMateri?.text =
                        mKelasDetails.materiList[mMateriListPosition].desc
                }
            }
        }

        if (mKelasDetails.materiList[mMateriListPosition].image.isEmpty()){
            binding?.llImageMateri?.visibility = View.GONE
        }

        binding?.btnUpdateText?.setOnClickListener {
            val newDesc = binding?.etMateri?.text.toString()
            updateMateriContent(newDesc)
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

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(intent, Constants.PICK_FILE_REQUEST_CODE)

            FirestoreClass().getKelasDetails(this,mKelasDocumentId)
            populateMateriFileListToUI(mKelasDetails.materiList[mMateriListPosition].materiFile)

        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbar)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp)
            supportActionBar?.title = "Daftar Kelompok ${mKelasDetails.materiList[mMateriListPosition].nama}"
        }
        binding?.toolbar?.setNavigationOnClickListener {
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }
        }else{
            Toast.makeText(this,"kamu menolak izin storage, aktifkan di setting", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this@MateriDetailsActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding?.ivImageMateri!!)

                binding?.llImageMateri?.visibility = View.VISIBLE

                // Panggil fungsi untuk upload gambar
                uploadMateriImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (requestCode == Constants.PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // Get selected file URI

            mSelectedFileUri = data.data
            mFileType = getFileType(mSelectedFileUri)
            mFileName =  getFileName(mSelectedFileUri)
            mSelectedFileUri?.let { uploadFileToFirebase(it) }


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

    fun kelasDetails(kelas: Kelas){
        mKelasDetails = kelas
//        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.mohon_tunggu))

        populateMateriFileListToUI(kelas.materiList[mMateriListPosition].materiFile)
        }

    private fun applyStyle(style: Int) {
        val start = binding?.etMateri?.selectionStart
        val end = binding?.etMateri?.selectionEnd
        val spannableString = SpannableStringBuilder(binding?.etMateri?.text)
        if (start != null) {
            if (end != null) {
                spannableString.setSpan(StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        binding?.etMateri?.setText(spannableString)
        if (end != null) {
            binding?.etMateri?.setSelection(end)
        }
    }

    fun populateMateriFileListToUI(materiFileList: ArrayList<MateriFile>){
        mMateriFileList = materiFileList

        val rvMateriList : RecyclerView = findViewById(R.id.rv_materi_file_list)

        hideProgressDialog()

        if (materiFileList.isNotEmpty()){
            rvMateriList.visibility = View.VISIBLE

            rvMateriList.layoutManager = LinearLayoutManager(this)
            rvMateriList.setHasFixedSize(true)

            val adapter = MateriFileItemsAdapter(this, materiFileList)
            rvMateriList.adapter = adapter

            adapter.setOnClickListener(object: MateriFileItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: MateriFile) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(model.url)
                    startActivity(intent)
                }
            })
        } else {
            rvMateriList.visibility = View.GONE
        }
    }

    private fun updateMateriContent(newDesc: String) {
        if (::mKelasDetails.isInitialized && mMateriListPosition != -1) {
            mKelasDetails.materiList[mMateriListPosition].desc = newDesc
            updateMateriInFirestore()
        }
    }

    private fun updateMateriInFirestore() {
        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        FirestoreClass().updateMateriDetail(
            this@MateriDetailsActivity,
            mKelasDocumentId,
            mKelasDetails.materiList[mMateriListPosition]
        )
    }

    fun materiUpdateSuccess() {
        hideProgressDialog()
        setResult(RESULT_OK)
        Toast.makeText(this, "Deskripsi materi berhasil diperbarui", Toast.LENGTH_SHORT).show()
    }

    fun populateMateriDesc() {
        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        binding?.etMateri?.setText(mKelasDetails.materiList[mMateriListPosition].desc)

        if (mKelasDetails.materiList[mMateriListPosition].image.isNotEmpty()) {
            binding?.llImageMateri?.visibility = View.VISIBLE
            Glide
                .with(this@MateriDetailsActivity)
                .load(mKelasDetails.materiList[mMateriListPosition].image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(binding?.ivImageMateri!!)
        } else {
            binding?.llImageMateri?.visibility = View.GONE
        }

        materiUpdateSuccess()
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

                    // Update Materi dengan URL gambar baru
                    updateMateriWithImage()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this@MateriDetailsActivity,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()
                hideProgressDialog()
            }
        }
    }

    private fun updateMateriWithImage() {
        mKelasDetails.materiList[mMateriListPosition].image = mMateriImageURL
        updateMateriInFirestore()
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

    private fun uploadFileToFirebase(fileUri: Uri) {
        showProgressDialog("Uploading File...")
        mStorageReference = FirebaseStorage.getInstance().reference
        val fileName = UUID.randomUUID().toString()
        val fileRef = mStorageReference!!.child(Constants.MATERIFILE).child(fileName)

        fileRef.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->
                hideProgressDialog()
                Toast.makeText(this, "Upload Success", Toast.LENGTH_LONG).show()

                // Get the download URL for the uploaded file
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val downloadUrl = downloadUri.toString()
                    // Create Materi object with the download URL
                    val materiFile = MateriFile(
                        name = mFileName.toString(),
                        url = downloadUrl,
                        fileType = mFileType.toString()
                    )

                    mKelasDetails.materiList[mMateriListPosition].materiFile.add(0,materiFile)

                    // Notify the adapter of the new item
                    val adapter = rv_materi_file_list.adapter as MateriFileItemsAdapter
                    adapter.notifyItemInserted(0)
                    rv_materi_file_list.scrollToPosition(0)

                    updateMateriInFirestore()
                }
            }
            .addOnFailureListener { exception ->
                hideProgressDialog()
                Toast.makeText(this, "Failed to upload file: ${exception.message}", Toast.LENGTH_LONG).show()
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

    fun fileUpdateSuccess() {
        hideProgressDialog()
        setResult(RESULT_OK)
        Toast.makeText(this, "Deskripsi materi berhasil diperbarui", Toast.LENGTH_SHORT).show()
    }

}