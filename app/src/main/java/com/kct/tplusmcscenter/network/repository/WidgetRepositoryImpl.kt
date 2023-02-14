package com.kct.tplusmcscenter.network.repository

import android.util.Log
import com.kct.tplusmcscenter.network.dto.widget.request.RequestWidgetAll
import com.kct.tplusmcscenter.network.dto.widget.response.ResponseWidgetAll
import com.kct.tplusmcscenter.network.remote.WidgetRemoteSource
import com.kct.tplusmcscenter.utils.network.BaseResult
import com.kct.tplusmcscenter.model.data.Failure
import retrofit2.HttpException
import javax.inject.Inject

class WidgetRepositoryImpl
@Inject
constructor(
    private val widgetRemoteSource: WidgetRemoteSource
) : WidgetRepository {
    companion object {
        private const val TAG = "[WidgetImpl]"
    }

    override suspend fun getWidgetData(
        requestWidgetAll: RequestWidgetAll
    ): BaseResult<ResponseWidgetAll, Failure>
    = try {
        val response: ResponseWidgetAll = widgetRemoteSource.getWidgetData(requestWidgetAll)
        response.run {
            when (header[0].result) {
                "Y" -> {
                    Log.d(TAG, "Success Api Connection")
                    BaseResult.Success(this)
                }
                "N" -> {
                    Log.e(TAG, "Error Code: ${header[0].resultCd}, Error Message: ${header[0].resultMsg}")
                    BaseResult.Error(Failure(header[0].resultCd, header[0].resultMsg))
                }
                else -> {
                    BaseResult.Error(Failure("404", "Not Found Error Code/Message"))
                }
            }
        }
    } catch (e: HttpException) {
        Log.e(TAG, "Exception", e)
        BaseResult.Error(Failure(e.code().toString(), e.message.toString()))
    } catch (e: Exception) {
        Log.e(TAG, "Exception", e)
        BaseResult.Error(Failure("-1", e.message.toString()))
    }


}