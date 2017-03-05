package com.udacity.stockhawk.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.github.mikephil.charting.utils.Utils;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.StockProvider;
import com.udacity.stockhawk.data.charts.CurrencyFormatter;
import com.udacity.stockhawk.data.charts.DateFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by george on 25/02/2017.
 */

public class StockDetails extends AppCompatActivity {

    @BindView(R.id.chart) LineChart chart;
    @BindView(R.id.stockDetailName) TextView stockName;
    @BindView(R.id.stockVal1) TextView stockVal1;
    @BindView(R.id.stockVal2) TextView stockVal2;
    @BindView(R.id.stockVal3) TextView stockVal3;
    @BindView(R.id.stockVal4) TextView stockVal4;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_details);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        String symbol = intent.getStringExtra("symbol");
        stockName.setText("Historical data chart for "+ symbol);

        ContentResolver contentResolver = getContentResolver();
        Uri uri = Contract.Quote.URI.buildUpon().appendPath(symbol).build();
        String[] params = new String[] {symbol};

        Cursor cursor = contentResolver.query(uri , null, null, params, null);
        cursor.moveToFirst();

        String history = cursor.getString(Contract.Quote.POSITION_HISTORY);
        stockVal1.setText("Current Price: "+cursor.getString(Contract.Quote.POSITION_PRICE)+" $");
        stockVal2.setText("Percentage Change: "+cursor.getString(Contract.Quote.POSITION_PERCENTAGE_CHANGE)+" %");
        stockVal3.setText("Absolute Change: "+cursor.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE));

        //stockVal4.setText("Price: "+cursor.getString(Contract.Quote.POSITION_));


        cursor.close();

        String[] historyParts = history.split("\n");

        Utils.init(this);
        List<Entry> entries = new ArrayList<Entry>();
        int x = historyParts.length;
        for (String data : historyParts) {
           // float x = Float.parseFloat(data.split(", ")[0]);
            float y = Float.parseFloat(data.split(", ")[1]);
            Entry entry = new Entry(x, y);

            entries.add(new Entry(x, y));

           x=x-1;
        }

        Collections.reverse(entries);

        LineDataSet dataset = new LineDataSet(entries,"Price");

        dataset.setCircleColor(Color.RED);
        dataset.setCircleColorHole(Color.GREEN);
        dataset.setDrawFilled(true);
        dataset.setFillColor(Color.GREEN);
        dataset.setValueTextColor(Color.WHITE);
        dataset.setValueTextSize(9);
        dataset.setHighlightEnabled(true);
        dataset.setColor(Color.RED);


        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextColor(Color.WHITE);
        yAxis.setGranularity(1);
        yAxis.setValueFormatter(new CurrencyFormatter());
        yAxis.setDrawGridLines(true);
        yAxis.setGridColor(Color.BLUE);

        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setTextColor(Color.WHITE);
        yAxisRight.setValueFormatter(new CurrencyFormatter());

        dataset.setLabel("Historical stock price for " + symbol);
        Legend legend = chart.getLegend();
        legend.setTextColor(Color.WHITE);
        Description description = chart.getDescription();
        description.setText("2 years historical data for "+symbol+", stock each point represents one week");
        description.setTextSize(11);
        description.setYOffset(5);
        description.setXOffset(8);
        description.setTextColor(Color.WHITE);

        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(false);

        LineData lineData = new LineData(dataset);
        chart.setData(lineData);
        chart.animateX(2000);
        chart.invalidate();
    }
}
