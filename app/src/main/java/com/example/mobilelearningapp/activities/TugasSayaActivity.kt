package com.example.mobilelearningapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilelearningapp.JawabTugasItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityTugasSayaBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.JawabanTugas
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Materi
import com.example.mobilelearningapp.models.Tugas
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class TugasSayaActivity : BaseActivity() {

    private var binding: ActivityTugasSayaBinding? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserId: String
    private var kelasList: ArrayList<Kelas> = ArrayList()
    private lateinit var jawabAdapter: JawabTugasItemsAdapter
    private var allJawaban: ArrayList<JawabanTugas> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTugasSayaBinding.inflate(layoutInflater)
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

            supportActionBar?.title = "Daftar Tugas Anda"

        }

        binding?.toolbarMyTaskListActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        Log.d("TugasSayaActivity", "Setting up RecyclerView")
        binding?.rvTugasSayaList?.layoutManager = LinearLayoutManager(this)
        jawabAdapter = JawabTugasItemsAdapter(this, allJawaban)
        jawabAdapter.setOnClickListener(object : JawabTugasItemsAdapter.OnClickListener {
            override fun onClick(position: Int, model: JawabanTugas) {
                Log.d("TugasSayaActivity", "Clicked on item at position $position")
                onJawabanClicked(position, model)
            }
        })
        binding?.rvTugasSayaList?.adapter = jawabAdapter
        hideProgressDialog()
        Log.d("TugasSayaActivity", "RecyclerView setup complete")
    }

    private fun fetchUserTasks() {
        db.collection(Constants.KELAS)
            .get()
            .addOnSuccessListener { kelasSnapshot ->
                Log.d("TugasSayaActivity", "Fetched ${kelasSnapshot.size()} kelas")
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
        Log.d("TugasSayaActivity", "Processing kelas: ${kelas.nama}, Materi count: ${kelas.materiList.size}")
        for (materi in kelas.materiList) {
            processTugasForMateri(kelas, materi)
        }
    }

    private fun processTugasForMateri(kelas: Kelas, materi: Materi) {
        Log.d("TugasSayaActivity", "Processing materi: ${materi.nama}, Tugas count: ${materi.tugas.size}")
        for (tugas in materi.tugas) {
            processJawabanForTugas(kelas, materi, tugas)
        }
    }

    private fun processJawabanForTugas(kelas: Kelas, materi: Materi, tugas: Tugas) {
        Log.d("TugasSayaActivity", "Processing tugas: ${tugas.namaTugas}, Jawaban count: ${tugas.jawab.size}")
        for (jawaban in tugas.jawab) {
            if (jawaban.createdBy == currentUserId) {
                allJawaban.add(jawaban)
                Log.d("TugasSayaActivity", "Added jawaban for user: ${jawaban.namaPenjawab}")
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

    private fun onJawabanClicked(position: Int, jawaban: JawabanTugas) {
        for (kelas in kelasList) {
            for (materi in kelas.materiList) {
                for (tugas in materi.tugas) {
                    if (tugas.jawab.contains(jawaban)) {
                        val intent = Intent(this, MateriDetailsActivity::class.java).apply {
                            putExtra(Constants.MATERI_LIST_ITEM_POSITION, kelas.materiList.indexOf(materi))
                            putExtra(Constants.TUGAS_LIST_ITEM_POSITION, materi.tugas.indexOf(tugas))
                            putExtra(Constants.JAWAB_LIST_ITEM_POSITION, tugas.jawab.indexOf(jawaban))
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