package com.example.mobilelearningapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class TugasSayaActivity : BaseActivity() {

    private var binding: ActivityTugasSayaBinding? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserId: String
    private var kelasList: ArrayList<Kelas> = ArrayList()
    private lateinit var jawabAdapter: JawabTugasItemsAdapter
    private var allJawaban: ArrayList<JawabanTugas> = ArrayList()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

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
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_navigation_menu)

            supportActionBar?.title = "Daftar Tugas Anda"

        }

        binding?.toolbarMyTaskListActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    private fun setupNavigationDrawer() {
        drawerLayout = binding?.drawerLayout!!
        val navView: NavigationView = binding!!.navView

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding!!.toolbarMyTaskListActivity,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_beranda -> {
                    startActivity(Intent(this, MainActivitySiswa::class.java))
                    finish()
                }
                R.id.nav_my_profile -> {
                    val intent = Intent(this, MyProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_tugas -> {
                    val intent = Intent(this, TugasSayaActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_kuis -> {
                    val intent = Intent(this, KuisSayaActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_sign_out -> {
                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(this, UserChooseActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        Log.d("TugasSayaActivity", "Setting up RecyclerView")
        binding?.rvTugasSayaList?.layoutManager = LinearLayoutManager(this)
        jawabAdapter = JawabTugasItemsAdapter(this, allJawaban)
        jawabAdapter.setOnClickListener(object : JawabTugasItemsAdapter.OnClickListener {
            override fun onClick( model: JawabanTugas) {

                onJawabanClicked(model)
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
        setupNavigationDrawer()
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

    private fun onJawabanClicked(jawaban: JawabanTugas) {
        for (kelas in kelasList) {
            for (materi in kelas.materiList) {
                for ((tugasIndex, tugas) in materi.tugas.withIndex()) {
                    val jawabanIndex = tugas.jawab.indexOfFirst { it.id == jawaban.id }
                    if (jawabanIndex != -1) {
                        val intent = Intent(this, MateriDetailsActivity::class.java).apply {
                            putExtra(Constants.MATERI_ID, materi.id)
                            putExtra(Constants.TUGAS_LIST_ITEM_POSITION, tugasIndex)
                            putExtra(Constants.JAWAB_LIST_ITEM_POSITION, jawabanIndex)
                            putExtra(Constants.MATERI_DETAIL, materi)
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