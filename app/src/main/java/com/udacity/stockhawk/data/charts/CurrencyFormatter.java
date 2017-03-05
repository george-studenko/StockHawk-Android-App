package com.udacity.stockhawk.data.charts;


import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * Created by george on 26/02/2017.
 */

public class CurrencyFormatter implements IAxisValueFormatter {

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        String formatedValue = String.valueOf(value).replace(".0","")+" $";
        return formatedValue;
    }
}
