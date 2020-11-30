package com.example.koshelek.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.koshelek.ui.bidask.BidAskFragment
import com.example.koshelek.ui.details.DetailsFragment

class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    val NUM_PAGES = 3

    override fun getItemCount(): Int = NUM_PAGES

    override fun createFragment(position: Int): Fragment {

        lateinit var fragment:Fragment
        return when(position){
            0 ->  BidAskFragment.newInstance("Bids")
            1 ->  BidAskFragment.newInstance("Asks")
            2 ->  DetailsFragment()
            else ->   BidAskFragment.newInstance("Bids")
            }
        }
    }
