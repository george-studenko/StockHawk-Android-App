package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.Stock;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static final int STOCK_LOADER = 0;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView stockRecyclerView;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error)
    TextView error;
    private StockAdapter adapter;
    private static boolean mInvalidStockSymbol=false;
    private static String mInvalidStockSymbolValue;

    @Override
    public void onClick(String symbol) {
        Timber.d("Symbol clicked: %s", symbol);
        Intent intent = new Intent(this,StockDetails.class);
        intent.putExtra("symbol",symbol);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if(networkUp()) {
            error.setVisibility(View.INVISIBLE);
            adapter = new StockAdapter(this, this);
            stockRecyclerView.setAdapter(adapter);
            stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            swipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();

            QuoteSyncJob.initialize(this);
            getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                    PrefUtils.removeStock(MainActivity.this, symbol);
                    getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
                }
            }).attachToRecyclerView(stockRecyclerView);

        }else {
            error.setText(getString(R.string.error_no_network));
            error.setVisibility(View.VISIBLE);
        }
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {

        QuoteSyncJob.syncImmediately(this);

        if (!networkUp() && adapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_network));
            error.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        } else if (PrefUtils.getStocks(this).size() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_stocks));
            error.setVisibility(View.VISIBLE);
        } else {
            error.setVisibility(View.GONE);
        }
    }

    public void button(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {
            if (networkUp()) {
                swipeRefreshLayout.setRefreshing(true);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
            PrefUtils.addStock(this, symbol.toUpperCase());
            QuoteSyncJob.syncImmediately(this);
        }
    }

    public static void setInvalidStockSymbol(boolean value){
        mInvalidStockSymbol=value;
    }
    public static void setmInvalidStockSymbolValue(String value){
        mInvalidStockSymbolValue=value;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);

        if (data.getCount() != 0) {
            error.setVisibility(View.GONE);
        }
        adapter.setCursor(data);

        if(mInvalidStockSymbol){
            String message =  getString(R.string.error_adding_invalid_symbol);
            message = mInvalidStockSymbolValue + " " + message;
            Toast.makeText(this,message,Toast.LENGTH_LONG).show();
            mInvalidStockSymbol=false;
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        item.setTitle(getResources().getString(R.string.action_change_units_content_description_dollar));
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(networkUp()) {
            int id = item.getItemId();

            if (id == R.id.action_change_units) {
                PrefUtils.toggleDisplayMode(this);
                setDisplayModeMenuItemIcon(item);

                // set the button title for screen readers
                String currentMode = PrefUtils.getDisplayMode(this);

                if (currentMode == getString(R.string.pref_display_mode_percentage_key)){
                    item.setTitle(getResources().getString(R.string.action_change_units_content_description_percentage));
                }else{
                    item.setTitle(getResources().getString(R.string.action_change_units_content_description_dollar));
                }

                adapter.notifyDataSetChanged();
                return true;
            }
        }else{
            Toast.makeText(this, R.string.no_internet_error_on_unit_change,Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
