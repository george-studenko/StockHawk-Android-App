package com.udacity.stockhawk.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;

import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import timber.log.Timber;

/**
 * Created by george on 26/02/2017.
 */

public class StockHawkWidgetDataProvider implements RemoteViewsService.RemoteViewsFactory // ,LoaderManager.LoaderCallbacks<Cursor>
{
    Context context;
    Intent intent;
    Cursor cursor;

    public StockHawkWidgetDataProvider(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
            ContentResolver resolver = context.getContentResolver();
            cursor = resolver.query(Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onDestroy() {
        if(cursor!=null) {
            this.cursor.close();
        }
    }

    @Override
    public int getCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Timber.e("FACTORY GET VIEW!");
        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_item);
        cursor.moveToPosition(position);
        remoteView.setTextViewText(R.id.widgetSymbol,cursor.getString(Contract.Quote.POSITION_SYMBOL));
        remoteView.setTextViewText(R.id.widgetPrice,"$"+ cursor.getString(Contract.Quote.POSITION_PRICE));
        String absoluteChange = "$"+cursor.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        remoteView.setTextViewText(R.id.widgetChange,absoluteChange.replace("$","+$").replace("+$-","-$"));
        if(absoluteChange.contains("-")){
            remoteView.setTextColor(R.id.widgetChange, Color.RED);
        }else {
            remoteView.setTextColor(R.id.widgetChange, Color.GREEN);
        }

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        long id = 0;
        cursor.moveToPosition(position);
        id = cursor.getLong(Contract.Quote.POSITION_ID);
        return id;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


//    //CursorLoader Methods
//    @Override
//    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        Timber.e("LOADER CREATED!");
//        return new CursorLoader(context,
//                Contract.Quote.URI,
//                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
//                null, null, Contract.Quote.COLUMN_SYMBOL);
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
//    {
//        Timber.e("LOADER FINISHED!");
//            this.cursor = data;
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//
//        //this.cursor = null;
//    }
}
