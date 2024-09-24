package com.example.mobilelearningapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.*
import android.text.method.ScrollingMovementMethod
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.KuisItemsAdapter
import com.example.mobilelearningapp.MateriItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.TugasItemsAdapter
import com.example.mobilelearningapp.adapters.MateriFileItemsAdapter
import com.example.mobilelearningapp.databinding.ActivityMateriDetailsBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.*
import com.example.mobilelearningapp.utils.Constants
import com.example.mobilelearningapp.utils.SwipeToDeleteCallback
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_materi_details.*
import kotlinx.android.synthetic.main.dialog_tugas.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MateriDetailsActivity : BaseActivity() {

    private var binding : ActivityMateriDetailsBinding? = null
    private lateinit var mKelasDetails : Kelas
    lateinit var mKelasDocumentId : String

    private lateinit var mMateriId: String

    private var mMateriListPosition = -1
        get() = findMateriIndexById(mMateriId)

    private lateinit var mFileList: ArrayList<File>

    private var mSelectedImageFileUri : Uri? = null
    private var mMateriImageURL: String = ""

    private var mSelectedVideoFileUri : Uri? = null
    private var mMateriVideoURL: String = ""

    private var mSelectedFileUri: Uri? = null
    private var mFileType: String? = ""
    private var mFileName: String? = ""
    private var mStorageReference: StorageReference? = null

    companion object{
        const val REQUEST_CODE_TUGAS_DETAILS = 8
        const val REQUEST_CODE_QUIZ_DETAILS = 9
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMateriDetailsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        showProgressDialog(resources.getString(R.string.mohon_tunggu))
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
                    binding?.btnUploadVideo?.visibility = View.GONE
                    binding?.btnUploadFile?.visibility = View.GONE
                    binding?.btnDeleteImage?.visibility = View.GONE
                    binding?.btnDeleteVideo?.visibility = View.GONE
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
            populateMateriFileListToUI(mKelasDetails.materiList[mMateriListPosition].file)

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
                    binding?.llImageMateri?.visibility = View.GONE
                    deleteMateriImage()
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
                    deleteMateriVideo()
                    dialog.dismiss()
                }
                .setNegativeButton("Tidak") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding?.btnTugas?.setOnClickListener {
            showTugasDialog()
        }

        binding?.btnKuis?.setOnClickListener {
            showQuizDialog()
        }

        binding?.llVideoMateri?.setOnClickListener {
            if (mKelasDetails.materiList[mMateriListPosition].video.isNotEmpty()) {
                val intent = Intent(this, VideoPlayerActivity::class.java)
                intent.putExtra("VIDEO_URL", mKelasDetails.materiList[mMateriListPosition].video)
                startActivity(intent)
            }
        }
    }



    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbar)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
//            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp)
            supportActionBar?.title = "Materi ${mKelasDetails.materiList[mMateriListPosition].nama}"
        }
        binding?.toolbar?.setNavigationOnClickListener {
            onBackPressed()

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
            showProgressDialog("Uploading Image")
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
        if (requestCode == REQUEST_CODE_TUGAS_DETAILS && resultCode == RESULT_OK) {
            showProgressDialog(resources.getString(R.string.mohon_tunggu))
            FirestoreClass().getKelasDetails(this, mKelasDocumentId)
        }
        if (requestCode == REQUEST_CODE_QUIZ_DETAILS && resultCode == RESULT_OK) {
            showProgressDialog(resources.getString(R.string.mohon_tunggu))
            FirestoreClass().getKelasDetails(this, mKelasDocumentId)
        }

        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_VIDEO_REQUEST_CODE
            && data!!.data != null
        ) {

            mSelectedVideoFileUri = data.data

            try {
                Glide
                    .with(this@MateriDetailsActivity)
                    .load(mSelectedVideoFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding?.ivVideoMateri!!)

                binding?.llVideoMateri?.visibility = View.VISIBLE
                binding?.llUploadProgress?.visibility = View.VISIBLE
                uploadMateriVideo()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun findMateriIndexById(id: String): Int {
        return mKelasDetails.materiList.indexOfFirst { it.id == id }
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.KELAS_DETAIL)) {
            mKelasDetails = intent.getParcelableExtra(Constants.KELAS_DETAIL)!!
        }
        if (intent.hasExtra(Constants.MATERI_ID)) {
            mMateriId = intent.getStringExtra(Constants.MATERI_ID)!!
            Log.e("MATERI_ID", mMateriId)
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

        populateMateriFileListToUI(kelas.materiList[mMateriListPosition].file)
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

    fun populateMateriFileListToUI(fileList: ArrayList<File>){
        mFileList = fileList

        val rvMateriFileList : RecyclerView = findViewById(R.id.rv_materi_file_list)

        hideProgressDialog()

        if (fileList.isNotEmpty()){
            rvMateriFileList.visibility = View.VISIBLE

            rvMateriFileList.layoutManager = LinearLayoutManager(this)
            rvMateriFileList.setHasFixedSize(true)

            val adapter = MateriFileItemsAdapter(this, fileList)
            rvMateriFileList.adapter = adapter

            adapter.setOnClickListener(object: MateriFileItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: File) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(model.url)
                    startActivity(intent)
                }
            })

            val currentUserID = FirestoreClass().getCurrentUserID()
            if (currentUserID.isNotEmpty()) {
                FirestoreClass().getUserRole(currentUserID) { role ->
                    if (role == "guru") {
                        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
                            override fun onSwiped(
                                viewHolder: RecyclerView.ViewHolder,
                                direction: Int
                            ) {
                                val position = viewHolder.adapterPosition
                                val fileToDelete = fileList[position]

                                val dialogView = LayoutInflater.from(this@MateriDetailsActivity)
                                    .inflate(R.layout.dialog_confirm_delete, null)
                                val dialog = AlertDialog.Builder(this@MateriDetailsActivity)
                                    .setView(dialogView)
                                    .create()

                                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                dialog.show()

                                val tvYa = dialogView.findViewById<TextView>(R.id.tv_ya)
                                val tvTidak = dialogView.findViewById<TextView>(R.id.tv_tidak)

                                tvYa.setOnClickListener {
                                    showProgressDialog(resources.getString(R.string.mohon_tunggu))
                                    FirestoreClass().deleteFileMateri(
                                        this@MateriDetailsActivity,
                                        mKelasDocumentId,
                                        mMateriListPosition,
                                        fileToDelete.id
                                    )

                                    dialog.dismiss()
                                }

                                tvTidak.setOnClickListener {
                                    dialog.dismiss()
                                }

                            }
                        }

                        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
                        deleteItemTouchHelper.attachToRecyclerView(rvMateriFileList)
                    }
                }
            }



        } else {
            rvMateriFileList.visibility = View.GONE
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
        FirestoreClass().addUpdateMateriList(this, mKelasDetails)
    }

    fun materiUpdateSuccess() {
        hideProgressDialog()
        setResult(RESULT_OK)
        Toast.makeText(this, "Deskripsi materi berhasil diperbarui", Toast.LENGTH_SHORT).show()
    }

    fun populateMateriDesc() {
        hideProgressDialog()
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

        if (mKelasDetails.materiList[mMateriListPosition].video.isNotEmpty()) {
            binding?.llVideoMateri?.visibility = View.VISIBLE
            Glide
                .with(this@MateriDetailsActivity)
                .load(mKelasDetails.materiList[mMateriListPosition].video)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(binding?.ivVideoMateri!!)
        } else {
            binding?.llVideoMateri?.visibility = View.GONE
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
                        hideProgressDialog()

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

    private fun deleteMateriImage() {

        // Get the storage reference of the image
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mKelasDetails.materiList[mMateriListPosition].image)

        // Delete the image from Firebase Storage
        storageRef.delete().addOnSuccessListener {
            // Image deleted successfully from Storage, now update Firestore
            mKelasDetails.materiList[mMateriListPosition].image = ""
            updateMateriInFirestore()
        }.addOnFailureListener { exception ->
            hideProgressDialog()
            Toast.makeText(
                this@MateriDetailsActivity,
                "Error deleting image: ${exception.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun deleteMateriVideo() {

        // Get the storage reference of the image
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mKelasDetails.materiList[mMateriListPosition].video)

        // Delete the image from Firebase Storage
        storageRef.delete().addOnSuccessListener {
            // Image deleted successfully from Storage, now update Firestore
            mKelasDetails.materiList[mMateriListPosition].video = ""
            updateMateriInFirestore()
        }.addOnFailureListener { exception ->
            hideProgressDialog()
            Toast.makeText(
                this@MateriDetailsActivity,
                "Error deleting image: ${exception.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateMateriWithImage() {
        mKelasDetails.materiList[mMateriListPosition].image = mMateriImageURL
        updateMateriInFirestore()
    }



    private fun uploadMateriVideo() {

        if (mSelectedVideoFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "MATERI_VIDEO" + System.currentTimeMillis() + "."
                        + Constants.getFileExtension(this, mSelectedVideoFileUri!!)
            )

            sRef.putFile(mSelectedVideoFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.e(
                    "Firebase Video URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.e("Downloadable Video URL", uri.toString())
                    mMateriVideoURL = uri.toString()
                    binding?.llUploadProgress?.visibility = View.GONE

                    // Update Materi dengan URL gambar baru
                    updateMateriWithVideo()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this@MateriDetailsActivity,
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

    private fun updateMateriWithVideo() {
        mKelasDetails.materiList[mMateriListPosition].video = mMateriVideoURL
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
                    val file = File(
                        name = mFileName.toString(),
                        url = downloadUrl,
                        fileType = mFileType.toString()
                    )

                    mKelasDetails.materiList[mMateriListPosition].file.add(0, file)

                    // Notify the adapter of the new item
                    val adapter = rv_materi_file_list.adapter as? MateriFileItemsAdapter
                    if (adapter != null) {
                        adapter.notifyItemInserted(0)
                        rv_materi_file_list.scrollToPosition(0)
                        hideProgressDialog()
                    } else {
                        populateMateriFileListToUI(mKelasDetails.materiList[mMateriListPosition].file)
                    }

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
       FirestoreClass().getKelasDetails(this, mKelasDocumentId)
        Toast.makeText(this, "Deskripsi materi berhasil diperbarui", Toast.LENGTH_SHORT).show()
    }

    private fun showTugasDialog() {
        val tugasList = mKelasDetails.materiList[mMateriListPosition].tugas
        val dialogView = layoutInflater.inflate(R.layout.dialog_tugas, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

        val dialog = dialogBuilder.create()
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )


        val rvTugasList = dialogView.findViewById<RecyclerView>(R.id.rv_tugas_list)
        val btnBuatTugas = dialogView.findViewById<Button>(R.id.btn_buat_tugas)

        if (tugasList.size >0){
            rvTugasList.visibility = View.VISIBLE

            rvTugasList.layoutManager = LinearLayoutManager(this)
            rvTugasList.setHasFixedSize(true)

            val adapter = TugasItemsAdapter(this@MateriDetailsActivity,tugasList)
            rvTugasList.adapter = adapter

            adapter.setOnClickListener(object: TugasItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Tugas) {
                    tugasDetails(position)
                    dialog.dismiss()
                }
            })

        }else{
            rvTugasList.visibility = View.GONE

        }

        // Tampilkan atau sembunyikan tombol "Buat Tugas" berdasarkan peran pengguna
        val currentUserID = FirestoreClass().getCurrentUserID()
        if (currentUserID.isNotEmpty()) {
            FirestoreClass().getUserRole(currentUserID) { role ->
                if (role == "siswa") {
                  btnBuatTugas?.visibility = View.GONE
                }
            }
        }

        btnBuatTugas.setOnClickListener {
            val intent = Intent(this,TugasActivity::class.java)
            intent.putExtra(Constants.MATERI_LIST_ITEM_POSITION,mMateriListPosition)
            intent.putExtra(Constants.KELAS_DETAIL,mKelasDetails)
            intent.putExtra(Constants.DOCUMENT_ID, mKelasDocumentId)
            startActivityForResult(intent,REQUEST_CODE_TUGAS_DETAILS)
            dialog.dismiss()
        }

        dialog.show()
    }

    fun tugasDetails(tugasPosition: Int){
        val intent = Intent(this, TugasActivity::class.java)
        intent.putExtra(Constants.MATERI_LIST_ITEM_POSITION,mMateriListPosition)
        intent.putExtra(Constants.TUGAS_LIST_ITEM_POSITION,tugasPosition)
        intent.putExtra(Constants.KELAS_DETAIL,mKelasDetails)
        intent.putExtra(Constants.IS_UPDATE, true)
        intent.putExtra(Constants.DOCUMENT_ID, mKelasDocumentId)
        startActivityForResult(intent,REQUEST_CODE_TUGAS_DETAILS)

    }

    fun quizDetails(quizPosition: Int){
        val intent = Intent(this, CreateQuizActivity::class.java)
        intent.putExtra(Constants.MATERI_LIST_ITEM_POSITION,mMateriListPosition)
        intent.putExtra(Constants.QUIZ_LIST_ITEM_POSITION,quizPosition)
        intent.putExtra(Constants.KELAS_DETAIL,mKelasDetails)
        intent.putExtra(Constants.IS_UPDATE, true)
        intent.putExtra(Constants.DOCUMENT_ID, mKelasDocumentId)
        startActivityForResult(intent, REQUEST_CODE_QUIZ_DETAILS)

    }

    fun quizDetailsForSiswa(quizPosition: Int){
        val intent = Intent(this, QuizJawabActivity::class.java)
        intent.putExtra(Constants.MATERI_LIST_ITEM_POSITION,mMateriListPosition)
        intent.putExtra(Constants.QUIZ_LIST_ITEM_POSITION,quizPosition)
        intent.putExtra(Constants.KELAS_DETAIL,mKelasDetails)
        intent.putExtra(Constants.IS_UPDATE, true)
        intent.putExtra(Constants.DOCUMENT_ID, mKelasDocumentId)
        startActivityForResult(intent, REQUEST_CODE_QUIZ_DETAILS)

    }

    fun addUpdateMateriListSuccess(){
        setResult(RESULT_OK)
        Toast.makeText(this, " tugas berhasil ditambah", Toast.LENGTH_SHORT).show()

    }

    private fun showQuizDialog() {
        val quizList = mKelasDetails.materiList[mMateriListPosition].kuis
        val dialogView = layoutInflater.inflate(R.layout.dialog_kuis, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

        val dialog = dialogBuilder.create()

        val rvKuisList = dialogView.findViewById<RecyclerView>(R.id.rv_kuis_list)
        val btnBuatKuis = dialogView.findViewById<Button>(R.id.btn_buat_kuis)

        if (quizList.size >0){
            rvKuisList.visibility = View.VISIBLE

            rvKuisList.layoutManager = LinearLayoutManager(this)
            rvKuisList.setHasFixedSize(true)

            val adapter = KuisItemsAdapter(this@MateriDetailsActivity,quizList)
            rvKuisList.adapter = adapter

            adapter.setOnClickListener(object: KuisItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Kuis) {
                    val currentUserID = FirestoreClass().getCurrentUserID()
                    if (currentUserID.isNotEmpty()) {
                        FirestoreClass().getUserRole(currentUserID) { role ->
                            if (role == "siswa") {
                                checkQuizCompletion(currentUserID, position, model)
                                dialog.dismiss()
                            }else{
                                quizDetails(position)
                                dialog.dismiss()
                            }
                        }
                    }

                }
            })

        }else{
            rvKuisList.visibility = View.GONE

        }

        // Tampilkan atau sembunyikan tombol "Buat Tugas" berdasarkan peran pengguna
        val currentUserID = FirestoreClass().getCurrentUserID()
        if (currentUserID.isNotEmpty()) {
            FirestoreClass().getUserRole(currentUserID) { role ->
                if (role == "siswa") {
                    btnBuatKuis?.visibility = View.GONE
                }
            }
        }
//
        btnBuatKuis.setOnClickListener {
            val intent = Intent(this,CreateQuizActivity::class.java)
            intent.putExtra(Constants.MATERI_LIST_ITEM_POSITION,mMateriListPosition)
            intent.putExtra(Constants.KELAS_DETAIL,mKelasDetails)
            intent.putExtra(Constants.DOCUMENT_ID, mKelasDocumentId)
            startActivityForResult(intent,REQUEST_CODE_QUIZ_DETAILS)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkQuizCompletion(userID: String, position: Int, quiz: Kuis) {
        // Check if the user has already completed the quiz
        val userJawaban = quiz.jawab.find { it.createdBy == userID }

        if (userJawaban != null) {
            // User has already completed the quiz, redirect to ResultActivity
            val intent = Intent(this, ResultKuisActivity::class.java)
            intent.putExtra(Constants.MATERI_LIST_ITEM_POSITION, mMateriListPosition)
            intent.putExtra(Constants.QUIZ_LIST_ITEM_POSITION, position)
            intent.putExtra(Constants.KELAS_DETAIL, mKelasDetails)
            intent.putExtra(Constants.DOCUMENT_ID, mKelasDocumentId)
            intent.putExtra(Constants.IS_UPDATE, true)
            startActivity(intent)
        } else {
            // User hasn't completed the quiz yet, proceed to QuizJawabActivity
            quizDetailsForSiswa(position)
        }
    }

}