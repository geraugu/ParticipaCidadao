package com.monitorabrasil.participacidadao.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.application.MyApp;
import com.monitorabrasil.participacidadao.model.Grafico;
import com.monitorabrasil.participacidadao.views.interfaces.RecyclerViewOnClickListenerHack;
import com.monitorabrasil.participacidadao.model.util.MyValueFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geral_000 on 26/06/2015.
 */
public class GastoAdapter extends RecyclerView.Adapter<GastoAdapter.ViewHolder> {

    private List<Grafico> mDataset;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case

        public BarChart chart;
        public TextView titulo;
        public ViewHolder(View v) {
            super(v);

            chart = (BarChart) v.findViewById(R.id.chart1);
            titulo = (TextView) v.findViewById(R.id.titulo);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mRecyclerViewOnClickListenerHack != null){
                mRecyclerViewOnClickListenerHack.onClickListener(v, getPosition());
            }
        }
    }

    public GastoAdapter(List<Grafico> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_grafico, viewGroup, false);
        // set the view's size, margins, paddings and layout parameter
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r){
        mRecyclerViewOnClickListenerHack = r;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        //construir grafico
        buildGraph(viewHolder.chart);
        //setdata
        Grafico grafico = mDataset.get(i);
        setData(grafico,viewHolder.chart);

        viewHolder.chart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
        viewHolder.chart.notifyDataSetChanged();

        viewHolder.titulo.setText(grafico.getTitulo());
        //ParseObject  vereador = mDataset.get(i);
        //vereador.pinInBackground();
        //viewHolder.pb.setText(vereador.get("nome").toString());

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private void buildGraph(BarChart mChart) {

//        mChart.set

        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(false);

        mChart.setDescription("");

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // draw shadows for each bar that show the maximum value
        // mChart.setDrawBarShadow(true);

        // mChart.setDrawXLabels(false);

        mChart.setDrawGridBackground(false);
        // mChart.setDrawYLabels(false);

//        mTf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setTypeface(mTf);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);

        ValueFormatter custom = new MyValueFormatter();

        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setTypeface(mTf);
        leftAxis.setLabelCount(8,true);
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);

        mChart.getAxisRight().setEnabled(false);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
//        rightAxis.setTypeface(mTf);
        rightAxis.setLabelCount(8,true);
        rightAxis.setValueFormatter(custom);
        rightAxis.setSpaceTop(15f);

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        // l.setExtra(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });
        // l.setCustom(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });



        // setting data
//        mSeekBarY.setProgress(50);
//        mSeekBarX.setProgress(12);
//
//        mSeekBarY.setOnSeekBarChangeListener(this);
//        mSeekBarX.setOnSeekBarChangeListener(this);

        // mChart.setDrawLegend(false);

    }

    private void setData(Grafico grafico, BarChart mChart) {



        BarDataSet set1 = new BarDataSet(grafico.getyAxis(), "Gastos Totais(R$)");
        set1.setBarSpacePercent(35f);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);
        try{
            set1.setColor(MyApp.getInstance().getResources().getColor(grafico.getCor()));

            BarData data = new BarData(grafico.getxVals(), dataSets);
            data.setDrawValues(false);
            // data.setValueFormatter(new MyValueFormatter());
            data.setValueTextSize(10f);
//        data.setValueTypeface(mTf);
            mChart.setVisibility(View.VISIBLE);
            mChart.setData(data);



        }catch (Exception e){
            Crashlytics.log("GastosCamaraFragment " + e.toString());
        }

    }


}
