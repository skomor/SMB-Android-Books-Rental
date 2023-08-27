package com.example.smb.booksapp

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import android.app.PendingIntent
import android.app.Activity

import android.content.Intent







/**
 * Implementation of App Widget functionality.
 */
class AppWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val configIntent = Intent(context, MainActivity::class.java).let { intent ->
        PendingIntent.getActivity(context, 0, intent, 0)
    }

    val configIntent2 = Intent(context, MainActivity::class.java).let{intent ->
        intent.putExtra("openAudios", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.setAction("asdas")
        PendingIntent.getActivity(context, 0, intent, 0)
    }

    val views = RemoteViews(context.packageName, R.layout.app_widget)
    views.setOnClickPendingIntent(R.id.appwidget_Home, configIntent);
    views.setOnClickPendingIntent(R.id.appwidget_audio, configIntent2);

    appWidgetManager.updateAppWidget(appWidgetId, views)
}