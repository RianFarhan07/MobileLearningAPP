package com.example.mobilelearningapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.JawabTugasItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityJawabanTugasListBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.JawabanTugas
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.utils.Constants
import com.example.mobilelearningapp.utils.SwipeToDeleteCallback
import com.google.firebase.storage.StorageReference
import java.io.IOException

class JawabanListActivity : BaseActivity() {

    private var binding : ActivityJawabanTugasListBinding? = null
    private lateinit var mKelasDetails : Kelas
    lateinit var mKelasDocumentId : String
    private var mMateriListPosition = -1
    private var mTugasListPosition = -1

    private lateinit var mJawabList: java.util.ArrayList<JawabanTugas>

    companion object {
        const val REQUEST_CODE_JAWAB_DETAILS = 22
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityJawabanTugasListBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        getIntentData()
        setupActionBar()
        FirestoreClass().getKelasDetails(this, mKelasDocumentId)

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
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarJawabanListActivity)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)

            supportActionBar?.title = "Tugas ${mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition].namaTugas}"

        }
        binding?.toolbarJawabanListActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_JAWAB_DETAILS && resultCode == RESULT_OK) {
            showProgressDialog(resources.getString(R.string.mohon_tunggu))

            FirestoreClass().getKelasDetails(this, mKelasDocumentId)

        }
    }

    fun kelasDetails(kelas: Kelas){
        mKelasDetails = kelas

        setupActionBar()
        populateJawabListToUI(mKelasDetails.materiList[mMateriListPosition].tugas[mTugasListPosition].jawab)
        hideProgressDialog()

    }

    fun populateJawabListToUI(jawabList: ArrayList<JawabanTugas>) {
        mJawabList = jawabList


        val rvJawabList: RecyclerView = findViewById(R.id.rv_jawaban_list)
        val tvNoJawaban : TextView = findViewById(R.id.tv_no_jawaban_available)

        if (mJawabList.isNotEmpty()) {
            rvJawabList.visibility = View.VISIBLE
            tvNoJawaban.visibility = View.GONE
            rvJawabList.layoutManager = LinearLayoutManager(this)
            rvJawabList.setHasFixedSize(true)

            val adapter = JawabTugasItemsAdapter(this, mJawabList)
            rvJawabList.adapter = adapter

            adapter.setOnDeleteClickListener(object : JawabTugasItemsAdapter.OnDeleteClickListener {
                override fun onDeleteClick(position: Int) {
                    val jawabToDelete = jawabList[position]

                    val dialogView = LayoutInflater.from(this@JawabanListActivity).inflate(R.layout.dialog_confirm_delete, null)
                    val dialog = AlertDialog.Builder(this@JawabanListActivity)
                        .setView(dialogView)
                        .create()

                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()

                    val tvYa = dialogView.findViewById<TextView>(R.id.tv_ya)
                    val tvTidak = dialogView.findViewById<TextView>(R.id.tv_tidak)

                    tvYa.setOnClickListener {
                        showProgressDialog(resources.getString(R.string.mohon_tunggu))
                        FirestoreClass().deleteJawabTugasForGuru(
                            this@JawabanListActivity,
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
            })

            adapter.setOnClickListener(object: JawabTugasItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: JawabanTugas) {
                    jawabanDetails(position)
                }
            })

        } else {
            rvJawabList.visibility = View.GONE
            tvNoJawaban.visibility = View.VISIBLE
        }
    }

    fun jawabTugasDeleteSuccess() {
        setResult(RESULT_OK)
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
}