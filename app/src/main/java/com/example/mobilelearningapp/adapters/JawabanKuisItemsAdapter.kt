package com.example.mobilelearningapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.example.mobilelearningapp.models.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class JawabanKuisItemsAdapter(
    private val context: Context,
    private val items: ArrayList<JawabanKuis>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: JawabanKuisItemsAdapter.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_jawaban_kuis,
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
            holder.tvNilai.text = model.nilai ?: "Belum dinilai"
            holder.tvNamaKuis.text = model.namaKuis
            holder.tvNamaMateri.text = model.namaMateri
            holder.tvNamaKelas.text = model.namaKelas
        }

        holder.itemView.setOnClickListener {
            val index = items.indexOf(model)
            if (onClickListener != null && index != -1) {
                onClickListener!!.onClick(index, model)
            }
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
        fun onClick(position: Int, model: JawabanKuis)
    }

    fun setOnClickListener(onClickListener: JawabanKuisItemsAdapter.OnClickListener) {
        this.onClickListener = onClickListener
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNamaPenjawab: TextView = view.findViewById(R.id.tvNamaPenjawab)
        val tvNilai: TextView = view.findViewById(R.id.tvNilai)
        val tvNamaKuis: TextView = view.findViewById(R.id.tv_nama_kuis)
        val tvNamaMateri: TextView = view.findViewById(R.id.tv_nama_materi)
        val tvNamaKelas: TextView = view.findViewById(R.id.tv_nama_kelas)
    }
}