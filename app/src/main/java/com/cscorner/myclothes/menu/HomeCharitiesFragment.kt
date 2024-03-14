package com.cscorner.myclothes.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.cscorner.myclothes.menu.adapters.ListAdapter
import com.cscorner.myclothes.data.Clothes
import com.cscorner.myclothes.databinding.FragmentHomecharitiesBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

//Home Screen for the user type "Charities" (Not used)
class HomeCharitiesFragment : Fragment() {

    private lateinit var adapter: ListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var clothesArrayList: ArrayList<Clothes>
    lateinit var binding: FragmentHomecharitiesBinding

    //Firebase References
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var user: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomecharitiesBinding.inflate(inflater, container, false)

        //Firebase Auth
        firebaseAuth = Firebase.auth
        user = firebaseAuth.currentUser!!

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        dataInitialize()
        //display the list with two items in a row
        val gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView = binding.womenList
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.setHasFixedSize(true)
        adapter = ListAdapter(clothesArrayList)
        recyclerView.adapter = adapter

        //pass data from the clicked item to the item activity
        adapter.setOnItemClickListener (object : ListAdapter.OnItemClickListener {
            override fun onItemClick(position: Int){
                val intent = Intent(activity, ItemActivity::class.java)
                intent.putExtra("heading", clothesArrayList[position].heading)
                intent.putExtra("clothId",  clothesArrayList[position].clothId)
                intent.putExtra("category", clothesArrayList[position].category)

                activity?.startActivity(intent)
            }
        })
    }

    //initialize the list with the items
    private fun dataInitialize() {
        val db = Firebase.firestore

        clothesArrayList = arrayListOf()

        //Fetch data from Firestore
        val categories: List<String> = listOf("Jackets", "T-Shirts", "Trousers",
            "Skirts", "Dresses", "Shorts", "Sportswear", "Accessories")

        for (category in categories){
            db.collection(category).whereEqualTo("donateTo", "Charities").get().addOnSuccessListener { result ->
                for (document in result) {
                    val cloth = document.toObject(Clothes::class.java)
                    Log.d("Cloth got", "${document.id} => ${document.data}")
                    clothesArrayList.add(cloth)
                }

                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged()

                if (clothesArrayList.isEmpty()) {
                    Log.i("CHECK LIST", "EMPTY LIST")
                } else {
                    Log.i("CHECK LIST", "The list is not empty.")
                }
            }
                .addOnFailureListener { exception ->
                    Log.w("Cloth got", "Error getting documents: ", exception)
                }
        }

    }
}