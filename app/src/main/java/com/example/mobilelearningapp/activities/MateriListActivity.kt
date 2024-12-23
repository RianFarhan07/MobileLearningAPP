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
import androidx.viewpager.widget.ViewPager
import com.example.mobilelearningapp.KelasItemsAdapter
import com.example.mobilelearningapp.MateriItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.adapters.MapelPagerAdapter
import com.example.mobilelearningapp.databinding.ActivityMateriListBinding
import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.fragments.MateriListFragment
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Materi
import com.example.mobilelearningapp.utils.Constants
import com.google.android.material.tabs.TabLayout

class MateriListActivity : BaseActivity() {

    private var binding : ActivityMateriListBinding? = null
    private lateinit var mKelasDetails : Kelas
    lateinit var mKelasDocumentId : String
    private lateinit var viewPager: ViewPager
    private lateinit var tabs: TabLayout
    private lateinit var adapter: MapelPagerAdapter

    companion object{
        const val REQUEST_CODE_MATERI_DETAILS = 7
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMateriListBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        viewPager = findViewById(R.id.view_pager)
        tabs = findViewById(R.id.tabs)

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
            val listName = binding!!.etMateriListName.text.toString().trim()
            val materiCourse = binding!!.etMateriListCourse.text.toString().trim()
            val className = mKelasDetails.nama
            val camelCaseMateriCourse = materiCourse.toLowerCase()

            if (listName.isNotEmpty() && materiCourse.isNotEmpty()){
                createMateriList(listName,camelCaseMateriCourse,className)
                binding!!.etMateriListName.text.clear()
                binding!!.etMateriListCourse.text.clear()
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

    private fun setupViewPager(materiList: ArrayList<Materi>) {
        adapter = MapelPagerAdapter(supportFragmentManager)
        val materiByMapel = materiList.groupBy { it.mapel }

        for ((mapel, materiForMapel) in materiByMapel) {
            val fragment = adapter.getFragment(mapel) as? MateriListFragment
            if (fragment != null && fragment.isAdded) {
                fragment.updateMateriList(ArrayList(materiForMapel))
            } else {
                adapter.addFragment(MateriListFragment.newInstance(mapel, ArrayList(materiForMapel)), mapel)
            }
        }

        adapter.notifyDataSetChanged()

        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarMateriList)
        val toolbar = supportActionBar
        if (toolbar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            supportActionBar?.title = "Daftar Materi ${mKelasDetails.nama}"
        }
        binding?.toolbarMateriList?.setNavigationOnClickListener {
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
        setupViewPager(mKelasDetails.materiList)
        hideProgressDialog()

    }

    fun materiDetails(materiId: String) {
        val intent = Intent(this, MateriDetailsActivity::class.java)
        intent.putExtra(Constants.MATERI_ID, materiId)
        //ganti ini jadi kirim kelasdocumentid
//        intent.putExtra((Constants.KELAS_DETAIL,mKelasDetails)
        intent.putExtra(Constants.DOCUMENT_ID, mKelasDocumentId)
        startActivityForResult(intent, REQUEST_CODE_MATERI_DETAILS)
    }

    fun createMateriList(materiListName: String, materiCourse: String, kelasName: String){

        val currentUserId = FirestoreClass().getCurrentUserID()
        val materiId = FirestoreClass().mFireStore.collection(Constants.KELAS).document().id
        val materi = Materi(materiId, materiListName, materiCourse,kelasName,currentUserId)


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

    fun updateMateri(materi: Materi) {
        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        FirestoreClass().updateMateri(this, mKelasDocumentId, materi)
        updateViewPager()
    }

    fun showDeleteConfirmationDialog(materi: Materi) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Hapus")
        builder.setMessage("Apakah Anda yakin ingin menghapus materi '${materi.nama}'?")
        builder.setPositiveButton("Ya") { dialog, _ ->
            deleteMateri(materi)
            dialog.dismiss()
        }
        builder.setNegativeButton("Tidak") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteMateri(materi: Materi) {
        showProgressDialog(resources.getString(R.string.mohon_tunggu))
        FirestoreClass().deleteMateri(this, materi.id)
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

    private fun updateViewPager() {
        val materiByMapel = mKelasDetails.materiList.groupBy { it.mapel }

        // Remove tabs that no longer have any materi
        val tabsToRemove = mutableListOf<String>()
        for (i in 0 until adapter.count) {
            val mapel = adapter.getPageTitle(i).toString()
            if (!materiByMapel.containsKey(mapel)) {
                tabsToRemove.add(mapel)
            }
        }
        tabsToRemove.forEach { mapel ->
            val index = adapter.getFragmentPosition(mapel)
            if (index != -1) {
                adapter.removeFragment(index)
            }
        }

        // Update existing fragments and add new ones
        for ((mapel, materiForMapel) in materiByMapel) {
            val fragment = adapter.getFragment(mapel) as? MateriListFragment
            if (fragment != null) {
                fragment.updateMateriList(ArrayList(materiForMapel))
            } else {
                adapter.addFragment(MateriListFragment.newInstance(mapel, ArrayList(materiForMapel)), mapel)
            }
        }

        adapter.notifyDataSetChanged()
        tabs.setupWithViewPager(viewPager)
    }

}