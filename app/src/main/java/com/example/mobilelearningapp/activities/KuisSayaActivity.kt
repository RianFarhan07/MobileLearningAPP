package com.example.mobilelearningapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilelearningapp.JawabTugasItemsAdapter
import com.example.mobilelearningapp.databinding.ActivityKuisSayaBinding
import com.example.mobilelearningapp.JawabanKuisItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.*
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class KuisSayaActivity : BaseActivity() {

    private var binding : ActivityKuisSayaBinding? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserId: String
    private var kelasList: ArrayList<Kelas> = ArrayList()
    private lateinit var jawabAdapter: JawabanKuisItemsAdapter
    private var allJawaban: ArrayList<JawabanKuis> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityKuisSayaBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        db = FirebaseFirestore.getInstance()
        currentUserId = FirestoreClass().getCurrentUserID()
        fetchUserTasks()
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarMyKuisListActivity)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)

            supportActionBar?.title = "Daftar Kuis Anda"

        }

        binding?.toolbarMyKuisListActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding?.rvKuisSayaList?.layoutManager = LinearLayoutManager(this)
        jawabAdapter = JawabanKuisItemsAdapter(this, allJawaban)
        jawabAdapter.setOnClickListener(object : JawabanKuisItemsAdapter.OnClickListener {
            override fun onClick(position: Int, model: JawabanKuis) {
                onJawabanClicked(position, model)
            }
        })
        binding?.rvKuisSayaList?.adapter = jawabAdapter
        hideProgressDialog()
    }

    private fun fetchUserTasks() {
        db.collection(Constants.KELAS)
            .get()
            .addOnSuccessListener { kelasSnapshot ->
                for (kelasDoc in kelasSnapshot.documents) {
                    val kelas = kelasDoc.toObject<Kelas>()
                    kelas?.let {
                        it.documentId = kelasDoc.id
                        kelasList.add(it)
                        processKelas(it)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("TugasSayaActivity", "Error fetching kelas: ", e)
            }
    }

    private fun processKelas(kelas: Kelas) {
        for (materi in kelas.materiList) {
            processKuisForMateri(kelas, materi)
        }
    }

    private fun processKuisForMateri(kelas: Kelas, materi: Materi) {
        Log.d("KuisSayaActivity", "Processing materi: ${materi.nama}, Tugas count: ${materi.kuis.size}")
        for (kuis in materi.kuis) {
            processJawabanForKuis(kelas, materi, kuis)
        }
    }

    private fun processJawabanForKuis(kelas: Kelas, materi: Materi, kuis: Kuis) {
        Log.d("KuisSayaActivity", "Processing kuis: ${kuis.namaKuis}, Jawaban count: ${kuis.jawab.size}")
        for (jawaban in kuis.jawab) {
            if (jawaban.createdBy == currentUserId) {
                allJawaban.add(jawaban)
                Log.d("KuisSayaActivity", "Added jawaban for user: ${jawaban.namaPenjawab}")
            }
        }
        setupRecyclerView()
        setupActionBar()
    }

    //TODO INI BIKIN SALAHHHH
//    private fun updateUI() {
//        runOnUiThread {
//            if (allJawaban.isEmpty()) {
//                Log.d("TugasSayaActivity", "No jawaban found for current user")
//                binding?.rvTugasSayaList?.visibility = View.GONE
//                binding?.tvEmptyState?.visibility = View.VISIBLE
//            } else {
//                Log.d("TugasSayaActivity", "Updating UI with ${allJawaban.size} jawaban")
//                binding?.rvTugasSayaList?.visibility = View.VISIBLE
//                binding?.tvEmptyState?.visibility = View.GONE
//                jawabAdapter.updateData(allJawaban)
//                binding?.rvTugasSayaList?.post {
//                    jawabAdapter.notifyDataSetChanged()
//                    binding?.rvTugasSayaList?.invalidate()
//                }
//            }
//        }
//    }

    private fun onJawabanClicked(position: Int, jawaban: JawabanKuis) {
        for (kelas in kelasList) {
            for (materi in kelas.materiList) {
                for (kuis in materi.kuis) {
                    if (kuis.jawab.contains(jawaban)) {
                        val intent = Intent(this, MateriDetailsActivity::class.java).apply {
                            putExtra(Constants.MATERI_LIST_ITEM_POSITION, kelas.materiList.indexOf(materi))
                            putExtra(Constants.TUGAS_LIST_ITEM_POSITION, materi.kuis.indexOf(kuis))
                            putExtra(Constants.JAWAB_LIST_ITEM_POSITION, kuis.jawab.indexOf(jawaban))
                            putExtra(Constants.KELAS_DETAIL, kelas)
                            putExtra(Constants.IS_UPDATE, true)
                            putExtra(Constants.DOCUMENT_ID, kelas.documentId)
                        }
                        startActivity(intent)
                        return
                    }
                }
            }
        }
    }
}