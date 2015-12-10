package com.ctg.bean;

import com.github.mikephil.charting.utils.ValueFormatter;

public class NonValueFormatter implements ValueFormatter{

    public NonValueFormatter() {
        
    }

    @Override
    public String getFormattedValue(float value) {
        return "";
    }
}
