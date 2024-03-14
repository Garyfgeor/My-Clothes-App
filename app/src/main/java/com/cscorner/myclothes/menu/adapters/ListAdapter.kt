package com.cscorner.myclothes.menu.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cscorner.myclothes.R
import com.cscorner.myclothes.data.Clothes
import com.cscorner.myclothes.databinding.ListItemBinding

class ListAdapter(private var clothesList: ArrayList<Clothes>)
    : RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    lateinit var binding: ListItemBinding
    private lateinit var itemListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        itemListener = listener
    }

    class MyViewHolder(var binding: ListItemBinding, listener: OnItemClickListener)
        : RecyclerView.ViewHolder(binding.root){
        fun bind(clothes: Clothes){
            binding.clothes = clothes
            binding.executePendingBindings() //Ensure immediate binding

        }

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    //inflate list_item in list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)

        return MyViewHolder(binding, itemListener)
    }

    //returns the num of elements the list contains
    override fun getItemCount(): Int {
        return clothesList.size
    }

    //display data in the listImage and listHeading
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = clothesList[position]
        holder.bind(currentItem)
        Glide.with(holder.itemView.context)
            .load(currentItem.imageUrl1) // Assuming imageUrl1 is the URL string
            .placeholder(R.drawable.clothehanger) // Optional placeholder image
            .into(holder.binding.listImage) // Assumes listImage is the ImageView
    }

    fun searchDataList(searchList: ArrayList<Clothes>) {
        clothesList = searchList
        notifyDataSetChanged()
    }
}