package com.example.mobilelearningapp.activities

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelearningapp.JawabTugasItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityJawabanKuisListBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.JawabanKuisItemsAdapter
import com.example.mobilelearningapp.models.JawabanKuis
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.utils.Constants

class JawabanKuisListActivity : BaseActivity() {

    private var binding : ActivityJawabanKuisListBinding? = null
    private lateinit var mKelasDetails : Kelas
    lateinit var mKelasDocumentId : String
    private var mMateriListPosition = -1
    private var mKuisListPosition = -1

    private lateinit var mJawabList: ArrayList<JawabanKuis>

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityJawabanKuisListBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        getIntentData()
        setupActionBar()
        FirestoreClass().getKelasDetails(this,mKelasDocumentId)

    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.KELAS_DETAIL)) {
            mKelasDetails = intent.getParcelableExtra(Constants.KELAS_DETAIL)!!

        }
        if (intent.hasExtra(Constants.MATERI_LIST_ITEM_POSITION)) {
            mMateriListPosition = intent.getIntExtra(Constants.MATERI_LIST_ITEM_POSITION, -1)
            Log.e("MATERI_ITEM_POSITION", mMateriListPosition.toString())
        }
        if (intent.hasExtra(Constants.QUIZ_LIST_ITEM_POSITION)) {
            mKuisListPosition = intent.getIntExtra(Constants.QUIZ_LIST_ITEM_POSITION, -1)
            Log.e("QUIZ_ITEM_POSITION", mKuisListPosition.toString())
        }
        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mKelasDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
            Log.e("document", "document $mKelasDocumentId")
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbar)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
//            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp)
            supportActionBar?.title = "Daftar Jawaban Kuis ${mKelasDetails.materiList[mMateriListPosition].kuis[mKuisListPosition].namaKuis}"
        }
        binding?.toolbar?.setNavigationOnClickListener {
            onBackPressed()

        }
    }

    fun kelasDetails(kelas: Kelas){
        mKelasDetails = kelas

        setupActionBar()
        populateJawabListToUI(mKelasDetails.materiList[mMateriListPosition].kuis[mKuisListPosition].jawab)

    }

    fun populateJawabListToUI(jawabList: ArrayList<JawabanKuis>) {
        mJawabList = jawabList


        val rvJawabList: RecyclerView = findViewById(R.id.rv_jawaban_kuis_list)
        val tvNoJawaban : TextView = findViewById(R.id.tv_no_jawaban_available)

        if (mJawabList.isNotEmpty()) {
            rvJawabList.visibility = View.VISIBLE
            tvNoJawaban.visibility = View.GONE
            rvJawabList.layoutManager = LinearLayoutManager(this)
            rvJawabList.setHasFixedSize(true)

            val adapter = JawabanKuisItemsAdapter(this, mJawabList)
            rvJawabList.adapter = adapter

            adapter.setOnDeleteClickListener(object : JawabanKuisItemsAdapter.OnDeleteClickListener {
                override fun onDeleteClick(position: Int) {
                    val jawabToDelete = jawabList[position]

                    val dialogView = LayoutInflater.from(this@JawabanKuisListActivity).inflate(R.layout.dialog_confirm_delete, null)
                    val dialog = AlertDialog.Builder(this@JawabanKuisListActivity)
                        .setView(dialogView)
                        .create()

                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()

                    val tvYa = dialogView.findViewById<TextView>(R.id.tv_ya)
                    val tvTidak = dialogView.findViewById<TextView>(R.id.tv_tidak)

                    tvYa.setOnClickListener {
                        showProgressDialog(resources.getString(R.string.mohon_tunggu))
                        FirestoreClass().deleteJawabKuisForGuru(
                            this@JawabanKuisListActivity,
                            mKelasDocumentId,
                            mMateriListPosition,
                            mKuisListPosition,
                            jawabToDelete.id
                        )
                        dialog.dismiss()
                    }

                    tvTidak.setOnClickListener {
                        dialog.dismiss()
                    }

                }
            })

        } else {
            rvJawabList.visibility = View.GONE
            tvNoJawaban.visibility = View.VISIBLE
        }
    }

    fun jawabKuisDeleteSuccess() {
        setResult(RESULT_OK)
        hideProgressDialog()
        Toast.makeText(this, "Jawaban kuis berhasil dihapus", Toast.LENGTH_SHORT).show()
        FirestoreClass().getKelasDetails(this, mKelasDocumentId) // Refresh data
    }
}