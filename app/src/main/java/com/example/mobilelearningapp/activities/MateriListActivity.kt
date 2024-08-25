package com.example.mobilelearningapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityMateriListBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.utils.Constants

class MateriListActivity : BaseActivity() {

    private var binding : ActivityMateriListBinding? = null
    private lateinit var mKelasDetails : Kelas
    private lateinit var mKelasDocumentId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMateriListBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            mKelasDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
            Log.e("document","document $mKelasDocumentId")
        }
        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        FirestoreClass().getKelasDetails(this,mKelasDocumentId)

        val currentUserID = FirestoreClass().getCurrentUserID()
        if (currentUserID.isNotEmpty()) {
            FirestoreClass().getUserRole(currentUserID) { role ->
                if (role == "siswa") {
                    binding?.tvAddTugas?.visibility = View.GONE
                }
            }
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarMateriListActivity)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            supportActionBar?.title = "Daftar Tugas ${mKelasDetails.nama}"
        }
        binding?.toolbarMateriListActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun kelasDetails(kelas: Kelas){
        mKelasDetails = kelas

        setupActionBar()
        hideProgressDialog()

    }



}