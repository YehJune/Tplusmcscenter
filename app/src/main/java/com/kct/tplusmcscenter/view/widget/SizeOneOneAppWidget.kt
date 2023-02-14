package com.kct.tplusmcscenter.view.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.kct.tplusmcscenter.R
import com.kct.tplusmcscenter.service.widget.UserDataOneOneIntentService

class SizeOneOneAppWidget : AppWidgetProvider() {
    companion object {
        private const val TAG = "[SOOWidget]"
        const val WIDGET_BUTTON = "com.test.ktc.widget.action.WIDGET_REFRESH_BUTTON_ONE_ONE"
//        private const val WIDGET_UPDATE_INTERVAL = 30000
//        private var sender: PendingIntent? = null
//        private var manager: AlarmManager? = null

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            loadingUI(context)
        }

        private fun loadingUI(context: Context) {
            val views = RemoteViews(context.packageName, R.layout.size_one_one_app_widget)
            views.run {
                setViewVisibility(R.id.one_one_src_loading, View.VISIBLE)

                setViewVisibility(R.id.one_one_src_data, View.GONE)
                setViewVisibility(R.id.one_one_text_data, View.GONE)
                setViewVisibility(R.id.one_one_src_call, View.GONE)
                setViewVisibility(R.id.one_one_text_call, View.GONE)
                setViewVisibility(R.id.one_one_src_sms, View.GONE)
                setViewVisibility(R.id.one_one_text_sms, View.GONE)

                setViewVisibility(R.id.one_one_src_error, View.GONE)
            }
            AppWidgetManager.getInstance(context).updateAppWidget(
                AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, SizeOneOneAppWidget::class.java)),
                views
            )
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            Log.d(TAG, "Entry onUpdate")
            updateAppWidget(context, appWidgetManager, appWidgetId)
            UserDataOneOneIntentService.enqueueWork(context, Intent())
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        intent?.let {
            when (it.action) {
                UserDataOneOneIntentService.ACTION_UPDATE_DATA -> {
                    Log.d(TAG, "Refresh Data")
                    applyUI(
                        context!!,
                        intent.getStringExtra("data") ?: "-GB",
                        intent.getStringExtra("call") ?: "-분",
                        intent.getStringExtra("sms") ?: "-건",
                    )
                }
                UserDataOneOneIntentService.ACTION_UPDATE_ERROR -> {
                    Log.d(TAG, "Refresh Error")
                    errorUI(context!!)
                }
                WIDGET_BUTTON -> {
                    Log.d(TAG, "Action Widget Refresh Button")
                    loadingUI(context!!)
                    UserDataOneOneIntentService.enqueueWork(context, Intent())
                }
                //region alarm logic
//                "android.appwidget.action.APPWIDGET_UPDATE" -> {
//                    Log.d(TAG, "android.appwidget.action.APPWIDGET_UPDATE")
//                    removePreviousAlarm()
//                    RefreshDataIntentService.enqueueWork(context, intent)
//
//                    val fireTime = System.currentTimeMillis() + WIDGET_UPDATE_INTERVAL
//                    sender = PendingIntent.getBroadcast(context, 0, intent, 0 or PendingIntent.FLAG_IMMUTABLE)
//                    manager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//                    manager!!.set(AlarmManager.RTC, fireTime, sender)
//                }
//                "android.appwidget.action.APPWIDGET_DISABLED" -> {
//                    Log.d(TAG, "android.appwidget.action.APPWIDGET_DISABLED")
//                    removePreviousAlarm()
//                }
                //endregion
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private fun applyUI(
        context: Context,
        data: String,
        call: String,
        sms: String
    ) {
        val views = RemoteViews(context.packageName, R.layout.size_one_one_app_widget)
        views.run {
            setViewVisibility(R.id.one_one_src_loading, View.GONE)

            setViewVisibility(R.id.one_one_src_data, View.VISIBLE)
            setViewVisibility(R.id.one_one_text_data, View.VISIBLE)
            setViewVisibility(R.id.one_one_src_call, View.VISIBLE)
            setViewVisibility(R.id.one_one_text_call, View.VISIBLE)
            setViewVisibility(R.id.one_one_src_sms, View.VISIBLE)
            setViewVisibility(R.id.one_one_text_sms, View.VISIBLE)

            setViewVisibility(R.id.one_one_src_error, View.GONE)

            setTextViewText(R.id.one_one_text_data, data)
            setTextViewText(R.id.one_one_text_call, call)
            setTextViewText(R.id.one_one_text_sms, sms)

            val btnRefreshIntent = Intent(context, SizeOneOneAppWidget::class.java)
            btnRefreshIntent.action = WIDGET_BUTTON
            val pendingIntent = PendingIntent.getBroadcast(context, 0, btnRefreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            setOnClickPendingIntent(R.id.one_one_layout, pendingIntent)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(
            AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, SizeOneOneAppWidget::class.java)),
            views
        )
    }

    private fun errorUI(context: Context) {
        val views = RemoteViews(context.packageName, R.layout.size_one_one_app_widget)
        views.run {
            setViewVisibility(R.id.one_one_src_loading, View.GONE)

            setViewVisibility(R.id.one_one_src_data, View.GONE)
            setViewVisibility(R.id.one_one_text_data, View.GONE)
            setViewVisibility(R.id.one_one_src_call, View.GONE)
            setViewVisibility(R.id.one_one_text_call, View.GONE)
            setViewVisibility(R.id.one_one_src_sms, View.GONE)
            setViewVisibility(R.id.one_one_text_sms, View.GONE)

            setViewVisibility(R.id.one_one_src_error, View.VISIBLE)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(
            AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, SizeOneOneAppWidget::class.java)),
            views
        )
    }

    //region alarm logic
//    private fun removePreviousAlarm() {
//        manager?.let { nonNullManager ->
//            sender?.let {  nonNullSender ->
//                nonNullSender.cancel()
//                nonNullManager.cancel(sender)
//            }
//        }
//    }
    //endregion
}