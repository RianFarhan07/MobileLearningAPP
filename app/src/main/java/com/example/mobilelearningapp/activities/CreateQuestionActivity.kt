package com.example.mobilelearningapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityCreateQuestionBinding
import com.example.mobilelearningapp.models.Question
import com.example.mobilelearningapp.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create_question.*
import java.io.IOException

class CreateQuestionActivity : BaseActivity() {

    private var binding : ActivityCreateQuestionBinding? = null
    private var mQuestionSize : Int = -1
    private var mSelectedImageFileUri : Uri? = null
    private var mMateriImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateQuestionBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        setupActionBar()
        setupListeners()

        if (intent.hasExtra(Constants.QUESTION_SIZE)) {
            mQuestionSize = intent.getIntExtra(Constants.QUESTION_SIZE,-1)
                Log.e("QUESTIONSIZE ", mQuestionSize.toString())
        }

        binding?.btnUploadImage?.setOnClickListener{
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

    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarAddQuestion)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

        }
        binding?.toolbarAddQuestion?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        btnSaveQuestion.setOnClickListener {
            val question = createQuestionFromInput()
            val resultIntent = Intent().apply {
                putExtra("question", question)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            showProgressDialog("Uploading Image")
            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this@CreateQuestionActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding?.ivQuestionImage!!)

                binding?.ivQuestionImage?.visibility = View.VISIBLE

                // Panggil fungsi untuk upload gambar
                uploaQuestionImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploaQuestionImage() {

        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "QUESTION_IMAGE" + System.currentTimeMillis() + "."
                        + Constants.getFileExtension(this, mSelectedImageFileUri!!)
            )

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.e(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.e("Downloadable Image URL", uri.toString())
                    mMateriImageURL = uri.toString()
                    hideProgressDialog()

                    //TODO INI PULIHKAN JIKA GAGAL
                    // Update Materi dengan URL gambar baru
//                    updateMateriWithImage()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this@CreateQuestionActivity,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()
                hideProgressDialog()
            }
        }
    }

    private fun createQuestionFromInput(): Question {
        return Question(
            id = mQuestionSize + 1, // This is just a simple way to generate an ID
            question = etQuestion.text.toString(),
            image = mMateriImageURL, // Assume no image for simplicity
            optionOne = etOption1.text.toString(),
            optionTwo = etOption2.text.toString(),
            optionThree = etOption3.text.toString(),
            optionFour = etOption4.text.toString(),
            correctAnswer = when {
                rbOption1.isChecked -> 1
                rbOption2.isChecked -> 2
                rbOption3.isChecked -> 3
                rbOption4.isChecked -> 4
                else -> 0 // Handle error case
            }
        )
    }
}
