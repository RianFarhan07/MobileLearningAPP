package com.example.mobilelearningapp.activities

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.adapters.MateriFileItemsAdapter
import com.example.mobilelearningapp.databinding.ActivityGuruProfileBinding
import com.example.mobilelearningapp.databinding.ActivityMateriDetailsBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.MateriFile
import com.example.mobilelearningapp.utils.Constants

class MateriDetailsActivity : BaseActivity() {

    private var binding : ActivityMateriDetailsBinding? = null
    private lateinit var mKelasDetails : Kelas
    lateinit var mKelasDocumentId : String
    private var mMateriListPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMateriDetailsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)


        getIntentData()
        setupActionBar()
        FirestoreClass().getKelasDetails(this,mKelasDocumentId)
        populateMateriDesc()


        if (mKelasDetails.materiList[mMateriListPosition].image.isEmpty()){
            binding?.llImageMateri?.visibility = View.GONE
        }

        binding?.btnUpdateText?.setOnClickListener {
            val newDesc = binding?.etMateri?.text.toString()
            updateMateriContent(newDesc)
        }

        binding?.btnBold?.setOnClickListener { applyStyle(Typeface.BOLD) }
        binding?.btnItalic?.setOnClickListener { applyStyle(Typeface.ITALIC)}
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

    fun populateMateriFileListToUI(materiFileList: ArrayList<MateriFile>){

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
        FirestoreClass().updateMateriDetail(this@MateriDetailsActivity, mKelasDocumentId, mKelasDetails.materiList[mMateriListPosition])
    }

    fun materiUpdateSuccess() {
        hideProgressDialog()
        setResult(RESULT_OK)
        Toast.makeText(this, "Deskripsi materi berhasil diperbarui", Toast.LENGTH_SHORT).show()
    }

    fun populateMateriDesc() {
        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        binding?.etMateri?.setText(mKelasDetails.materiList[mMateriListPosition].desc)
        materiUpdateSuccess()
    }



}