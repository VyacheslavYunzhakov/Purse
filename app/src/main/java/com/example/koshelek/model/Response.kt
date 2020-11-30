package com.example.koshelek.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class BidsAsks(
    val bids : MutableList<MutableList<String>>,
    val asks : MutableList<MutableList<String>>
)

