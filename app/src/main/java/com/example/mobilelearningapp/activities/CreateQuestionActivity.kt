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

    private var binding: ActivityCreateQuestionBinding? = null
    private var mQuestionSize: Int = -1
    private var mSelectedImageFileUri: Uri? = null
    private var mMateriImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateQuestionBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        setupActionBar()
        setupListeners()

        if (intent.hasExtra(Constants.QUESTION_SIZE)) {
            mQuestionSize = intent.getIntExtra(Constants.QUESTION_SIZE, -1)
            Log.e("QUESTIONSIZE ", mQuestionSize.toString())
        }

        binding?.btnUploadImage?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarAddQuestion)
        val toolbar = supportActionBar
        if (toolbar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)

        }
        binding?.toolbarAddQuestion?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        btnSaveQuestion.setOnClickListener {
            val question = createQuestionFromInput()

            if (question != null) {
                val resultIntent = Intent().apply {
                    putExtra("question", question)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
//                Toast.makeText(this, "Pertanyaan tidak valid, mohon lengkapi semua field.", Toast.LENGTH_SHORT).show()
            }
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

    private fun createQuestionFromInput(): Question? {
        val pertanyaan = binding?.etQuestion?.text.toString()
        val option1 = binding?.etOption1?.text.toString()
        val option2 = binding?.etOption2?.text.toString()
        val option3 = binding?.etOption3?.text.toString()
        val option4 = binding?.etOption4?.text.toString()

        // Cek jika ada field yang kosong
        if (pertanyaan.isEmpty() || option1.isEmpty() || option2.isEmpty() || option3.isEmpty() || option4.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi semua field", Toast.LENGTH_SHORT).show()
            return null
        }

        // Cek jika tidak ada jawaban yang dipilih
        val correctAnswer = when {
            binding?.rbOption1?.isChecked == true -> 1
            binding?.rbOption2?.isChecked == true -> 2
            binding?.rbOption3?.isChecked == true -> 3
            binding?.rbOption4?.isChecked == true -> 4
            else -> {
                Toast.makeText(this, "Mohon pilih jawaban yang benar", Toast.LENGTH_SHORT).show()
                return null
            }
        }

        // Kembalikan objek Question baru jika semua validasi berhasil
        return Question(
            id = mQuestionSize + 1, // Misalnya cara sederhana untuk menghasilkan ID
            question = pertanyaan,
            image = mMateriImageURL, // Asumsikan tidak ada gambar untuk kesederhanaan
            optionOne = option1,
            optionTwo = option2,
            optionThree = option3,
            optionFour = option4,
            correctAnswer = correctAnswer
        )
    }

}
