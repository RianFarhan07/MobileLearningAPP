package com.example.mobilelearningapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.activities.MateriDetailsActivity
import com.example.mobilelearningapp.databinding.ItemJawabBinding

import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.JawabanTugas
import com.example.mobilelearningapp.models.Kelas
import com.example.mobilelearningapp.models.Materi
import com.example.mobilelearningapp.models.Tugas
import com.example.mobilelearningapp.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class JawabTugasItemsAdapter(
    private val context: Context,
    private val items: ArrayList<JawabanTugas>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: JawabTugasItemsAdapter.OnClickListener? = null
    private var onDeleteClickListener: OnDeleteClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_jawab,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = items[position]
        if (holder is MyViewHolder) {
//            Glide
//                .with(context)
//                .load(model.image)
//                .centerCrop()
//                .placeholder(R.drawable.ic_user_place_holder)
//                .into(holder.binding.ivAnggotaImage)

            holder.tvNamaPenjawab.text = model.namaPenjawab
            holder.tvTanggalUpload.text = formatDate(model.uploadedDate)
            holder.tvNilai.text = model.nilai ?: "Belum dinilai"
            holder.tvNamaTugas.text = model.namaTugas
            holder.tvKelas.text = model.namaKelas
            holder.tvMateri.text = model.namaMateri

        }

        holder.itemView.setOnClickListener {
            val index = items.indexOf(model)
            if (onClickListener != null && index != -1) {
                onClickListener!!.onClick(index, model)
            }
        }

        holder.itemView.findViewById<ImageButton>(R.id.btnDelete).setOnClickListener {
            onDeleteClickListener?.onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    interface OnClickListener {
        fun onClick(position: Int, model: JawabanTugas)
    }

    fun setOnClickListener(onClickListener: JawabTugasItemsAdapter.OnClickListener) {
        this.onClickListener = onClickListener
    }

    fun setOnDeleteClickListener(onDeleteClickListener: OnDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener
    }

    fun updateData(newItems: List<JawabanTugas>) {
        Log.d("JawabTugasItemsAdapter", "Updating data with ${newItems.size} items")
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNamaPenjawab: TextView = view.findViewById(R.id.tvNamaPenjawab)
        val tvTanggalUpload: TextView = view.findViewById(R.id.tvTanggalUpload)
        val tvNilai: TextView = view.findViewById(R.id.tvNilai)
        val tvNamaTugas: TextView = view.findViewById(R.id.tvNamaTugas)
        val tvKelas: TextView = view.findViewById(R.id.tvNamaKelas)
        val tvMateri: TextView = view.findViewById(R.id.tvNamaMateri)
    }
}