package com.cscorner.myclothes.categories

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.cscorner.myclothes.menu.ItemActivity
import com.cscorner.myclothes.menu.adapters.ListAdapter
import com.cscorner.myclothes.R
import com.cscorner.myclothes.data.Clothes
import com.cscorner.myclothes.databinding.FragmentAllBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.Locale

//Fragment for the All Category Tab View with data initialization and search bar
//Shows all categories of items that users have uploaded
class AllFragment: Fragment(R.layout.fragment_all) {
    private lateinit var adapter: ListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var clothesArrayList: ArrayList<Clothes>
    lateinit var binding: FragmentAllBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        adapter = ListAdapter(ArrayList())
        binding = FragmentAllBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var count = 0
        val categories: List<String> = listOf(
            "Jackets", "TshirtsBlouses", "Trousers",
            "Skirts", "Dresses", "Shorts", "Sportswear", "Accessories"
        )

        //Initialize the list with data from all the categories
        for (category in categories) {
            val res = dataInitialize(category, binding.allList)
            if (res) {
                count++
            }
        }

        if (count == categories.size) {
            //Search through the list of the category to find the words
            //that the user enter from the keyboard
            val searchView = view.findViewById<SearchView>(R.id.searchView)

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    //Clear focus to hide the keyboard after search button in keyboard is pressed
                    searchView.clearFocus()
                    return true
                }

                //If the result of the search is empty show an empty list
                // else show a list with the results
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) {
                        adapter.searchDataList(clothesArrayList)
                    } else {
                        val filteredList = clothesArrayList.filter {
                            it.heading.lowercase(Locale.getDefault())
                                .contains(newText.lowercase(Locale.getDefault()))
                        } as ArrayList<Clothes>
                        adapter.searchDataList(filteredList)
                    }
                    return true
                }
            })
        }

    }

    //Initialize the data from the firebase and show the list of items found
    private fun dataInitialize(category: String, list: RecyclerView): Boolean{
        val db = Firebase.firestore

        clothesArrayList = arrayListOf()

        binding.allProgressBar.visibility = View.VISIBLE

        //Fetch data from Firestore
        db.collection(category).whereEqualTo("donateTo", "Individuals").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val cloth = document.toObject(Clothes::class.java)
                    clothesArrayList.add(cloth)
                }

                //display the list with two items in a row
                val gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                recyclerView = list
                recyclerView.layoutManager = gridLayoutManager
                recyclerView.setHasFixedSize(true)
                adapter = ListAdapter(clothesArrayList)
                recyclerView.adapter = adapter


                if (clothesArrayList.isEmpty()) {
                    Log.i("CHECK LIST", "EMPTY LIST")
                } else {
                    Log.i("CHECK LIST", "The list is not empty.")
                }
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged()

                binding.allProgressBar.visibility = View.INVISIBLE


                //pass data from the clicked item to the item activity
                adapter.setOnItemClickListener(object : ListAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val intent = Intent(activity, ItemActivity::class.java)
                        intent.putExtra("heading", clothesArrayList[position].heading)
                        intent.putExtra("clothId", clothesArrayList[position].clothId)
                        intent.putExtra("category", clothesArrayList[position].category)

                        activity?.startActivity(intent)
                    }
                })
            }.addOnFailureListener { exception ->
                binding.allProgressBar.visibility = View.INVISIBLE
                Log.w("Cloth got", "Error getting documents: ", exception)
            }
        return true
    }
}