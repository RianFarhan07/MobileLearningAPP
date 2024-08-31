package com.example.mobilelearningapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.activities.MateriDetailsActivity
import com.example.mobilelearningapp.databinding.ItemJawabBinding

import com.example.mobilelearningapp.firebase.FirestoreClass
import com.example.mobilelearningapp.models.*
import com.example.mobilelearningapp.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class jawabanKuisItemsAdapter(
    private val context: Context,
    private val items: ArrayList<JawabanKuis>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: jawabanKuisItemsAdapter.OnClickListener? = null

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

    fun setOnClickListener(onClickListener: jawabanKuisItemsAdapter.OnClickListener) {
        this.onClickListener = onClickListener
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNamaPenjawab: TextView = view.findViewById(R.id.tvNamaPenjawab)
        val tvNilai: TextView = view.findViewById(R.id.tvNilai)
    }
}