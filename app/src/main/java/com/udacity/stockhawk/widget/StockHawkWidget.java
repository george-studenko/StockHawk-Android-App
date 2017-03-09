package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;

import timber.log.Timber;

/**
 * Created by george on 20/02/2017.
 */

public class StockHawkWidget extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        // TODO: Widget. Handle the broadcast and update the widget instances

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        Timber.e("CALLING ON UPDATE HERE!");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    //ok
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        Timber.e("SETTING REMOTE VIEW HERE!");
        // Set up the collection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            views.setRemoteAdapter(R.id.widget_list,
                    new Intent(context, StockHawkWidgetService.class));
        } else {
            views.setRemoteAdapter(0, R.id.widget_list,
                    new Intent(context, StockHawkWidgetService.class));
        }
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
