package com.example.mobilelearningapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityGuruProfileBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Guru
import com.example.mobilelearningapp.models.Siswa
import com.example.mobilelearningapp.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class GuruProfileActivity : BaseActivity() {

    private var binding : ActivityGuruProfileBinding? = null
    private lateinit var mGuruDetails : Guru
    private var mSelectedImageFileUri : Uri? = null
    private var mUserProfileImageURL: String = ""
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityGuruProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        setupActionBar()
        setupNavigationDrawer()

        FirestoreClass().getGuruDetails(this)

        binding?.ivProfileUserImage?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        binding?.btnSubmit?.setOnClickListener{
            if (mSelectedImageFileUri != null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.mohon_tunggu))

                updateUserProfileData()
            }
        }


    }

    //menampilkan logo smk di toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    private fun setupActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_update_profile_guru_activity)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_navigation_menu) // Ganti dengan icon menu Anda
        supportActionBar?.setDisplayShowTitleEnabled(false) // Sembunyikan judul default
        supportActionBar?.title = "PROFILE"
        toolbar.title = "PROFILE"
    }

    private fun setupNavigationDrawer() {
        drawerLayout = binding?.drawerLayout!!
        val navView: NavigationView = binding!!.navView

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding!!.toolbarUpdateProfileGuruActivity,
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
                R.id.nav_my_profile -> {
                    FirestoreClass().getGuruDetails(this)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }
        }else{
            Toast.makeText(this,"kamu menolak izin storage, aktifkan di setting", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!= null){
            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(findViewById(R.id.iv_profile_user_image))
            }catch (e: IOException){
                e.printStackTrace()
            }

        }
    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.mohon_tunggu))

        if (mSelectedImageFileUri != null){

            val sRef : StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "USER_IMAGE" + System.currentTimeMillis()
                            + "." + Constants.getFileExtension(this,mSelectedImageFileUri!!))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                    taskSnapShot ->
                Log.e(
                    "Firebase image URL",
                    taskSnapShot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapShot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                    Log.e(
                        "Downloadable Image URL",
                        uri.toString())
                    mUserProfileImageURL = uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener{
                    exception ->
                Toast.makeText(this@GuruProfileActivity,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()
                hideProgressDialog()
            }

        }
    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()

        var anyChangesMade = false

        if (mUserProfileImageURL.isNotEmpty() && mUserProfileImageURL != mGuruDetails.image){
            userHashMap[Constants.IMAGE] = mUserProfileImageURL
            anyChangesMade = true
        }
        if (binding?.etClasses?.text.toString() != mGuruDetails.classes){
            userHashMap[Constants.CLASSES] = binding?.etClasses?.text.toString()
            anyChangesMade = true
        }
        if (binding?.etName?.text.toString() != mGuruDetails.name){
            userHashMap[Constants.NAME] = binding?.etName?.text.toString()
            anyChangesMade = true
        }
        if (binding?.etEmail?.text.toString() != mGuruDetails.email){
            userHashMap[Constants.EMAIL] = binding?.etEmail?.text.toString()
            anyChangesMade = true
        }

        val newPassword = binding?.etPassword?.text.toString()
        if (newPassword.isNotEmpty()) {
            // Jika password diisi, kita akan mengubahnya
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                showProgressDialog(resources.getString(R.string.mohon_tunggu))
                user.updatePassword(newPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                            anyChangesMade = true
                        } else {
                            Toast.makeText(this, "Gagal mengubah password coba logout dan login kembali: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            hideProgressDialog()
                            finish()
                        }
                        // Lanjutkan dengan pembaruan profil lainnya
                        continueProfileUpdate(userHashMap, anyChangesMade)
                    }
            } else {
                Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
        } else {
            // Jika password tidak diubah, langsung lanjut ke pembaruan profil
            continueProfileUpdate(userHashMap, anyChangesMade)
        }

        if (anyChangesMade){
            FirestoreClass().updateGuruProfileData(this,userHashMap)
        }else{
            Toast.makeText(this,"there are no changes made", Toast.LENGTH_SHORT).show()
            hideProgressDialog()
        }

    }

    private fun continueProfileUpdate(userHashMap: HashMap<String, Any>, anyChangesMade: Boolean) {
        if (anyChangesMade || userHashMap.isNotEmpty()) {
            FirestoreClass().updateGuruProfileData(this, userHashMap)
        } else {
            Toast.makeText(this, "Tidak ada perubahan yang dilakukan", Toast.LENGTH_SHORT).show()
            hideProgressDialog()
        }
    }

    fun setGuruDataInUI(guru: Guru){
        mGuruDetails = guru

        Glide
            .with(this)
            .load(guru.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.iv_profile_user_image))

        binding?.etName?.setText(guru.name)
        binding?.etEmail?.setText(guru.email)
        binding?.etPassword?.setText("")

    }

    fun profileUpdateSuccess(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }
}