package com.example.mobilelearningapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.KelasItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityMainSiswaBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Siswa
import com.example.mobilelearningapp.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivitySiswa : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding : ActivityMainSiswaBinding? = null
    private lateinit var mUserName : String
    private lateinit var mSiswaId : String

    companion object{
        const val MY_PROFILE_REQUEST_CODE = 11
        const val UPDATE_KELAS_REQUEST_CODE = 14
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainSiswaBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        setupActionBar()
        showProgressDialog(resources.getString(R.string.mohon_tunggu))

        FirestoreClass().getSiswaDetails(this)
        FirestoreClass().getKelasList(this)

        binding?.navView?.setNavigationItemSelectedListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().getSiswaDetails(this)
        }
        else{
            Log.e("cancelled","cancelled")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logo -> {
                // Tindakan ketika logo diklik
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar() {
        val toolbar : Toolbar = findViewById(R.id.toolbar_main_activity)

        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar.setNavigationOnClickListener {
            toogleDrawer()
        }

    }

    private fun toogleDrawer(){

        if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            binding?.drawerLayout!!.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {

        if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)

        } else{
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_beranda -> {
                binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
            }
            R.id.nav_my_profile -> {
                val intent = Intent(this, MyProfileActivity::class.java)
                startActivityForResult(intent, MY_PROFILE_REQUEST_CODE)
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
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)

        return true
    }

    fun updateNavigationUserDetails(siswa : Siswa){
        mUserName = siswa.name
        mSiswaId = siswa.id

        Glide
            .with(this)
            .load(siswa.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.nav_user_image))

        var tvUserName : TextView = findViewById(R.id.tv_username)
        tvUserName.text = siswa.name

//        if (readKelompokList){
//            showProgressDialog(resources.getString(R.string.mohon_tunggu))
//
//            FirestoreClass().getKelompokList(this)
//        }
    }

    fun populateKelasListToUI(kelasList: ArrayList<Kelas>){
        hideProgressDialog()
        val rvKelasList : RecyclerView = findViewById(R.id.rv_class_list)
        val tvNoKelasAvailable : TextView = findViewById(R.id.tv_no_class_available)


        if (kelasList.size >0){
            rvKelasList.visibility = View.VISIBLE
            tvNoKelasAvailable.visibility  = View.GONE

            rvKelasList.layoutManager = LinearLayoutManager(this)
            rvKelasList.setHasFixedSize(true)

            val adapter = KelasItemsAdapter(this,kelasList)
            rvKelasList.adapter = adapter

            adapter.setOnClickListener(object: KelasItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Kelas) {
                    val intent = Intent(this@MainActivitySiswa, MateriListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivityForResult(intent, UPDATE_KELAS_REQUEST_CODE)
                }
            })
        }else{
            rvKelasList.visibility = View.GONE
            tvNoKelasAvailable.visibility  = View.VISIBLE
        }
    }

}