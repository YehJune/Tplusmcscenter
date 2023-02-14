package com.kct.tplusmcscenter.network.repository

import com.kct.tplusmcscenter.network.dto.widget.request.RequestWidgetAll
import com.kct.tplusmcscenter.network.dto.widget.response.ResponseWidgetAll
import com.kct.tplusmcscenter.utils.network.BaseResult
import com.kct.tplusmcscenter.model.data.Failure

interface WidgetRepository {
    suspend fun getWidgetData(requestWidgetAll: RequestWidgetAll) : BaseResult<ResponseWidgetAll, Failure>
}