package com.kct.tplusmcscenter.network.dto.widget.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseWidgetAll(

    @SerializedName("header")
    @Expose
    val header: List<ResponseWidgetHeader>,

    @SerializedName("body")
    @Expose
    val body: List<ResponseWidgetBody>

)
