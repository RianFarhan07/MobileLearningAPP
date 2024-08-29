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
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.MateriItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.TugasItemsAdapter
import com.example.mobilelearningapp.adapters.MateriFileItemsAdapter
import com.example.mobilelearningapp.databinding.ActivityMateriDetailsBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.File
import com.example.mobilelearningapp.models.Materi
import com.example.mobilelearningapp.models.Tugas
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
    private var mMateriListPosition = -1
    private lateinit var mFileList: ArrayList<File>

    private var mSelectedImageFileUri : Uri? = null
    private var mMateriImageURL: String = ""

    private var mSelectedFileUri: Uri? = null
    private var mFileType: String? = ""
    private var mFileName: String? = ""
    private var mStorageReference: StorageReference? = null

    companion object{
        const val REQUEST_CODE_TUGAS_DETAILS = 8
    }

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
            populateMateriFileListToUI(mKelasDetails.materiList[mMateriListPosition].file)

        }

        binding?.btnTugas?.setOnClickListener {
            showTugasDialog()
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
//            showTugasDialog()
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

            val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val fileToDelete = fileList[position]

                    val dialogView = LayoutInflater.from(this@MateriDetailsActivity).inflate(R.layout.dialog_confirm_delete, null)
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

    fun addUpdateMateriListSuccess(){
        setResult(RESULT_OK)
        Toast.makeText(this, " tugas berhasil ditambah", Toast.LENGTH_SHORT).show()

    }



}