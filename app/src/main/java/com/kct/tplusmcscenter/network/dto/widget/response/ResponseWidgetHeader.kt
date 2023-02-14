package com.kct.tplusmcscenter.network.dto.widget.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseWidgetHeader(

    @SerializedName("type")
    @Expose
    val type: String,

    @SerializedName("result")
    @Expose
    val result: String,

    @SerializedName("resultCd")
    @Expose
    val resultCd: String,

    @SerializedName("resultMsg")
    @Expose
    val resultMsg: String

)