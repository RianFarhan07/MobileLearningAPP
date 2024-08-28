package com.example.mobilelearningapp.activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelearningapp.KelasItemsAdapter
import com.example.mobilelearningapp.MateriItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.databinding.ActivityMateriListBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Materi
import com.example.mobilelearningapp.utils.Constants

class MateriListActivity : BaseActivity() {

    private var binding : ActivityMateriListBinding? = null
    private lateinit var mKelasDetails : Kelas
    lateinit var mKelasDocumentId : String

    companion object{
        const val REQUEST_CODE_MATERI_DETAILS = 7
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMateriListBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            mKelasDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
            Log.e("document","document $mKelasDocumentId")
        }
        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        FirestoreClass().getKelasDetails(this,mKelasDocumentId)

        val currentUserID = FirestoreClass().getCurrentUserID()
        if (currentUserID.isNotEmpty()) {
            FirestoreClass().getUserRole(currentUserID) { role ->
                if (role == "siswa") {
                    binding?.tvAddMateri?.visibility = View.GONE
                }
            }
        }

        binding!!.tvAddMateri.setOnClickListener {
            binding!!.tvAddMateri.visibility = View.GONE
            binding!!.cvAddTaskListName.visibility = View.VISIBLE
        }

        binding!!.ibCloseListName.setOnClickListener {
            binding!!.tvAddMateri.visibility = View.VISIBLE
            binding!!.cvAddTaskListName.visibility = View.GONE
        }

        binding!!.ibDoneListName.setOnClickListener{
            val listName = binding!!.etTaskListName.text.toString()

            if (listName.isNotEmpty()){
                createMateriList(listName)
                binding!!.etTaskListName.text.clear()
                binding!!.tvAddMateri.visibility = View.VISIBLE
                binding!!.cvAddTaskListName.visibility = View.GONE
            }else{
                Toast.makeText(this,"Please enter list name",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MATERI_DETAILS && resultCode == RESULT_OK) {
            showProgressDialog(resources.getString(R.string.mohon_tunggu))
            FirestoreClass().getKelasDetails(this, mKelasDocumentId)
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarMateriListActivity)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            supportActionBar?.title = "Daftar Materi ${mKelasDetails.nama}"
        }
        binding?.toolbarMateriListActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    fun kelasDetails(kelas: Kelas){
        mKelasDetails = kelas

        setupActionBar()
        populateMaterListToUI(mKelasDetails.materiList)
        hideProgressDialog()

    }

    fun materiDetails(materiListPosition: Int){
        val intent = Intent(this,MateriDetailsActivity::class.java)
        intent.putExtra(Constants.MATERI_LIST_ITEM_POSITION,materiListPosition)
        intent.putExtra(Constants.KELAS_DETAIL,mKelasDetails)
        intent.putExtra(Constants.DOCUMENT_ID, mKelasDocumentId)
        startActivityForResult(intent,REQUEST_CODE_MATERI_DETAILS)
    }

    fun populateMaterListToUI(materiList: ArrayList<Materi>){
        val rvMateriList : RecyclerView = findViewById(R.id.rv_materi_list)
        val tvNoKelasAvailable : TextView = findViewById(R.id.tv_no_materi_available)


        if (materiList.size >0){
            rvMateriList.visibility = View.VISIBLE
            tvNoKelasAvailable.visibility  = View.GONE

            rvMateriList.layoutManager = LinearLayoutManager(this)
            rvMateriList.setHasFixedSize(true)

            val adapter = MateriItemsAdapter(this@MateriListActivity,materiList)
            rvMateriList.adapter = adapter

            adapter.setOnClickListener(object: MateriItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Materi) {
                   materiDetails(position)
                }
            })

            adapter.setOnEditClickListener(object : MateriItemsAdapter.OnEditClickListener {
                override fun onEditClick(position: Int, model: Materi) {
                    updateMateri(model)
                }
            })

            adapter.setOnDeleteClickListener(object : MateriItemsAdapter.OnDeleteClickListener {
                override fun onDeleteClick(position: Int, model: Materi) {
                    val dialogView = LayoutInflater.from(this@MateriListActivity).inflate(R.layout.dialog_confirm_delete, null)
                    val dialog = AlertDialog.Builder(this@MateriListActivity)
                        .setView(dialogView)
                        .create()

                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()

                    val tvYa = dialogView.findViewById<TextView>(R.id.tv_ya)
                    val tvTidak = dialogView.findViewById<TextView>(R.id.tv_tidak)

                    tvYa.setOnClickListener {
                        showProgressDialog(resources.getString(R.string.mohon_tunggu))
                        FirestoreClass().deleteMateri(this@MateriListActivity, model.id)
                        dialog.dismiss()
                    }

                    tvTidak.setOnClickListener {
                        dialog.dismiss()
                    }
                }
            })
        }else{
            rvMateriList.visibility = View.GONE
            tvNoKelasAvailable.visibility  = View.VISIBLE
        }
    }

    fun createMateriList(materiListName: String){

        val currentUserId = FirestoreClass().getCurrentUserID()
        val materiId = FirestoreClass().mFireStore.collection(Constants.KELAS).document().id
        val materi = Materi(materiId, materiListName, currentUserId)


        mKelasDetails.materiList.add(0,materi)
        //        mKelompokDetails.taskList.removeAt(mKelompokDetails.taskList.size -1)

        showProgressDialog(resources.getString(R.string.mohon_tunggu))

        FirestoreClass().addUpdateMateriList(this,mKelasDetails)
    }

    fun addUpdateMateriListSuccess(){
        hideProgressDialog()

        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        FirestoreClass().getKelasDetails(this@MateriListActivity,mKelasDetails.documentId.toString())
    }

    private fun updateMateri(materi: Materi) {
        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        FirestoreClass().updateMateri(this, mKelasDocumentId, materi)
    }

    fun materiUpdateSuccess() {
        hideProgressDialog()
        Toast.makeText(this, "Materi berhasil diupdate", Toast.LENGTH_SHORT).show()
        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        FirestoreClass().getKelasDetails(this, mKelasDocumentId)
    }

    fun materiDeleteSuccess() {
        hideProgressDialog()
        Toast.makeText(this, "Materi berhasil dihapus", Toast.LENGTH_SHORT).show()
        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        FirestoreClass().getKelasDetails(this, mKelasDocumentId)
    }

}