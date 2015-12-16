package com.monitorabrasil.participacidadao.views;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.model.Grafico;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by geral_000 on 03/12/2015.
 */
public class VereadorCotaFragment extends Fragment {

    public static final String ID_POLITICO = "idPolitico";
    private LineChart mChart;
    private ParseObject politico;
    private String idPolitico;
    float maximo = 0f, minimo = 0f;
    View mView;
    ArrayList<String> xVals = new ArrayList<String>();
    ArrayList<Entry> yVals = new ArrayList<Entry>();
    private ArrayList<Grafico> graficos =new ArrayList<Grafico>();


    public VereadorCotaFragment() {
    }

    public static VereadorCotaFragment newInstance( String idPolitico) {
        VereadorCotaFragment fragment = new VereadorCotaFragment();
        Bundle args = new Bundle();
        args.putString(ID_POLITICO, idPolitico);
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idPolitico = getArguments().getString(ID_POLITICO);
        }

        Activity activity = this.getActivity();
        View view = inflater.inflate(R.layout.fragment_politico_gastos, container, false);

        Bundle bundle = getArguments();
        politico = buscaGastos(idPolitico) ;

        mView=view;

        return view;
    }

    private void buildGraph(){
        mChart = (LineChart)mView.findViewById(R.id.chart1);

        mChart.setDrawGridBackground(false);


        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable value highlighting
        mChart.setHighlightEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // enable/disable highlight indicators (the lines that indicate the
        // highlighted Entry)
        mChart.setHighlightEnabled(false);

        LimitLine ll1 = new LimitLine(130f, "Upper Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.POS_RIGHT);
        ll1.setTextSize(10f);

        LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.POS_RIGHT);
        ll2.setTextSize(10f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        //leftAxis.addLimitLine(ll1);
        //leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaxValue(maximo + 200);
        minimo = minimo-200;
        if(minimo < 0f){
            minimo=0f;
        }
        leftAxis.setAxisMinValue(minimo);
        leftAxis.setStartAtZero(false);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);

        // add data
        setData();

//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, AxisDependency.LEFT);

        mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
//        mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
    }

    private ParseObject buscaGastos(String  id) {

        ParseObject politico = buscaPolitico(id);
        xVals =new ArrayList<String>();
        yVals = new ArrayList<Entry>();
        maximo = 0f;
        minimo = 0f;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Verba");
        query.whereEqualTo("politico",politico);
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> gastos, ParseException e) {
                Iterator<ParseObject> gastoIt = gastos.iterator();
                int i=0;
                while (gastoIt.hasNext()) {
                    ParseObject g = gastoIt.next();
                    float possivelMaximo = Float.valueOf(g.getString("valor"));
                    if(possivelMaximo > maximo ){
                        maximo = possivelMaximo;
                    }
                    if(minimo > possivelMaximo || i==0){
                        minimo=possivelMaximo;
                    }
                    String mes = g.getString("mes");

                    if(mes.length() > 3)
                        xVals.add(g.getString("mes").substring(0,3)+" "+g.getString("ano"));
                    else
                        xVals.add(g.getString("mes")+"/"+g.getString("ano"));
                    yVals.add(new Entry(possivelMaximo, i));
                    i++;
                }
                buildGraph();
            }
        });

        return null;
    }

    private ParseObject buscaPolitico(String  id) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Politico");
        query.fromLocalDatastore();
        try {
            return query.get(id);
        } catch (com.parse.ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setData() {



        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "Verba idenizatoria");
        set1.setFillAlpha(110);
        set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        set1.enableDashedLine(10f, 5f, 0f);
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(1f);
        set1.setCircleSize(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.BLACK);
//        set1.setDrawFilled(true);
        // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
        // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        mChart.setData(data);
    }



}
