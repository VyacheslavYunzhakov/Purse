package com.example.koshelek.api


import com.example.koshelek.model.BidsAsks
import com.example.koshelek.ui.bidask.BidAskFragment
import com.example.koshelek.ui.details.DetailsFragment
import com.example.koshelek.ui.details.DetailsViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BidsAsksRetriever {
    private val service: BidsAsksService

    companion object {
        const val BASE_URL = "https://api.binance.com/api/v3/"
    }

    init {
        val retrofit = Retrofit.Builder()
            // 1
            .baseUrl(BASE_URL)
            //.addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(BidsAsksService::class.java)
    }

    suspend fun getBidsAsks(): BidsAsks {
        return  service.retrieveBidsAsks(BidAskFragment.SYMBOL.replace("/", ""), 500)
    }
    suspend fun getBidsAsksDetails(): BidsAsks {
        return  service.retrieveBidsAsks(DetailsViewModel.SYMBOL.replace("/", ""), 500)
    }
}
