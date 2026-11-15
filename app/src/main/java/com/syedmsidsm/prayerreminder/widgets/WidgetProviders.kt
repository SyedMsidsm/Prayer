package com.syedmsidsm.prayerreminder.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle

abstract class BaseWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) =
        WidgetUpdater.updateAll(context)

    override fun onAppWidgetOptionsChanged(context: Context, mgr: AppWidgetManager, id: Int, options: Bundle) =
        WidgetUpdater.updateAll(context)

    override fun onEnabled(context: Context) = WidgetUpdater.updateAll(context)
    override fun onDisabled(context: Context) = WidgetScheduler.cancel(context)
}

class SmallWidgetProvider : BaseWidgetProvider()
class MediumWidgetProvider : BaseWidgetProvider()
class LargeWidgetProvider : BaseWidgetProvider()
