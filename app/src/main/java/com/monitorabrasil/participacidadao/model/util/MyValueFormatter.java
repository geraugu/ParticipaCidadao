package com.monitorabrasil.participacidadao.model.util;

import com.github.mikephil.charting.utils.ValueFormatter;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by geral_000 on 21/06/2015.
 */
public class MyValueFormatter implements ValueFormatter {

    private DecimalFormat mFormat;

    public MyValueFormatter() {
        mFormat = new DecimalFormat("###,###,###,##0.0");
    }

    @Override
    public String getFormattedValue(float value) {
        return "R$" + NumberFormat.getCurrencyInstance().format(value) ;
    }

}
