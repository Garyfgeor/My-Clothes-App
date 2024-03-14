package com.cscorner.myclothes.menu.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cscorner.myclothes.R
import com.cscorner.myclothes.data.Clothes
import com.cscorner.myclothes.databinding.MyuploadsItemBinding
import com.cscorner.myclothes.menu.HomeIndividualsFragment
import com.cscorner.myclothes.menu.MyUploadsFragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MyUploadsListAdapter(
    private var clothesList: ArrayList<Clothes>,
    private val context: Context,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<MyUploadsListAdapter.MyViewHolder>() {

    lateinit var binding: MyuploadsItemBinding
    private lateinit var itemListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemListener = listener
    }

    inner class MyViewHolder(var binding: MyuploadsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                itemListener.onItemClick(adapterPosition)
            }

            binding.deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteDialog()
                }
            }
        }

        fun bind(clothes: Clothes) {
            binding.clothes = clothes
            binding.executePendingBindings() //Ensure immediate binding
        }

        private fun showDeleteDialog() {
            val builder = AlertDialog.Builder(context, R.style.AlertDialogStyle)
            builder.setTitle("Confirm Deleting the Item")
                .setMessage(("Are you sure you want to delete this item?\n" +
                        "This action cannot be undone and the item will be permanently removed."))
                .setPositiveButton("Yes") { dialog, which ->
                    //Delete item from firebase
                    val db = Firebase.firestore
                    val currentItem = clothesList[position]
                    val categories: List<String> = listOf(
                        "Jackets", "TshirtsBlouses", "Trousers",
                        "Skirts", "Dresses", "Shorts", "Sportswear", "Accessories"
                    )
                    for (category in categories) {
                        db.collection(category)
                            .whereEqualTo("clothId", currentItem.clothId)
                            .get()
                            .addOnSuccessListener { result ->
                                db.collection(category)
                                    .document(currentItem.clothId) // Get the DocumentReference
                                    .delete()
                                    .addOnSuccessListener {
                                        //Document successfully deleted
                                        fragmentManager.beginTransaction().remove(MyUploadsFragment()).commit()
                                        fragmentManager.beginTransaction()
                                            .replace(R.id.fragment_container, HomeIndividualsFragment()).commit()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "An error occurred!", Toast.LENGTH_LONG)
                                            .show()
                                    }
                            }
                    }


                }
                .setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }.show()
        }
    }

    //inflate list_item in list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = MyuploadsItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)

        return MyViewHolder(binding)
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
