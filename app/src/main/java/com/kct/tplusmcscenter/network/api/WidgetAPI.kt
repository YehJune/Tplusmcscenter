package com.kct.tplusmcscenter.network.api

import com.kct.tplusmcscenter.network.dto.widget.request.RequestWidgetAll
import com.kct.tplusmcscenter.network.dto.widget.response.ResponseWidgetAll
import retrofit2.http.Body
import retrofit2.http.POST

interface WidgetAPI {

    @POST("https://ncscenter.tplusmobile.com/common/api/widget.aspx")
    suspend fun getHiltUserData(
        @Body requestWidgetAll: RequestWidgetAll
    ): ResponseWidgetAll
}