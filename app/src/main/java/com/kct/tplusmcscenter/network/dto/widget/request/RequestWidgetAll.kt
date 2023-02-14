package com.kct.tplusmcscenter.network.dto.widget.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestWidgetAll(

    @SerializedName("header")
    @Expose
    val header: List<RequestWidgetHeader>,

    @SerializedName("body")
    @Expose
    val body: List<RequestWidgetBody>

)