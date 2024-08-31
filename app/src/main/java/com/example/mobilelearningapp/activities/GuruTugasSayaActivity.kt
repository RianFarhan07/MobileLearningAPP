package com.example.mobilelearningapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilelearningapp.KuisItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.TugasItemsAdapter
import com.example.mobilelearningapp.databinding.ActivityGuruKuisSayaBinding
import com.example.mobilelearningapp.databinding.ActivityTugasSayaBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Kuis
import com.example.mobilelearningapp.models.Materi
import com.example.mobilelearningapp.models.Tugas
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class GuruTugasSayaActivity : BaseActivity() {

    private var binding : ActivityTugasSayaBinding? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserId: String
    private var kelasList: ArrayList<Kelas> = ArrayList()
    private lateinit var tugasAdapter: TugasItemsAdapter
    private var allTugas: ArrayList<Tugas> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTugasSayaBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        db = FirebaseFirestore.getInstance()
        currentUserId = FirestoreClass().getCurrentUserID()
        fetchUserTasks()

    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarMyTaskListActivity)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)

            supportActionBar?.title = "Daftar Kuis Anda"

        }

        binding?.toolbarMyTaskListActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding?.rvTugasSayaList?.layoutManager = LinearLayoutManager(this)
        tugasAdapter = TugasItemsAdapter(this, allTugas)
        tugasAdapter.setOnClickListener(object : TugasItemsAdapter.OnClickListener {
            override fun onClick(position: Int, model: Tugas) {
                onTugasClicked(position, model)
            }
        })
        binding?.rvTugasSayaList?.adapter = tugasAdapter
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
            processTugasForMateri(kelas, materi)
        }
    }

    private fun processTugasForMateri(kelas: Kelas, materi: Materi) {
        Log.d("KuisSayaActivity", "Processing materi: ${materi.nama}, Tugas count: ${materi.kuis.size}")
        for (tugas in materi.tugas) {
            if (tugas.createdBy == currentUserId) {
                allTugas.add(tugas)
                Log.d("KuisSayaActivity", "Added jawaban for user: ${tugas.namaTugas}")
            }
        }
        setupRecyclerView()
        setupActionBar()
    }

    private fun onTugasClicked(position: Int, tugas: Tugas) {
        for (kelas in kelasList) {
            for (materi in kelas.materiList) {
                if (materi.tugas.contains(tugas)) {
                    val intent = Intent(this, MateriDetailsActivity::class.java).apply {
                        putExtra(Constants.MATERI_LIST_ITEM_POSITION, kelas.materiList.indexOf(materi))
                        putExtra(Constants.TUGAS_LIST_ITEM_POSITION, materi.tugas.indexOf(tugas))
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
