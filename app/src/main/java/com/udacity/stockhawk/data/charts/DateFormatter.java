package com.udacity.stockhawk.data.charts;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Date;

/**
 * Created by george on 26/02/2017.
 */

public class DateFormatter implements IAxisValueFormatter {
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        Date date = new Date((long) value);
        return date.toString();
    }
}
