package com.example.koshelek.ui.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koshelek.R
import com.example.koshelek.model.WsBidsAsks
import com.example.koshelek.ui.adapters.AsksListAdapter
import com.example.koshelek.ui.adapters.BidsListAdapter
import com.example.koshelek.ui.bidask.BidAskFragment
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.coroutines.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.SSLSocketFactory


class DetailsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val bidsJob = Job()
    private val asksJob = Job()
    private lateinit var  coroutineBids: Job
    lateinit var coroutineAsks: Job
    private val progressBar: ProgressBar by lazy {progress_bar_loading}

    companion object {
        var SYMBOL = "BTCUSDT"
        var WEB_SOCKET_URL = "wss://stream.binance.com:9443/ws/${SYMBOL.replace("/", "")}@depth"
        const val TAG = "Coinbase"
    }
    private lateinit var detailsViewModel: DetailsViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        detailsViewModel = ViewModelProvider(this).get(DetailsViewModel::class.java)
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ArrayAdapter.createFromResource(
                activity?.applicationContext!!,
                R.array.symbols_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            symbol_spinner_details.adapter = adapter
        }
        symbol_spinner_details.onItemSelectedListener = this
        spinner_imageView.setOnClickListener{symbol_spinner_details.performClick()}
        bidsList.layoutManager = LinearLayoutManager(this.requireContext())
        asksList.layoutManager = LinearLayoutManager(this.requireContext())
        detailsViewModel.activity= activity as FragmentActivity
        bidsList.adapter = BidsListAdapter()
        asksList.adapter = AsksListAdapter()
        val someHandler = Handler(getMainLooper())
        someHandler.postDelayed(object : Runnable {
            override fun run() {
                textClock.text = SimpleDateFormat("dd/MM/yyyy - hh:mm:ss", Locale.US).format(Date())
                someHandler.postDelayed(this, 1000)
            }
        }, 10)
    }

    override fun onResume() {
        super.onResume()
        showLoading()
        try {
            detailsViewModel.closeWebSocket()
        }catch (ex: Exception){}
        try {
            coroutineBids.cancel()
        }catch (ex: Exception){}
        try {
            coroutineAsks.cancel()
        }catch (ex: Exception){}
        symbol_spinner_details.setSelection(resources.getStringArray(R.array.symbols_array).indexOf(SYMBOL))
        retrieveBids(detailsViewModel)
        retrieveAsks(detailsViewModel)
        detailsViewModel.initWebSocket()
    }

    private fun retrieveBids(detailsViewModel: DetailsViewModel) {
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            AlertDialog.Builder(this.requireContext()).setTitle("Error")
                    .setMessage(exception.message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert).show()
        }
        val coroutineScope = CoroutineScope(asksJob + Dispatchers.Main)
        coroutineBids = coroutineScope.launch(errorHandler) {
            detailsViewModel.initBids()
            detailsViewModel.bidDetailsList.observe(viewLifecycleOwner, {
                it?.let {
                    (bidsList.adapter as BidsListAdapter).refreshDetails(it)
                    hideLoading()
                }
            })
        }
    }
    private fun retrieveAsks(detailsViewModel: DetailsViewModel) {
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            AlertDialog.Builder(this.requireContext()).setTitle("Error")
                    .setMessage(exception.message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert).show()
        }
        val coroutineScope = CoroutineScope(bidsJob + Dispatchers.Main)
        coroutineAsks = coroutineScope.launch(errorHandler) {
            detailsViewModel.initAsks()
            detailsViewModel.askDetailsList.observe(viewLifecycleOwner, {
                it?.let {
                    (asksList.adapter as AsksListAdapter).refreshDetails(it)
                    hideLoading()
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            detailsViewModel.closeWebSocket()
        }catch (ex: Exception){}
        try {
            coroutineBids.cancel()
        }catch (ex: Exception){}
        try {
            coroutineAsks.cancel()
        }catch (ex: Exception){}
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        SYMBOL = resources.getStringArray(R.array.symbols_array)[position]
        DetailsViewModel.SYMBOL = resources.getStringArray(R.array.symbols_array)[position]
        onPause()
        onResume()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
    fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    fun hideLoading() {
        progressBar.visibility = View.GONE
    }
}