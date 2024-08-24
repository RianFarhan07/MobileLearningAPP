package com.example.mobilelearningapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobilelearningapp.databinding.ItemKelasBinding
import com.example.mobilelearningapp.models.Kelas

class KelasItemsAdapter(private val context: Context,
                        private var list : ArrayList<Kelas> ) :
    RecyclerView.Adapter<KelasItemsAdapter.KelasViewHolder>() {

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KelasViewHolder {
        val binding = ItemKelasBinding.inflate(
            LayoutInflater.from(context), parent, false)
        return KelasViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KelasViewHolder, position: Int) {
        val model = list[position]

            holder.binding.tvName.text = model.nama

            holder.itemView.setOnClickListener {
                val index = list.indexOf(model) // Dapatkan indeks model
                if (onClickListener != null && index != -1) {
                    onClickListener!!.onClick(index, model) // Gunakan indeks ini untuk memanggil onClickListener
                }
            }
        }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener{
        fun onClick(position: Int,model: Kelas)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    inner class KelasViewHolder(val binding: ItemKelasBinding) :
        RecyclerView.ViewHolder(binding.root)
}

