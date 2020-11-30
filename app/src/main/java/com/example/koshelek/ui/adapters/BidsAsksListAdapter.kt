package com.example.koshelek.ui.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koshelek.R
import kotlinx.android.synthetic.main.bid_ask.view.*

class BidsAsksListAdapter() : RecyclerView.Adapter<BidsAsksListAdapter.ViewHolder>() {

    private var bidsAsks:MutableList<MutableList<String>> = ArrayList()
    private var type: String = ""
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val greenColor = 0xff31c857
        private val redColor = 0xfffb362f
        fun bindBidsOrAsks(bidsOrAsksList: MutableList<String>, type: String, position: Int) {
            if (type == "Bids") {
                itemView.priceTextView.setTextColor(greenColor.toInt())
            }
            else{
                itemView.priceTextView.setTextColor(redColor.toInt())
            }
            when(position%2){
                1 -> itemView.setBackgroundColor(0xfff7f7f7.toInt())
                0 -> itemView.setBackgroundColor(0xFFFFFFFF.toInt())
            }
            itemView.priceTextView.text = bidsOrAsksList[0]
            itemView.amountTextView.text = bidsOrAsksList[1]
            itemView.totalTextView.text = (bidsOrAsksList[0].toFloat() * bidsOrAsksList[1].toFloat()).toString()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bid_ask, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindBidsOrAsks(bidsAsks[position], type, position)
    }

    override fun getItemCount(): Int = bidsAsks.size

    fun refreshBids(bidsAsks: MutableList<MutableList<String>>) {
        this.bidsAsks = bidsAsks
        this.type = "Bids"
        notifyDataSetChanged()
    }
    fun refreshAsks(bidsAsks: MutableList<MutableList<String>>) {
        this.bidsAsks = bidsAsks
        this.type = "Asks"
        notifyDataSetChanged()
    }
}

