package com.kct.tplusmcscenter.network.dto.widget.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestWidgetHeader(
    @SerializedName("type")
    @Expose
    val type: String
)