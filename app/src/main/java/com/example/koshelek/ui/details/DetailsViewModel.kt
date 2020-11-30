package com.example.koshelek.ui.details



import android.app.Activity
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.koshelek.MainActivity
import com.example.koshelek.api.BidsAsksRetriever
import com.example.koshelek.model.Details
import com.example.koshelek.model.WsBidsAsks
import com.example.koshelek.ui.bidask.BidAskFragment
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.*
import javax.net.ssl.SSLSocketFactory
import kotlin.collections.ArrayList

class DetailsViewModel : ViewModel() {

    var bidsOrAsksWithDiff: MutableList<MutableList<String>> = ArrayList()
    var bidsWithDiff: MutableList<MutableList<String>> = ArrayList()
    var asksWithDiff: MutableList<MutableList<String>> = ArrayList()
    private lateinit var bidsForReplace: MutableList<MutableList<String>>
    private lateinit var asksForReplace: MutableList<MutableList<String>>

    var bidDetailsList: MutableLiveData<Details> = MutableLiveData()
    var askDetailsList: MutableLiveData<Details> = MutableLiveData()

    lateinit var activity: FragmentActivity
    companion object {
        var SYMBOL = "BTCUSDT"
        var WEB_SOCKET_URL = "wss://stream.binance.com:9443/ws/${SYMBOL.replace("/", "")}@depth"
        const val TAG = "Coinbase"
    }
    private lateinit var webSocketClient: WebSocketClient

    suspend fun initBids() {
        bidsWithDiff.clear()
        bidsForReplace = BidsAsksRetriever().getBidsAsksDetails().bids
    }
    suspend fun initAsks() {
        asksWithDiff.clear()
        asksForReplace = BidsAsksRetriever().getBidsAsksDetails().asks
    }

    fun updateDetails(bidsAsksWs: WsBidsAsks){
        bidsForReplace = updateTable(bidsAsksWs.b, bidsForReplace, "Bids")
        if(bidsWithDiff.size >= 100){
            bidsWithDiff = bidsWithDiff.slice(0..99) as MutableList
        }
        bidDetailsList.value = Details(bidsWithDiff)

        asksForReplace = updateTable(bidsAsksWs.a, asksForReplace, "Asks")
        if(asksWithDiff.size >= 100){
            asksWithDiff = asksWithDiff.slice(0..99) as MutableList
        }
        askDetailsList.value = Details(asksWithDiff)
    }

        private fun updateTable(ab: MutableList<MutableList<String>>,
                                bidsOrAsksForReplace:MutableList<MutableList<String>>,
                                type:String): MutableList<MutableList<String>> {
            bidsOrAsksWithDiff.clear()
            for (j in 0 until ab.size) {
                var elementExist = false
                for (i in 0 until bidsOrAsksForReplace.size){
                    if (bidsOrAsksForReplace[i][0] == ab[j][0] && bidsOrAsksForReplace[i][1] != ab[j][1]){
                        bidsOrAsksWithDiff.add(mutableListOf(ab[j][0]
                                , (ab[j][1].toFloat()-bidsOrAsksForReplace[i][1].toFloat()).toString()))
                        bidsOrAsksForReplace[i] = ab[j]
                        elementExist = true
                    }
                }
                if (!elementExist){
                    bidsOrAsksForReplace.add(ab[j])
                    bidsOrAsksWithDiff.add(mutableListOf(ab[j][0], ab[j][1]))
                }
            }

            bidsOrAsksWithDiff = bidsOrAsksWithDiff.filter { item -> item[1].toFloat() != 0.0f} as MutableList<MutableList<String>>
            when(type){
                "Asks" -> asksWithDiff.addAll(0,(bidsOrAsksWithDiff.sortedBy {ask -> ask[0].toFloat()}
                        as MutableList).distinct())
                "Bids" -> bidsWithDiff.addAll(0,(bidsOrAsksWithDiff.sortedBy {bid -> bid[0].toFloat()}
                        .reversed().distinct()) as MutableList)
            }
            return bidsOrAsksForReplace
    }
    fun initWebSocket() {
        val coinbaseUri: URI? = URI(DetailsFragment.WEB_SOCKET_URL)

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
                        "  \"id\": 2\n" +
                        "}"
        )
    }

    private fun subscribe() {
        webSocketClient.send("{\n" +
                "  \"method\": \"SUBSCRIBE\",\n" +
                "  \"params\": [\n" +
                " \"${SYMBOL.replace("/", "").toLowerCase(Locale.ROOT)}@depth\"\n" +
                "  ],\n" +
                "  \"id\": 2\n" +
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
                updateDetails(bidsAsks!!)
            }
        }
    }
    fun closeWebSocket(){
        webSocketClient.close()
    }
}



