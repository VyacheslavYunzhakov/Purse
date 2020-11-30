package com.example.koshelek.ui.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koshelek.R
import com.example.koshelek.model.Details
import kotlinx.android.synthetic.main.detail.view.*

class BidsListAdapter: RecyclerView.Adapter<BidsListAdapter.ViewHolder>() {
    private var bidsList: MutableList<MutableList<String>> = ArrayList()
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val greenColor = 0xff31c857
        private val redColor = 0xfffb362f
        fun bindDetails(bidsList: MutableList<String>, position: Int) {
            when{
                (bidsList[1]).toFloat() < 0.0f -> itemView.diffTextView.setTextColor(redColor.toInt())
                (bidsList[1]).toFloat() > 0.0f -> itemView.diffTextView.setTextColor(greenColor.toInt())
            }
            when(position%2){
                1 -> itemView.setBackgroundColor(0xfff7f7f7.toInt())
                0 -> itemView.setBackgroundColor(0xFFFFFFFF.toInt())
            }
            itemView.priceTextView.text = bidsList[0]
            itemView.diffTextView.text = bidsList[1]
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindDetails(bidsList[position], position)
    }

    override fun getItemCount(): Int =bidsList.size

    fun refreshDetails(details: Details) {
        this.bidsList = details.detail
        notifyDataSetChanged()
    }
}