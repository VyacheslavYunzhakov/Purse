package com.example.koshelek.ui.bidask

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.koshelek.api.BidsAsksRetriever
import com.example.koshelek.model.WsBidsAsks
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.util.*
import javax.net.ssl.SSLSocketFactory
import kotlin.collections.ArrayList

class BidAskViewModel : ViewModel() {

    private var bidsOrAsksForReplace : MutableList<MutableList<String>> = ArrayList()
    var bidsOrAsksList: MutableLiveData<MutableList<MutableList<String>>> = MutableLiveData()

    lateinit var activity: FragmentActivity

    companion object {
        var SYMBOL = "BTCUSDT"
        var WEB_SOCKET_URL = "wss://stream.binance.com:9443/ws/${SYMBOL.replace("/", "")}@depth"
        const val TAG = "Coinbase"
        var type = "Bids"
    }

    private lateinit var webSocketClient: WebSocketClient

    suspend fun initBids (){
        Log.d("myLogs","Im in initBids()")
            bidsOrAsksForReplace = BidsAsksRetriever().getBidsAsks().bids
            bidsOrAsksList.value = bidsOrAsksForReplace.sortedBy { bid -> bid[0] }.reversed().slice(0..99) as MutableList
    }

    suspend fun initAsks () {
        bidsOrAsksForReplace = BidsAsksRetriever().getBidsAsks().asks
        bidsOrAsksList.value = bidsOrAsksForReplace.sortedBy { ask -> ask[0] }.slice(0..99) as MutableList
    }

    fun updateBids(ab: MutableList<MutableList<String>>){
        updateTable(ab)
        bidsOrAsksForReplace = bidsOrAsksForReplace.filter { item -> item[1].toFloat() != 0.0f} as MutableList<MutableList<String>>
            bidsOrAsksList.value = bidsOrAsksForReplace.sortedBy { bidAsk -> bidAsk[0].toFloat() }.reversed()
                    .slice(0..99) as MutableList
    }
    fun updateAsks(ab: MutableList<MutableList<String>>){
        updateTable(ab)
        bidsOrAsksForReplace = bidsOrAsksForReplace.filter { item -> item[1].toFloat() != 0.0f} as MutableList<MutableList<String>>
        bidsOrAsksList.value = bidsOrAsksForReplace.sortedBy { bidAsk -> bidAsk[0].toFloat() }
                .slice(0..99) as MutableList
    }

    private fun updateTable(ab: MutableList<MutableList<String>>){
        for (j in 0 until ab.size) {
            var elementExist = false
            for (i in 0 until bidsOrAsksForReplace.size){
                if (bidsOrAsksForReplace[i][0] == ab[j][0]){
                    bidsOrAsksForReplace[i] = ab[j]
                    elementExist = true
                }
            }
            if (!elementExist){
                bidsOrAsksForReplace.add(ab[j])
            }
        }
    }

    fun initWebSocket() {
        val coinbaseUri: URI? = URI(WEB_SOCKET_URL)

        createWebSocketClient(coinbaseUri)
        val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        webSocketClient.setSocketFactory(socketFactory)
        webSocketClient.connect()
    }

    private fun createWebSocketClient(coinbaseUri: URI?) {
        webSocketClient = object : WebSocketClient(coinbaseUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(BidAskFragment.TAG, "onOpen")
                subscribe()
            }

            override fun onMessage(message: String?) {
                Log.d(BidAskFragment.TAG, "onMessage $message")
                if(!message?.contains("result")!!) {
                    setUpBid(message)
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(BidAskFragment.TAG, "onClose")
                unsubscribe()
            }

            override fun onError(ex: Exception?) {
                Log.e(BidAskFragment.TAG, "onError: ${ex?.message}")
            }

        }
    }

    private fun unsubscribe() {
        webSocketClient.send(
                "{\n" +
                        "  \"method\": \"UNSUBSCRIBE\",\n" +
                        "  \"params\": [\n" +
                        "    \"${SYMBOL.replace("/", "").toLowerCase(Locale.ROOT)}@depth\"\n" +
                        "  ],\n" +
                        "  \"id\": 1\n" +
                        "}"
        )
    }

    private fun subscribe() {
        webSocketClient.send("{\n" +
                "  \"method\": \"SUBSCRIBE\",\n" +
                "  \"params\": [\n" +
                " \"${SYMBOL.replace("/", "").toLowerCase(Locale.ROOT)}@depth\"\n" +
                "  ],\n" +
                "  \"id\": 1\n" +
                "}"
        )
    }

    private fun setUpBid(message: String?) {
        message?.let {
            val moshi = Moshi.Builder().build()
            val adapter: JsonAdapter<WsBidsAsks> = moshi.adapter(WsBidsAsks::class.java)
            val bidsAsks: WsBidsAsks?
            bidsAsks = adapter.fromJson(message)
            activity.runOnUiThread {
                if (bidsAsks != null && type =="Bids") {
                    updateBids(bidsAsks.b)
                    Log.d("myLogs","Im doing updateBids")
                }
                if (bidsAsks != null && type =="Asks") {
                    updateAsks(bidsAsks.a)
                    Log.d("myLogs","Im doing updateAsks")
                }
            }
        }
    }

    fun closeWebSocket(){
        webSocketClient.close()
    }

}