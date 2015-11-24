package com.monitorabrasil.participacidadao.model;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

/**
 * Created by geral_000 on 27/06/2015.
 */
public class Grafico {
    private String titulo;
    private ArrayList<BarEntry> yAxis;
    private ArrayList<String> xVals;
    private int cor;

    public Grafico(String titulo, ArrayList<BarEntry> yAxis, ArrayList<String> xVals,int cor) {
        this.titulo = titulo;
        this.yAxis = yAxis;
        this.xVals = xVals;
        this.cor = cor;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public ArrayList<BarEntry> getyAxis() {
        return yAxis;
    }

    public void setyAxis(ArrayList<BarEntry> yAxis) {
        this.yAxis = yAxis;
    }

    public ArrayList<String> getxVals() {
        return xVals;
    }

    public void setxVals(ArrayList<String> xVals) {
        this.xVals = xVals;
    }

    public int getCor() {
        return cor;
    }

    public void setCor(int cor) {
        this.cor = cor;
    }
}
