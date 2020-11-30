package com.example.koshelek.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WsBidsAsks(
    val b : MutableList<MutableList<String>>,
    val a : MutableList<MutableList<String>>
)
@JsonClass(generateAdapter = true)
data class AAsks(
        val a : MutableList<MutableList<String>>
)