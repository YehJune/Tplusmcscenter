package com.kct.tplusmcscenter.view.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.kct.tplusmcscenter.R
import com.kct.tplusmcscenter.service.widget.UserDataThreeTwoIntentService
import com.kct.tplusmcscenter.view.main.MainActivity

class SizeThreeTwoAppWidget : AppWidgetProvider() {
    companion object {
        private const val TAG = "[STTWidget]"
        const val WIDGET_BUTTON = "com.test.ktc.widget.action.WIDGET_REFRESH_BUTTON_THREE_TWO"

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            loadingUI(context)
        }

        private fun loadingUI(context: Context) {
            val views = RemoteViews(context.packageName, R.layout.size_three_two_app_widget)
            views.run {
                setViewVisibility(R.id.three_two_text_loading, View.VISIBLE)
                setViewVisibility(R.id.three_two_text_error, View.GONE)
                setViewVisibility(R.id.three_two_layout_data, View.GONE)
            }
            AppWidgetManager.getInstance(context).updateAppWidget(
                AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, SizeThreeTwoAppWidget::class.java)),
                views
            )
        }
    }

    override fun onUpdate(
    context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
            UserDataThreeTwoIntentService.enqueueWork(context, Intent())
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        intent?.let {
            when(it.action) {
                UserDataThreeTwoIntentService.ACTION_UPDATE_DATA -> {
                    Log.d(TAG, "Widget Debug:  Update Data")
                    applyUI(
                        context!!,
                        intent.getStringExtra("mdn") ?: "-",
                        intent.getStringExtra("data") ?: "-GB",
                        intent.getStringExtra("voice") ?: "-분",
                        intent.getStringExtra("sms") ?: "-건",
                        intent.getIntExtra("smspercent",100) ?: 100,
                        intent.getIntExtra("voicepercent",100) ?: 100,
                        intent.getIntExtra("datapercent",100) ?: 100

                        )
                }
                UserDataThreeTwoIntentService.ACTION_UPDATE_ERROR -> {
                    Log.d(TAG, "Update Error")
                    errorUI(context!!)
                }
                WIDGET_BUTTON -> {
                    Log.d(TAG, "Action Widget Button")
                    loadingUI(context!!)
                    UserDataThreeTwoIntentService.enqueueWork(context, Intent())
                }
                else -> { Log.d(TAG, intent.action.toString()) }
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val minWidth = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val maxWidth = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        val minHeight = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val maxHeight = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        //val maxtextLen = maxWidth / currentTextSize * (maxHeight / currentTextSize)

        //val isFit = maxtextLen > textInWidgetTextView.length()

    }

    private fun errorUI(context: Context) {
        val views = RemoteViews(context.packageName, R.layout.size_three_two_app_widget)
        views.run {
            setViewVisibility(R.id.three_two_text_loading, View.GONE)
            setViewVisibility(R.id.three_two_text_error, View.VISIBLE)
            setViewVisibility(R.id.three_two_layout_data, View.GONE)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(
            AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, SizeThreeTwoAppWidget::class.java)),
            views
        )
    }

    private fun applyUI(
        context: Context,
        mdn: String,
        data: String,
        call: String,
        sms: String,
        smspercent:Int,
        voicepercent:Int,
        datapercent:Int,

    ) {
        val views = RemoteViews(context.packageName, R.layout.size_three_two_app_widget)
        views.run {
            setViewVisibility(R.id.three_two_text_loading, View.GONE)
            setViewVisibility(R.id.three_two_text_error, View.GONE)

            setViewVisibility(R.id.three_two_layout_data, View.VISIBLE)

            setTextViewText(R.id.three_two_text_mdn, mdn)

            setTextViewText(R.id.three_two_text_call, call)
            setTextViewText(R.id.three_two_text_sms, sms)
            setTextViewText(R.id.three_two_text_data, data)

            setProgressBar(R.id.three_two_progress_call,100,voicepercent,false)
            setProgressBar(R.id.three_two_progress_sms,100,smspercent,false)
            setProgressBar(R.id.three_two_progress_data,100,datapercent,false)


            val btnRefreshIntent = Intent(context, SizeThreeTwoAppWidget::class.java)
            btnRefreshIntent.action = WIDGET_BUTTON
            val pendingIntent = PendingIntent.getBroadcast(context, 0, btnRefreshIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            setOnClickPendingIntent(R.id.three_two_src_refresh, pendingIntent)

            val btnHomeIntent = Intent(context, MainActivity::class.java)
            val pendingHomeIntent = PendingIntent.getActivity(context, 0, btnHomeIntent, 0 or PendingIntent.FLAG_IMMUTABLE)
            setOnClickPendingIntent(R.id.three_two_src_home, pendingHomeIntent)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(
            AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, SizeThreeTwoAppWidget::class.java)),
            views
        )
    }


}