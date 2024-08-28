package com.example.mobilelearningapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.models.JawabanTugas
import java.text.SimpleDateFormat
import java.util.*

class JawabItemsAdapter(private val jawabList: ArrayList<JawabanTugas>) :
    RecyclerView.Adapter<JawabItemsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaPenjawab: TextView = itemView.findViewById(R.id.tvNamaPenjawab)
        val tvTanggalUpload: TextView = itemView.findViewById(R.id.tvTanggalUpload)
        val tvNilai: TextView = itemView.findViewById(R.id.tvNilai)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_jawab, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jawaban = jawabList[position]

        holder.tvNamaPenjawab.text = jawaban.namaPenjawab
        holder.tvTanggalUpload.text = formatDate(jawaban.uploadedDate)
        holder.tvNilai.text = jawaban.nilai ?: "Belum dinilai"
    }

    override fun getItemCount(): Int = jawabList.size

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    interface OnClickListener {
        fun onClick(position: Int, model: JawabanTugas)
    }

    private var onClickListener: OnClickListener? = null

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
}