package com.example.mobilelearningapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.KelasItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityMainGuruBinding
import com.example.mobilelearningapp.databinding.DialogBuatKelasBinding
import com.example.mobilelearningapp.databinding.DialogMoreMateriBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Guru
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainGuruActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding : ActivityMainGuruBinding? = null
    private lateinit var mUserName : String
    private lateinit var mGuruId : String
    private lateinit var mUsername : String

    companion object{
        const val GURU_PROFILE_REQUEST_CODE = 12
        const val CREATE_KELOMPOK_REQUEST_CODE = 13
        const val UPDATE_KELAS_REQUEST_CODE = 14
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainGuruBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        setupActionBar()

        FirestoreClass().getGuruDetails(this)
        FirestoreClass().getKelasList(this)

        binding?.navView?.setNavigationItemSelectedListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == GURU_PROFILE_REQUEST_CODE){
            FirestoreClass().getGuruDetails(this)
        }else if (resultCode == Activity.RESULT_OK && requestCode == CREATE_KELOMPOK_REQUEST_CODE){
            FirestoreClass().getKelasList(this)
        }else if (resultCode == Activity.RESULT_OK && requestCode == UPDATE_KELAS_REQUEST_CODE) {
            FirestoreClass().getKelasList(this)
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
                FirestoreClass().getSiswaDetails(this)
                FirestoreClass().getKelasList(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setupActionBar() {
        val toolbar : Toolbar = findViewById(R.id.toolbar_main_activity_guru)

        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbar.title = "Mobile Learning SMAN 4 Jeneponto"

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
                dialogBuatKelas()
            }

            R.id.nav_guru_profile -> {
                val intent = Intent(this, GuruProfileActivity::class.java)
                startActivityForResult(intent, GURU_PROFILE_REQUEST_CODE)
            }

            R.id.nav_tugas -> {
                val intent = Intent(this, GuruTugasSayaActivity::class.java)
                startActivity(intent)
            }

            R.id.nav_kuis -> {
                val intent = Intent(this,GuruKuisSayaActivity::class.java)
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

    private fun dialogBuatKelas() {
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
                Toast.makeText(this,"Silahkan isi informasi kelasips", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun dialogUpdateKelas(kelas: Kelas) {
        val binding = DialogBuatKelasBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(binding.root)

        // Mengisi EditText dengan informasi kelas yang ada
        binding.etClasssesName.setText(kelas.nama)
        binding.etCourse.setText(kelas.course)

        binding.tvAdd.text = "UPDATE" // Mengubah teks tombol menjadi "Update"

        binding.tvAdd.setOnClickListener {
            val newNama = binding.etClasssesName.text.toString()
            val newCourse = binding.etCourse.text.toString()

            val kelasHashMap = HashMap<String, Any>()
            var anyChangesMade = false

            if (newNama != kelas.nama) {
                kelasHashMap["nama"] = newNama
                anyChangesMade = true
            }

            if (newCourse != kelas.course) {
                kelasHashMap["course"] = newCourse
                anyChangesMade = true
            }

            if (anyChangesMade) {
                // Update kelas di Firestore
                kelas.documentId?.let { id ->
                    FirestoreClass().updateKelasData(this, kelasHashMap, id)
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Tidak ada perubahan yang dilakukan", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun kelompokCreatedSuccessfully(){
        FirestoreClass().getKelasList(this)
    }

    fun populateKelasListToUI(kelasList: ArrayList<Kelas>){
        val rvKelasList : RecyclerView = findViewById(R.id.rv_class_list)
        val tvNoKelasAvailable : TextView = findViewById(R.id.tv_no_class_available)


        if (kelasList.size >0){
            rvKelasList.visibility = View.VISIBLE
            tvNoKelasAvailable.visibility  = View.GONE

            rvKelasList.layoutManager = LinearLayoutManager(this)
            rvKelasList.setHasFixedSize(true)

            val adapter = KelasItemsAdapter(this@MainGuruActivity,kelasList)
            rvKelasList.adapter = adapter

            adapter.setOnClickListener(object: KelasItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Kelas) {
                    val intent = Intent(this@MainGuruActivity, MateriListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivityForResult(intent, UPDATE_KELAS_REQUEST_CODE)
                }
            })

            adapter.setOnEditClickListener(object : KelasItemsAdapter.OnEditClickListener {
                override fun onEditClick(position: Int, model: Kelas) {
                    dialogUpdateKelas(model)
                }
            })

            adapter.setOnDeleteClickListener(object : KelasItemsAdapter.OnDeleteClickListener {
                override fun onDeleteClick(position: Int, model: Kelas) {

                    val dialogView = LayoutInflater.from(this@MainGuruActivity).inflate(R.layout.dialog_confirm_delete, null)
                    // Konfirmasi penghapusan
                    val dialog = AlertDialog.Builder(this@MainGuruActivity)
                        .setView(dialogView)
                        .create()


                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()

                    val tvYa = dialogView.findViewById<TextView>(R.id.tv_ya)
                    val tvTidak = dialogView.findViewById<TextView>(R.id.tv_tidak)

                    tvYa.setOnClickListener {
                        model.documentId?.let { FirestoreClass().deleteKelas(this@MainGuruActivity, it) }
                        dialog.dismiss()
                    }

                    tvTidak.setOnClickListener {
                        dialog.dismiss()
                    }
                }
            })
        }else{
            rvKelasList.visibility = View.GONE
            tvNoKelasAvailable.visibility  = View.VISIBLE
        }
    }

    fun kelompokUpdateSuccess(){
        FirestoreClass().getKelasList(this)
    }

    fun kelasDeleteSuccess(){
        FirestoreClass().getKelasList(this)
    }

}