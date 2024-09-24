package com.example.mobilelearningapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilelearningapp.KuisItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.TugasItemsAdapter
import com.example.mobilelearningapp.databinding.ActivityGuruKuisSayaBinding
import com.example.mobilelearningapp.databinding.ActivityGuruTugasSayaBinding
import com.example.mobilelearningapp.databinding.ActivityTugasSayaBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Kuis
import com.example.mobilelearningapp.models.Materi
import com.example.mobilelearningapp.models.Tugas
import com.example.mobilelearningapp.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class GuruTugasSayaActivity : BaseActivity() {

    private var binding : ActivityGuruTugasSayaBinding? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserId: String
    private var kelasList: ArrayList<Kelas> = ArrayList()
    private lateinit var tugasAdapter: TugasItemsAdapter
    private var allTugas: ArrayList<Tugas> = ArrayList()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityGuruTugasSayaBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        db = FirebaseFirestore.getInstance()
        currentUserId = FirestoreClass().getCurrentUserID()
        fetchUserTasks()

    }

    private fun setupActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_my_tugas_guru_list_activity)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_navigation_menu)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.title = "DAFTAR TUGAS"
        toolbar.title = "DAFTAR TUGAS"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    private fun setupNavigationDrawer() {
        drawerLayout = binding?.drawerLayout!!
        val navView: NavigationView = binding!!.navView

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding!!.toolbarMyTugasGuruListActivity,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_beranda -> {
                    startActivity(Intent(this, MainGuruActivity::class.java))
                    finish()
                }
                R.id.nav_guru_profile -> {
                    val intent = Intent(this, GuruProfileActivity::class.java)
                    startActivityForResult(intent, MainGuruActivity.GURU_PROFILE_REQUEST_CODE)
                }

                R.id.nav_buat_kelas ->{
                    Toast.makeText(this,"Silahkan kembali ke beranda untuk membuat tugas",
                        Toast.LENGTH_LONG).show()
                }
                R.id.nav_kuis ->{
                    startActivity(Intent(this, GuruKuisSayaActivity::class.java))
                    finish()
                }
                R.id.nav_tugas->{
                    startActivity(Intent(this, GuruTugasSayaActivity::class.java))
                    finish()
                }
                R.id.nav_sign_out->{
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
        binding?.rvGuruTugasSayaList?.layoutManager = LinearLayoutManager(this)
        tugasAdapter = TugasItemsAdapter(this, allTugas)
        tugasAdapter.setOnClickListener(object : TugasItemsAdapter.OnClickListener {
            override fun onClick(position: Int, model: Tugas) {
                onTugasClicked(position, model)
            }
        })
        binding?.rvGuruTugasSayaList?.adapter = tugasAdapter
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
        setupNavigationDrawer()
    }

    private fun onTugasClicked(position: Int, tugas: Tugas) {
        for (kelas in kelasList) {
            for ((materiIndex,materi) in kelas.materiList.withIndex()) {
                val tugasIndex = materi.tugas.indexOfFirst { it.id == tugas.id }
                if (tugasIndex != -1) {
                    val intent = Intent(this, MateriDetailsActivity::class.java).apply {
                        putExtra(Constants.MATERI_ID, materi.id)
                        // atau gunakan ini jika Anda lebih suka menggunakan posisi
                        // putExtra(Constants.MATERI_LIST_ITEM_POSITION, materiIndex)
                        putExtra(Constants.TUGAS_LIST_ITEM_POSITION, tugasIndex)
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
