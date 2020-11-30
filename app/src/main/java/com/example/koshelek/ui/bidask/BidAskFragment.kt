package com.example.koshelek.ui.bidask

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koshelek.R
import com.example.koshelek.ui.adapters.BidsAsksListAdapter
import kotlinx.android.synthetic.main.fragment_bid_ask.*
import kotlinx.coroutines.*
import java.util.*

class BidAskFragment: Fragment(), AdapterView.OnItemSelectedListener {

    private val bidAskJob = Job()
    lateinit var  coroutine: Job

    companion object {
        var SYMBOL = "BTCUSDT"
        const val TAG = "Coinbase"
        const val TYPE_ARGUMENT = "TYPE_ARGUMENT"
        fun newInstance(type: String) : Fragment {
            val fragment = BidAskFragment()
            val args = Bundle()
            args.putString(TYPE_ARGUMENT, type);
            fragment.arguments = args;
            fragment.requireArguments().getString(TYPE_ARGUMENT)?.let { Log.d("myLogs", it) }
                return fragment
            }
        }

    private lateinit var bidAskViewModel: BidAskViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        bidAskViewModel = ViewModelProvider(this).get(BidAskViewModel::class.java)
        return inflater.inflate(R.layout.fragment_bid_ask, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ArrayAdapter.createFromResource(
                activity?.applicationContext!!,
                R.array.symbols_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            symbol_spinner.adapter = adapter
        }
        bidAskViewModel.activity= activity as FragmentActivity
        Log.d("myLogs", "getType() = ${arguments?.getString(TYPE_ARGUMENT).toString()}, BidAskViewModel.type ${BidAskViewModel.type}")
        symbol_spinner.onItemSelectedListener = this
        spinner_imageView.setOnClickListener{symbol_spinner.performClick()}
        bidsAsksList.layoutManager = LinearLayoutManager(this.requireContext())
        bidsAsksList.adapter = BidsAsksListAdapter()
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        try {
            bidAskViewModel.closeWebSocket()
        }catch (ex: Exception){}
        try {
            coroutine.cancel()
        }catch (ex: Exception){}
        symbol_spinner.setSelection(resources.getStringArray(R.array.symbols_array).indexOf(SYMBOL))
        amountTitleView.text = "Amount ${SYMBOL.substring(0..2)}"
        priceTitleView.text = "Price in ${SYMBOL.substring(4)}"
        retrieveBidsAndAsks(bidAskViewModel)
        BidAskViewModel.type = getType()
        bidAskViewModel.initWebSocket()
    }

    private fun retrieveBidsAndAsks(bidAskViewModel: BidAskViewModel) {
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            AlertDialog.Builder(this.requireContext()).setTitle("Error")
                    .setMessage(exception.message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert).show()
        }
        val coroutineScope = CoroutineScope(bidAskJob + Dispatchers.Main)
        coroutine = coroutineScope.launch(errorHandler) {
            when(getType()){
                "Bids" -> bidAskViewModel.initBids()
                "Asks" -> bidAskViewModel.initAsks()
            }
            bidAskViewModel.bidsOrAsksList.observe(viewLifecycleOwner, {
                it?.let {
                    when (getType()) {
                        "Bids" -> (bidsAsksList.adapter as BidsAsksListAdapter).refreshBids(it)
                        "Asks" -> (bidsAsksList.adapter as BidsAsksListAdapter).refreshAsks(it)
                    }
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            bidAskViewModel.closeWebSocket()
        }catch (ex: Exception){}
        try {
            coroutine.cancel()
        }catch (ex: Exception){}
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        SYMBOL = resources.getStringArray(R.array.symbols_array)[position]
        BidAskViewModel.SYMBOL = resources.getStringArray(R.array.symbols_array)[position]
        onPause()
        onResume()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
    fun getType() = arguments?.getString(TYPE_ARGUMENT).toString()
}