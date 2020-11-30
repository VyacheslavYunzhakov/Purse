package com.example.koshelek.api


import com.example.koshelek.model.BidsAsks
import retrofit2.http.GET
import retrofit2.http.Query

interface BidsAsksService {
    @GET("depth")
    suspend fun retrieveBidsAsks(@Query("symbol") symbol:String, @Query("limit") limit:Int): BidsAsks
}
