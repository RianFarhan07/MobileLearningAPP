package com.example.mobilelearningapp.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityMainGuruBinding
import com.example.mobilelearningapp.databinding.DialogBuatKelasBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Guru
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Siswa
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainGuruActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding : ActivityMainGuruBinding? = null
    private lateinit var mUserName : String
    private lateinit var mGuruId : String
    private lateinit var mUsername : String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainGuruBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        setupActionBar()

        FirestoreClass().getGuruDetails(this)

        binding?.navView?.setNavigationItemSelectedListener(this)
    }

    private fun setupActionBar() {
        val toolbar : Toolbar = findViewById(R.id.toolbar_main_activity_guru)

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
            R.id.nav_buat_kelas -> {
                dialogSearchAnggota()
            }

            R.id.materi -> {

            }

            R.id.nav_kuis -> {

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

    fun updateNavigationUserDetails(guru: Guru){
        mUserName = guru.name
        mGuruId = guru.id

        Glide
            .with(this)
            .load(guru.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.nav_user_image))

        var tvUserName : TextView = findViewById(R.id.tv_username)
        tvUserName.text = guru.name

//        if (readKelompokList){
//            showProgressDialog(resources.getString(R.string.mohon_tunggu))
//
//            FirestoreClass().getKelompokList(this)
//        }
    }

    private fun dialogSearchAnggota() {
        val binding = DialogBuatKelasBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(binding.root)

        binding.tvAdd.setOnClickListener {
            val nama = binding.etClasssesName.text.toString()
            val course = binding.etCourse.text.toString()

            var kelas = Kelas(
                nama,
                course,
                mUserName,
            )

            if (binding.etClasssesName.text?.isNotEmpty() == true &&
                binding.etCourse.text?.isNotEmpty() == true){
                FirestoreClass().createKelas(this,kelas)

                dialog.dismiss()
            }else{
                Toast.makeText(this,"Silahkan isi informasi kelompok", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    fun kelompokCreatedSuccessfully(){
//
    }

}