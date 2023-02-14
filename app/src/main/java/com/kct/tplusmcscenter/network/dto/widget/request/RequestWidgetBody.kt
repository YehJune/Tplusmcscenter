package com.kct.tplusmcscenter.network.dto.widget.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestWidgetBody(

    @SerializedName("traceno")
    @Expose
    val traceno: String,

    @SerializedName("device")
    @Expose
    val device: String,

    @SerializedName("keyvalue")
    @Expose
    val keyValue: String

)
