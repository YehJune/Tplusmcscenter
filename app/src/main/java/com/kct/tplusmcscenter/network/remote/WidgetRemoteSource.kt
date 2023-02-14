package com.kct.tplusmcscenter.network.remote

import com.kct.tplusmcscenter.network.api.WidgetAPI
import com.kct.tplusmcscenter.network.dto.widget.request.RequestWidgetAll
import com.kct.tplusmcscenter.network.dto.widget.response.ResponseWidgetAll
import javax.inject.Inject

class WidgetRemoteSource
@Inject
constructor(
    private val widgetAPI: WidgetAPI
) {
    companion object {
        private const val TAG = "[WidgetRS]"
    }

    suspend fun getWidgetData(requestWidgetAll: RequestWidgetAll): ResponseWidgetAll = widgetAPI.getHiltUserData(requestWidgetAll)

}