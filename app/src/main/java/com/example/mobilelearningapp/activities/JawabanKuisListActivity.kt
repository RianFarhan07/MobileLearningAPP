package com.example.mobilelearningapp.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityJawabanKuisListBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.JawabanKuisItemsAdapter
import com.example.mobilelearningapp.models.JawabanKuis
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.utils.Constants

class JawabanKuisListActivity : BaseActivity() {

    private var binding : ActivityJawabanKuisListBinding? = null
    private lateinit var mKelasDetails : Kelas
    lateinit var mKelasDocumentId : String
    private var mMateriListPosition = -1
    private var mKuisListPosition = -1

    private lateinit var mJawabList: ArrayList<JawabanKuis>

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityJawabanKuisListBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        getIntentData()
        setupActionBar()
        FirestoreClass().getKelasDetails(this,mKelasDocumentId)

    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.KELAS_DETAIL)) {
            mKelasDetails = intent.getParcelableExtra(Constants.KELAS_DETAIL)!!

        }
        if (intent.hasExtra(Constants.MATERI_LIST_ITEM_POSITION)) {
            mMateriListPosition = intent.getIntExtra(Constants.MATERI_LIST_ITEM_POSITION, -1)
            Log.e("MATERI_ITEM_POSITION", mMateriListPosition.toString())
        }
        if (intent.hasExtra(Constants.QUIZ_LIST_ITEM_POSITION)) {
            mKuisListPosition = intent.getIntExtra(Constants.QUIZ_LIST_ITEM_POSITION, -1)
            Log.e("QUIZ_ITEM_POSITION", mKuisListPosition.toString())
        }
        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mKelasDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
            Log.e("document", "document $mKelasDocumentId")
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarJawabanListActivity)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)

            supportActionBar?.title = "Tugas ${mKelasDetails.materiList[mMateriListPosition].kuis[mKuisListPosition].namaKuis}"

        }
        binding?.toolbarJawabanListActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun kelasDetails(kelas: Kelas){
        mKelasDetails = kelas

        setupActionBar()
        populateJawabListToUI(mKelasDetails.materiList[mMateriListPosition].kuis[mKuisListPosition].jawab)

    }

    fun populateJawabListToUI(jawabList: ArrayList<JawabanKuis>) {
        mJawabList = jawabList


        val rvJawabList: RecyclerView = findViewById(R.id.rv_jawaban_kuis_list)
        val tvNoJawaban : TextView = findViewById(R.id.tv_no_jawaban_available)

        if (mJawabList.isNotEmpty()) {
            rvJawabList.visibility = View.VISIBLE
            tvNoJawaban.visibility = View.GONE
            rvJawabList.layoutManager = LinearLayoutManager(this)
            rvJawabList.setHasFixedSize(true)

            val adapter = JawabanKuisItemsAdapter(this, mJawabList)
            rvJawabList.adapter = adapter

//            val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
//                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                    val position = viewHolder.adapterPosition
//                    val jawabToDelete = jawabList[position]
//
//                    val dialogView = LayoutInflater.from(this@JawabanKuisListActivity).inflate(R.layout.dialog_confirm_delete, null)
//                    val dialog = AlertDialog.Builder(this@JawabanKuisListActivity)
//                        .setView(dialogView)
//                        .create()
//
//                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                    dialog.show()
//
//                    val tvYa = dialogView.findViewById<TextView>(R.id.tv_ya)
//                    val tvTidak = dialogView.findViewById<TextView>(R.id.tv_tidak)
//
//                    tvYa.setOnClickListener {
//                        showProgressDialog(resources.getString(R.string.mohon_tunggu))
//                        FirestoreClass().deleteJawabTugasForGuru(
//                            this@JawabanListActivity,
//                            mKelasDocumentId,
//                            mMateriListPosition,
//                            mTugasListPosition,
//                            jawabToDelete.id
//                        )
//                        dialog.dismiss()
//                    }
//
//                    tvTidak.setOnClickListener {
//                        dialog.dismiss()
//                    }
//
//                }
//            }
//
//            val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
//            deleteItemTouchHelper.attachToRecyclerView(rvJawabList)
//
//            adapter.setOnClickListener(object: JawabTugasItemsAdapter.OnClickListener{
//                override fun onClick(position: Int, model: JawabanTugas) {
//                    jawabanDetails(position)
//                }
//            })

        } else {
            rvJawabList.visibility = View.GONE
            tvNoJawaban.visibility = View.VISIBLE
        }
    }
}