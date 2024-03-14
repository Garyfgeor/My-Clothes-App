package com.cscorner.myclothes.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.cscorner.myclothes.menu.adapters.MyUploadsListAdapter
import com.cscorner.myclothes.R
import com.cscorner.myclothes.data.Clothes
import com.cscorner.myclothes.databinding.FragmentMyuploadsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.Locale


//This class represents the My Uploads Screen when the "MyUploads" option from
// the menu is clicked, and shows all user's uploaded items, giving him the option
// to delete an item that is not still available. Also contains a search view to
// be easier for him to search for a specific item.
class MyUploadsFragment : Fragment() {

    private lateinit var adapter: MyUploadsListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var clothesArrayList: ArrayList<Clothes>
    lateinit var binding: FragmentMyuploadsBinding

    //Firebase References
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var user: FirebaseUser
    lateinit var userId: String
    private lateinit var noUploadsText: TextView

    //This function creates and returns the layout for the fragment's UI.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMyuploadsBinding.inflate(inflater, container, false)

        noUploadsText = binding.noUploadsText

        //Firebase Auth
        firebaseAuth = Firebase.auth
        user = firebaseAuth.currentUser!!
        userId = user.uid

        adapter = MyUploadsListAdapter(ArrayList(), requireContext(), requireFragmentManager())

        return binding.root
    }

    //It initialize the UI components by calling the dataInitialize function and creates the search view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var count = 0
        val categories: List<String> = listOf(
            "Jackets", "TshirtsBlouses", "Trousers",
            "Skirts", "Dresses", "Shorts", "Sportswear", "Accessories"
        )
        for (category in categories) {
            val res = dataInitialize(category, binding.myUploadsList, userId)
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
    private fun dataInitialize(category: String, list: RecyclerView, userId: String): Boolean{
        val db = Firebase.firestore

        clothesArrayList = arrayListOf()

        binding.myUploadsProgressBar.visibility = View.VISIBLE

        //Fetch data from Firestore
        db.collection(category)
            .whereEqualTo("donateTo", "Individuals")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val cloth = document.toObject(Clothes::class.java)
                    clothesArrayList.add(cloth)
                }
                //Notify the adapter that the data has changed
                adapter.notifyDataSetChanged()

                //Display the list with two items in a row
                val gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                recyclerView = list
                recyclerView.layoutManager = gridLayoutManager
                recyclerView.setHasFixedSize(true)
                adapter = MyUploadsListAdapter(clothesArrayList, requireContext(), requireFragmentManager())
                recyclerView.adapter = adapter


                if (clothesArrayList.isEmpty()) {
                    noUploadsText.visibility = View.VISIBLE
                    Log.i("CHECK LIST", "EMPTY LIST")
                } else {
                    noUploadsText.visibility = View.INVISIBLE
                    Log.i("CHECK LIST", "The list is not empty.")
                }

                binding.myUploadsProgressBar.visibility = View.INVISIBLE


                //pass data from the clicked item to the item activity
                adapter.setOnItemClickListener(object : MyUploadsListAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val intent = Intent(activity, ItemActivity::class.java)
                        intent.putExtra("heading", clothesArrayList[position].heading)
                        intent.putExtra("clothId", clothesArrayList[position].clothId)
                        intent.putExtra("category", clothesArrayList[position].category)

                        activity?.startActivity(intent)
                    }
                })
            }.addOnFailureListener { exception ->
                binding.myUploadsProgressBar.visibility = View.INVISIBLE
                Log.w("Cloth got", "Error getting documents: ", exception)
            }
        return true
    }
}