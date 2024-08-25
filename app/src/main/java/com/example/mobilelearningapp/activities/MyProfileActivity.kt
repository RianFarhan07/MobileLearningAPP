package com.example.mobilelearningapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityMyProfileBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Siswa
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private var binding : ActivityMyProfileBinding? =null
    private lateinit var mSiswaDetails : Siswa
    private var mSelectedImageFileUri : Uri? = null
    private var mUserProfileImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        setupActionBar()

        FirestoreClass().getSiswaDetails(this)

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
    private fun setupActionBar() {

        setSupportActionBar(binding?.toolbarUpdateProfileActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        binding?.toolbarUpdateProfileActivity?.setNavigationOnClickListener { onBackPressed() }
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
                Toast.makeText(this@MyProfileActivity,
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

        if (mUserProfileImageURL.isNotEmpty() && mUserProfileImageURL != mSiswaDetails.image){
            userHashMap[Constants.IMAGE] = mUserProfileImageURL
            anyChangesMade = true
        }

        if (binding?.etName?.text.toString() != mSiswaDetails.name){
            userHashMap[Constants.NAME] = binding?.etName?.text.toString()
            anyChangesMade = true
        }
        if (binding?.etEmail?.text.toString() != mSiswaDetails.email){
            userHashMap[Constants.EMAIL] = binding?.etEmail?.text.toString()
            anyChangesMade = true
        }
        if (binding?.etPassword?.text.toString() != mSiswaDetails.classes){
            userHashMap[Constants.CLASSES] = binding?.etPassword?.text.toString()
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
                            Toast.makeText(this, "Gagal mengubah password: ${task.exception?.message}", Toast.LENGTH_LONG).show()
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
            FirestoreClass().updateSiswaProfileData(this,userHashMap)
        }else{
            Toast.makeText(this,"there are no changes made", Toast.LENGTH_SHORT).show()
            hideProgressDialog()
        }

    }

    private fun continueProfileUpdate(userHashMap: HashMap<String, Any>, anyChangesMade: Boolean) {
        if (anyChangesMade || userHashMap.isNotEmpty()) {
            FirestoreClass().updateSiswaProfileData(this, userHashMap)
        } else {
            Toast.makeText(this, "Tidak ada perubahan yang dilakukan", Toast.LENGTH_SHORT).show()
            hideProgressDialog()
        }
    }

    fun setSiswaDataInUI(siswa: Siswa){
        mSiswaDetails = siswa

        Glide
            .with(this)
            .load(siswa.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.iv_profile_user_image))

        binding?.etName?.setText(siswa.name)
        binding?.etEmail?.setText(siswa.email)
        binding?.etPassword?.setText("")
        binding?.etClasses?.setText(siswa.classes)

    }

    fun profileUpdateSuccess(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }
}