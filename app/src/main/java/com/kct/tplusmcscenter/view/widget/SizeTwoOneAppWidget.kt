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
import com.kct.tplusmcscenter.service.widget.UserDataTwoOneIntentService
import com.kct.tplusmcscenter.view.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SizeTwoOneAppWidget : AppWidgetProvider() {
    companion object {
        private const val TAG = "[STOWidget]"
        const val WIDGET_BUTTON = "com.test.ktc.widget.action.WIDGET_REFRESH_BUTTON_TWO_ONE"

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            loadingUI(context)
        }

        private fun loadingUI(context: Context) {
            val views = RemoteViews(context.packageName, R.layout.size_two_one_app_widget)
            views.run {
                setViewVisibility(R.id.two_one_text_loading, View.VISIBLE)
                setViewVisibility(R.id.two_one_text_error, View.GONE)
                setViewVisibility(R.id.two_one_layout_data, View.GONE)
                setViewVisibility(R.id.two_one_layout_sub, View.GONE)
            }
            AppWidgetManager.getInstance(context).updateAppWidget(
                AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, SizeTwoOneAppWidget::class.java)),
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
            updateAppWidget(context, appWidgetManager, appWidgetId)
            UserDataTwoOneIntentService.enqueueWork(context, Intent())
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        intent?.let {
            when(it.action) {
                UserDataTwoOneIntentService.ACTION_UPDATE_DATA -> {
                    Log.d(TAG, "Update Data")
                    applyUI(
                        context!!,
                        intent.getStringExtra("data") ?: "-",
                        intent.getStringExtra("call") ?: "-",
                        intent.getStringExtra("sms") ?: "-"
                    )
                }
                UserDataTwoOneIntentService.ACTION_UPDATE_ERROR -> {
                    Log.d(TAG, "Update Error")
                    errorUI(context!!)
                }
                WIDGET_BUTTON -> {
                    Log.d(TAG, "Action Widget Refresh Button")
                    loadingUI(context!!)
                    UserDataTwoOneIntentService.enqueueWork(context, Intent())
                }
                else -> {
                    Log.d(TAG, intent.action.toString())
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private fun errorUI(context: Context) {
        val views = RemoteViews(context.packageName, R.layout.size_two_one_app_widget)
        views.run {
            setViewVisibility(R.id.two_one_text_loading, View.GONE)
            setViewVisibility(R.id.two_one_text_error, View.VISIBLE)
            setViewVisibility(R.id.two_one_layout_data, View.GONE)
            setViewVisibility(R.id.two_one_layout_sub, View.GONE)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(
            AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, SizeTwoOneAppWidget::class.java)),
            views
        )
    }

    private fun applyUI(
        context: Context,
        data: String,
        call: String,
        sms: String
    ) {
        val views = RemoteViews(context.packageName, R.layout.size_two_one_app_widget)
        views.run {
            setViewVisibility(R.id.two_one_text_loading, View.GONE)
            setViewVisibility(R.id.two_one_text_error, View.GONE)
            setViewVisibility(R.id.two_one_layout_data,View.VISIBLE)
            setViewVisibility(R.id.two_one_layout_sub, View.VISIBLE)

            setTextViewText(R.id.two_one_text_data_amount, data)
            setTextViewText(R.id.two_one_text_call_amount, call)
            setTextViewText(R.id.two_one_text_sms_amount, sms)

            val btnRefreshIntent = Intent(context, SizeTwoOneAppWidget::class.java)
            btnRefreshIntent.action = WIDGET_BUTTON
            val pendingIntent = PendingIntent.getBroadcast(context, 0, btnRefreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            setOnClickPendingIntent(R.id.two_one_src_refresh, pendingIntent)

            val btnHomeIntent = Intent(context, MainActivity::class.java)
            val pendingHomeIntent = PendingIntent.getActivity(context, 0, btnHomeIntent, 0 or PendingIntent.FLAG_IMMUTABLE)
            setOnClickPendingIntent(R.id.two_one_src_home, pendingHomeIntent)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(
            AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, SizeTwoOneAppWidget::class.java)),
            views
        )
    }

}
