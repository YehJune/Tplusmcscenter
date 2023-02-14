package com.kct.tplusmcscenter.service.widget

//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import androidx.core.app.JobIntentService
//import com.kct.tplusmcscenter.model.data.requestAll1
//import com.kct.tplusmcscenter.utils.network.BaseResult
//import com.kct.tplusmcscenter.view.widget.SizeOneOneAppWidget
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import java.lang.Exception
//import javax.inject.Inject
//import kotlin.math.roundToLong
//
////@AndroidEntryPoint
//class RefreshDataIntentService : JobIntentService() {
//    companion object {
//        private const val TAG = "[RefreshDataService]"
//        private const val JOB_ID = 4010
//        const val ACTION_REFRESH_DATA = "com.test.ktc.widget.action.REFRESH_DATA"
//        const val ACTION_REFRESH_ERROR = "com.test.ktc.widget.action.REFRESH_ERROR"
//
//        internal fun enqueueWork(context: Context?, intent: Intent) {
//            enqueueWork(context!!, RefreshDataIntentService::class.java, JOB_ID, intent)
//        }
//    }
//
//    @Inject
//    lateinit var userDataRepository: UserDataRepository
//
//    override fun onHandleWork(intent: Intent) {
//        Log.d(TAG, "Entry onHandleWork")
//        networkConnection()
//    }
//
//    private fun networkConnection() = CoroutineScope(Dispatchers.Main).launch {
//        try{
//            when (val result = userDataRepository.getUserData(requestAll1)) {
//                is BaseResult.Success -> {
//                    var totalData = ""
//                    var totalCall = ""
//                    var totalSms = ""
//
//                    result.data.body[0].totaluseTimeDto.forEach {
//                        if (it.strBunGun == "P") {
//                            totalData = it.strFreeMinReMain
//                        }
//                    }
//                    totalCall = result.data.body[0].voiceCallDetailTotDto[0].iFreeMinRemainSum
//                    totalSms = result.data.body[0].totUseTimeCntTotDto[0].strFreeSmsRemain
//
//                    Log.d(TAG, "남은 무료 데이터: $totalData")
//                    Log.d(TAG, "남은 무료 통화: $totalCall")
//                    Log.d(TAG, "남은 무료 문자: $totalSms")
//
//                    val changeData: String = try{
//                        if (totalData.isNotEmpty()) {
//                            "${(totalData.toLong() * 0.5 / 100000).roundToLong() / 10.0}GB"
//                        } else {
//                            "-GB"
//                        }
//                    } catch (e: Exception) {
//                        Log.e(TAG, "Change Error: Data")
//                        "-GB"
//                    }
//
//                    val changeCall = try{
//                        if (totalData.isNotEmpty()) {
//                            "${(totalCall.toLong() / 60.0).roundToLong()}분"
//                        } else {
//                            "-분"
//                        }
//                    } catch (e: Exception) {
//                        Log.e(TAG, "Change Error: Call")
//                        "-분"
//                    }
//
//                    val changeSms = if(totalSms.isNotEmpty()) "${totalSms}건" else "-건"
//
//                    val actionIntent = Intent(ACTION_REFRESH_DATA)
//                    actionIntent.run {
//                        component = ComponentName(applicationContext, SizeOneOneAppWidget::class.java)
//                        putExtra("data", changeData)
//                        putExtra("call", changeCall)
//                        putExtra("sms", changeSms)
//                    }
//                    sendBroadcast(actionIntent)
//                }
//                is BaseResult.Error -> {
//                    Log.e(TAG, "Error Code: ${result.error.code}, Error Message: ${result.error.message}")
//                    defaultValueBroadcast()
//                }
//            }
//        } catch (e: Exception) {
//            val actionIntent = Intent(ACTION_REFRESH_ERROR)
//            actionIntent.run {
//                component = ComponentName(applicationContext, SizeOneOneAppWidget::class.java)
//            }
//            sendBroadcast(actionIntent)
//        }
//    }
//
//    private fun defaultValueBroadcast() {
//        val actionIntent = Intent(ACTION_REFRESH_DATA)
//        actionIntent.run {
//            component = ComponentName(applicationContext, SizeOneOneAppWidget::class.java)
//            putExtra("data", "-GB")
//            putExtra("call", "-분")
//            putExtra("sms", "-건")
//        }
//        sendBroadcast(actionIntent)
//    }
//}