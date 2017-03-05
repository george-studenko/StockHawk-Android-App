package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

import timber.log.Timber;

/**
 * Created by george on 26/02/2017.
 */

public class StockHawkWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Timber.e("returning RemoteViewFactory HERE!!");
        return new StockHawkWidgetDataProvider(this,intent);
    }
}
