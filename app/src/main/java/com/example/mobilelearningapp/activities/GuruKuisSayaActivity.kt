package com.example.mobilelearningapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilelearningapp.KuisItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityGuruKuisSayaBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Kuis
import com.example.mobilelearningapp.models.Materi
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class GuruKuisSayaActivity : BaseActivity() {

    private var binding: ActivityGuruKuisSayaBinding? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserId: String
    private var kelasList: ArrayList<Kelas> = ArrayList()
    private lateinit var kuisAdapter: KuisItemsAdapter
    private var allKuis: ArrayList<Kuis> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityGuruKuisSayaBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        db = FirebaseFirestore.getInstance()
        currentUserId = FirestoreClass().getCurrentUserID()
        fetchUserKuis()
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarMyKuisGuruListActivity)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)

            supportActionBar?.title = "Daftar Kuis Anda"

        }

        binding?.toolbarMyKuisGuruListActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding?.rvGuruKuisSayaList?.layoutManager = LinearLayoutManager(this)
        kuisAdapter = KuisItemsAdapter(this, allKuis)
        kuisAdapter.setOnClickListener(object : KuisItemsAdapter.OnClickListener {
            override fun onClick(position: Int, model: Kuis) {
                onKuisClicked(position, model)
            }
        })
        binding?.rvGuruKuisSayaList?.adapter = kuisAdapter
        hideProgressDialog()
    }

    private fun fetchUserKuis() {
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
            if (kuis.createdBy == currentUserId) {
                allKuis.add(kuis)
                Log.d("KuisSayaActivity", "Added jawaban for user: ${kuis.namaKuis}")
            }
        }
    setupRecyclerView()
    setupActionBar()
    }

    private fun onKuisClicked(position: Int, kuis: Kuis) {
        for (kelas in kelasList) {
            for (materi in kelas.materiList) {
                if (materi.kuis.contains(kuis)) {
                    val intent = Intent(this, MateriDetailsActivity::class.java).apply {
                        putExtra(Constants.MATERI_LIST_ITEM_POSITION, kelas.materiList.indexOf(materi))
                        putExtra(Constants.TUGAS_LIST_ITEM_POSITION, materi.kuis.indexOf(kuis))
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

