package com.monitorabrasil.participacidadao.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.actions.ActionsCreator;
import com.monitorabrasil.participacidadao.dispatcher.Dispatcher;
import com.monitorabrasil.participacidadao.model.Grafico;
import com.monitorabrasil.participacidadao.stores.CamaraStore;
import com.monitorabrasil.participacidadao.views.adapters.GastoAdapter;
import com.parse.ParseObject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class CamaraActivity extends AppCompatActivity {


    protected BarChart mChart;
    private HashMap<String,HashMap<String, Float>> dataset = new HashMap<String,HashMap<String, Float>>();
    private HashMap<String,Float> valorTotal = new HashMap<String,Float>();
    ArrayList<String> xVals = new ArrayList<String>();
    ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
    private RecyclerView mRecyclerView;
    private GastoAdapter mAdapter;
    List<Grafico> myDataset=new ArrayList<Grafico>();
    List<String> tipos = new ArrayList<String>();
    int[] cores = new int[]{R.color.cor1, R.color.cor2,R.color.cor3, R.color.cor4,R.color.cor5, R.color.cor6,
            R.color.cor7, R.color.cor8,R.color.cor9, R.color.cor10};

    private ProgressBar pb;

    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private CamaraStore camaraStore;

    public CamaraActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initDependencies();
        setupView();
        actionsCreator.getGastos();
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        camaraStore = CamaraStore.get(dispatcher);
    }


    private void setupView() {
        pb = (ProgressBar) findViewById(R.id.progressBar);

        //tableview
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);


    }

    private void buscaGastos() {


        Iterator<ParseObject> gastoIt = camaraStore.getGastos().iterator();

        while (gastoIt.hasNext()) {

            ParseObject g = gastoIt.next();
            String mes = g.getString("mes");
            Float novoValor = Float.valueOf(g.getString("valor"));

            String tipo = g.getString("tipo");
            if(!tipos.contains(tipo)){
                tipos.add(tipo);
            }
            //verifica tipo
            HashMap<String,Float> data = dataset.get(tipo);
            if(data != null){
                //verifica se tem o valor por mes
                Float valorMesTipo = data.get(mes);
                if(valorMesTipo != null){
                    valorMesTipo = valorMesTipo +novoValor;
                    data.remove(mes);
                    data.put(mes, valorMesTipo);
                }else{
                    data.put(mes,novoValor);
                }


            }else{
                data = new HashMap<String, Float>();
                data.put(mes,novoValor);
                dataset.put(tipo,data);
            }


            Float valor = valorTotal.get(mes);
            if (valor != null) {
                valor = valor + novoValor;
                valorTotal.remove(mes);
                valorTotal.put(mes, valor);
            } else {
                valorTotal.put(mes, novoValor);
            }
            if (!xVals.contains(mes)) {
                xVals.add(mes);
            }

            //


        }
        for (int j = 0; j < xVals.size(); j++) {
            Float val = valorTotal.get(xVals.get(j));
            yVals1.add(new BarEntry(val, j));
        }
        Grafico grafico = new Grafico("Todos", yVals1, xVals, R.color.cor10);
        myDataset.add(grafico);

        //adicionar os outros graficos
        for(int i=0; i < tipos.size();i++ ){
            HashMap<String,Float> temp = dataset.get(tipos.get(i));
            ArrayList<String> x = new ArrayList<String>();
            for ( int k=0; k < temp.size(); k++ ) {
                x.add(k,"");
            }
            ArrayList<BarEntry> y = new ArrayList<BarEntry>();
            for ( String mes : temp.keySet() ) {
                if(!x.contains(mes)) {
                    int pos = buscaPosicao(mes);
                    // if(x.size()==(pos-1)){
                    x.remove(pos);
                    x.add(pos, mes);
                    //  }

                }
                Float val = temp.get(mes);
                y.add(new BarEntry(val, x.indexOf(mes)));
            }



            myDataset.add(new Grafico(tipos.get(i),y,x,cores[i]));
        }

        carregaList();
        pb.setVisibility(View.INVISIBLE);

        //   setData();


    }

    @Subscribe
    public void onTodoStoreChange(CamaraStore.CamaraStoreChangeEvent event) {
        buscaGastos();
    }

    @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
        dispatcher.register(camaraStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(camaraStore);
    }

    private int buscaPosicao(String mes) {
        int ret=0;
        switch (mes){
            case "JAN":
                ret=0;
                break;
            case "FEV":
                ret=1;
                break;
            case "MAR":
                ret=2;
                break;
            case "ABR":
                ret=3;
                break;
            case "MAI":
                ret=4;
                break;
            case "JUN":
                ret=5;
                break;
            case "JUL":
                ret=6;
                break;
            case "AGO":
                ret=7;
                break;
            case "SET":
                ret=8;
                break;
            case "OUT":
                ret=9;
                break;
            case "NOV":
                ret=10;
                break;
            case "DEZ":
                ret=11;
                break;

        }
        return ret;
    }

    public void carregaList(){
        mAdapter = new GastoAdapter(myDataset);
//        mAdapter.setRecyclerViewOnClickListenerHack(this);
        mRecyclerView.setAdapter(mAdapter);
//        pb.setVisibility(View.INVISIBLE);
    }


}
