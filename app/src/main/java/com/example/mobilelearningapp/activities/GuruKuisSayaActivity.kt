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
import com.example.mobilelearningapp.databinding.ActivityGuruKuisSayaBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Kuis
import com.example.mobilelearningapp.models.Materi
import com.example.mobilelearningapp.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class GuruKuisSayaActivity : BaseActivity() {

    private var binding: ActivityGuruKuisSayaBinding? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserId: String
    private var kelasList: ArrayList<Kelas> = ArrayList()
    private lateinit var kuisAdapter: KuisItemsAdapter
    private var allKuis: ArrayList<Kuis> = ArrayList()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityGuruKuisSayaBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        db = FirebaseFirestore.getInstance()
        currentUserId = FirestoreClass().getCurrentUserID()
        fetchUserQuizzes()
    }

    private fun setupActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_my_kuis_guru_list_activity)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_navigation_menu)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.title = "DAFTAR KUIS"
        toolbar.title = "DAFTAR KUIS"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    private fun setupNavigationDrawer() {
        drawerLayout = binding?.drawerLayout!!
        val navView: NavigationView = binding!!.navView

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding!!.toolbarMyKuisGuruListActivity,
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
                R.id.nav_buat_kelas -> {
                    Toast.makeText(this, "Silahkan kembali ke beranda untuk membuat kuis",
                        Toast.LENGTH_LONG).show()
                }
                R.id.nav_kuis -> {
                    startActivity(Intent(this, GuruKuisSayaActivity::class.java))
                    finish()
                }
                R.id.nav_tugas -> {
                    startActivity(Intent(this, GuruTugasSayaActivity::class.java))
                    finish()
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
        binding?.rvGuruKuisSayaList?.layoutManager = LinearLayoutManager(this)
        kuisAdapter = KuisItemsAdapter(this, allKuis)
        kuisAdapter.setOnClickListener(object : KuisItemsAdapter.OnClickListener {
            override fun onClick(position: Int, model: Kuis) {
                onKuisClicked(position, model)
            }
        })
        Log.e("KUIS", allKuis.toString())
        binding?.rvGuruKuisSayaList?.adapter = kuisAdapter
        hideProgressDialog()
    }

    private fun fetchUserQuizzes() {
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
                Log.e("KuisSayaActivity", "Error fetching kelas: ", e)
            }
    }

    private fun processKelas(kelas: Kelas) {
        for (materi in kelas.materiList) {
            processKuisForMateri(kelas, materi)
        }
    }

    private fun processKuisForMateri(kelas: Kelas, materi: Materi) {
        Log.d("KuisSayaActivity", "Processing materi: ${materi.nama}, Kuis count: ${materi.kuis.size}")
        for (kuis in materi.kuis) {
            //salah di created by

            allKuis.add(kuis)
            Log.d("KuisSayaActivity", "Added kuis for user: ${kuis.namaKuis}")
        }
        setupRecyclerView()
        setupActionBar()
        setupNavigationDrawer()
    }

    private fun onKuisClicked(position: Int, kuis: Kuis) {
        for (kelas in kelasList) {
            for ((materiIndex, materi) in kelas.materiList.withIndex()) {
                val kuisIndex = materi.kuis.indexOfFirst { it.id == kuis.id }
                if (kuisIndex != -1) {
                    val intent = Intent(this, MateriDetailsActivity::class.java).apply {
                        putExtra(Constants.MATERI_ID, materi.id)
                        putExtra(Constants.QUIZ_LIST_ITEM_POSITION, kuisIndex)
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
