package com.example.koshelek


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.koshelek.ui.adapters.ScreenSlidePagerAdapter
import com.example.koshelek.ui.bidask.BidAskFragment
import com.example.koshelek.ui.details.DetailsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        nav_view.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                nav_view.menu.getItem(position).isChecked = true
                setBottomNavigationViewItemColor(position)
            }
        })
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_bid -> {
                viewPager.currentItem = 0
                setBottomNavigationViewItemColor(0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_ask -> {
                viewPager.currentItem = 1
                setBottomNavigationViewItemColor(1)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_details -> {
                viewPager.currentItem = 2
                setBottomNavigationViewItemColor(2)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }


    fun setBottomNavigationViewItemColor(position: Int){
        when(position){
            0 -> {
                nav_view.itemIconTintList = ContextCompat.getColorStateList(this, R.color.color_bid)
                nav_view.itemTextColor = ContextCompat.getColorStateList(this, R.color.color_bid)
            }
            1 -> {
                nav_view.itemIconTintList = ContextCompat.getColorStateList(this, R.color.color_ask)
                nav_view.itemTextColor = ContextCompat.getColorStateList(this, R.color.color_ask)
            }
            2 -> {
                nav_view.itemIconTintList = ContextCompat.getColorStateList(this, R.color.color_details)
                nav_view.itemTextColor = ContextCompat.getColorStateList(this, R.color.color_details)
            }
        }
    }

}
