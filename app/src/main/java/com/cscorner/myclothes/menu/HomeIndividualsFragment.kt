package com.cscorner.myclothes.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cscorner.myclothes.menu.adapters.HomeViewPagerAdapter
import com.cscorner.myclothes.categories.AccessoriesFragment
import com.cscorner.myclothes.categories.AllFragment
import com.cscorner.myclothes.categories.DressesFragment
import com.cscorner.myclothes.categories.JacketsFragment
import com.cscorner.myclothes.categories.ShortsFragment
import com.cscorner.myclothes.categories.SkirtsFragment
import com.cscorner.myclothes.categories.SportswearFragment
import com.cscorner.myclothes.categories.TrousersFragment
import com.cscorner.myclothes.categories.TshirtsFragment
import com.cscorner.myclothes.databinding.FragmentHomeindividualsBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

//This class represents the Home Screen when the "Home" option from
//the menu is clicked, and shows the categories of items in a Tab View
class HomeIndividualsFragment : Fragment() {

    lateinit var binding: FragmentHomeindividualsBinding

    //Firebase References
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var user: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeindividualsBinding.inflate(inflater, container, false)

        //Firebase Auth
        firebaseAuth = Firebase.auth
        user = firebaseAuth.currentUser!!

        return binding.root
    }

    //Create the Tab View with all the categories of items
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoriesFragments = arrayListOf(
            AllFragment(),
            TshirtsFragment(),
            DressesFragment(),
            JacketsFragment(),
            TrousersFragment(),
            SkirtsFragment(),
            ShortsFragment(),
            SportswearFragment(),
            AccessoriesFragment()
        )

        //Set in each tab the appropriate title
        val viewPagerToAdapter =
            HomeViewPagerAdapter(categoriesFragments, childFragmentManager, lifecycle)
        binding.viewpagerHome.adapter = viewPagerToAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewpagerHome) { tab, position ->
            when (position) {
                0 -> tab.text = "All"
                1 -> tab.text = "T-shirts/Blouses"
                2 -> tab.text = "Dresses"
                3 -> tab.text = "Jackets"
                4 -> tab.text = "Trousers"
                5 -> tab.text = "Skirts"
                6 -> tab.text = "Shorts"
                7 -> tab.text = "Sportswear"
                8 -> tab.text = "Accessories"
            }
        }.attach()
    }
}