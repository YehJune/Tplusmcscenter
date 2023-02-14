package com.kct.tplusmcscenter.service.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.kct.tplusmcscenter.network.dto.widget.request.RequestWidgetAll
import com.kct.tplusmcscenter.network.dto.widget.request.RequestWidgetBody
import com.kct.tplusmcscenter.network.dto.widget.request.RequestWidgetHeader
import com.kct.tplusmcscenter.network.repository.WidgetRepository
import com.kct.tplusmcscenter.utils.KeyManager
import com.kct.tplusmcscenter.utils.network.BaseResult
import com.kct.tplusmcscenter.view.widget.SizeTwoOneAppWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class UserDataTwoOneIntentService : JobIntentService() {
    companion object {
        private const val TAG = "[UserDataService]"
        private const val JOB_ID = 4006
        const val ACTION_UPDATE_DATA = "com.test.ktc.widget.action.UPDATE_DATA_TWO_ONE"
        const val ACTION_UPDATE_ERROR = "com.test.ktc.widget.action.UPDATE_ERROR_TWO_ONE"

        internal fun enqueueWork(context: Context?, intent: Intent) {
            enqueueWork(context!!, UserDataTwoOneIntentService::class.java, JOB_ID, intent)
        }
    }

    @Inject
    lateinit var widgetRepository: WidgetRepository

    override fun onHandleWork(intent: Intent) {
        Log.d(TAG, "Entry onHandleWork")
        networkConnection()
    }

    private fun networkConnection() = CoroutineScope(Dispatchers.IO).launch {
        val key = KeyManager.getKey(applicationContext).replace("+","%2B").replace("&","%26").replace("=","%3D")
        val request = RequestWidgetAll(
            header = listOf(
                RequestWidgetHeader(
                    type = "01"
                )
            ),
            body = listOf(
                RequestWidgetBody(
                    traceno = "X15202211101164800248",
                    device = "A",
                    keyValue = key
                )
            )
        )
        try{
            when (val result = widgetRepository.getWidgetData(request)) {
                is BaseResult.Success -> {
                    sendBroadcast(
                        Intent(ACTION_UPDATE_DATA).apply {
                            component = ComponentName(applicationContext, SizeTwoOneAppWidget::class.java)
                            putExtra("data", result.data.body[0].data) //.replace("MB", "")
                            putExtra("call", result.data.body[0].voice) //.replace("분", "")
                            putExtra("sms", result.data.body[0].sms)
                            putExtra("smspercent", result.data.body[0].smspercent)
                            putExtra("voicepercent", result.data.body[0].voicepercent)
                            putExtra("datapercent", result.data.body[0].datapercent)
                        }
                    )
                }
                is BaseResult.Error -> {
                    Log.e(TAG, "Error Code: ${result.error.code}, Error Message: ${result.error.message}")
                    defaultValueBroadcast()
                }
            }
        } catch (e: Exception) {
            sendBroadcast(
                Intent(ACTION_UPDATE_ERROR).apply {
                    component = ComponentName(applicationContext, SizeTwoOneAppWidget::class.java)
                }
            )
        }
    }

    private fun defaultValueBroadcast() {
        sendBroadcast(
            Intent(ACTION_UPDATE_DATA).apply {
                component = ComponentName(applicationContext, SizeTwoOneAppWidget::class.java)
                putExtra("data", "-")
                putExtra("call", "-")
                putExtra("sms", "-")
            }
        )
    }
}