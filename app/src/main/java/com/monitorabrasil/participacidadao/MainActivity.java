package com.monitorabrasil.participacidadao;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.monitorabrasil.participacidadao.actions.ActionsCreator;
import com.monitorabrasil.participacidadao.actions.DialogaActions;
import com.monitorabrasil.participacidadao.actions.PoliticoActions;
import com.monitorabrasil.participacidadao.application.MyApp;
import com.monitorabrasil.participacidadao.dispatcher.Dispatcher;
import com.monitorabrasil.participacidadao.model.Grafico;
import com.monitorabrasil.participacidadao.model.Pergunta;
import com.monitorabrasil.participacidadao.model.Tema;
import com.monitorabrasil.participacidadao.model.Usuario;
import com.monitorabrasil.participacidadao.model.util.MyValueFormatter;
import com.monitorabrasil.participacidadao.stores.CamaraStore;
import com.monitorabrasil.participacidadao.stores.DialogaStore;
import com.monitorabrasil.participacidadao.stores.PoliticoStore;
import com.monitorabrasil.participacidadao.views.CamaraActivity;
import com.monitorabrasil.participacidadao.views.CidadeActivity;
import com.monitorabrasil.participacidadao.views.DialogaActivity;
import com.monitorabrasil.participacidadao.views.LoginActivity;
import com.monitorabrasil.participacidadao.views.ProposicoesListActivity;
import com.monitorabrasil.participacidadao.views.SobreActivity;
import com.monitorabrasil.participacidadao.views.VereadorDetailActivity;
import com.monitorabrasil.participacidadao.views.VereadorDetailFragment;
import com.monitorabrasil.participacidadao.views.VereadorListActivity;
import com.monitorabrasil.participacidadao.views.adapters.PoliticoAdapter;
import com.monitorabrasil.participacidadao.views.interfaces.RecyclerViewOnClickListenerHack;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  , RecyclerViewOnClickListenerHack {

    private final int REQUEST_INVITE = 214;
    private RecyclerView mRecyclerView;
    private PoliticoAdapter mAdapter;
    private List<ParseObject> ob;
    private HashMap<String,Float> valorTotal = new HashMap<String,Float>();
    ArrayList<String> xVals = new ArrayList<String>();
    ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private DialogaStore dialogaStore;
    private PoliticoStore politicoStore;
    private CamaraStore camaraStore;

    private ProgressBar pbDialoga;
    private ProgressBar pbAvaliacao;


    public BarChart mChart;

    private View headerView;
    private Toolbar mToolbar;

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Recomendo o app Participa Cidadão " +
                        "https://play.google.com/store/apps/details?id=com.monitorabrasil.participacidadao";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Participa Cidadão");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Compartilhar via"));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        headerView = getLayoutInflater().inflate(R.layout.nav_header_main, navigationView, false);
        navigationView.addHeaderView(headerView);


        initDependencies();
        verificaPush();
        
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(it);
            }
        });


        
        setupView();
    }



    private void verificaCidade(Toolbar mToolbar) {
        mToolbar.setTitle(getString(R.string.app_name));
        ParseObject cidade = actionsCreator.buscaCidade();
        if(cidade == null){
            //abrir a activity para escolher a cidade
            startActivity(new Intent(getApplicationContext(), CidadeActivity.class));

        }else {
            mToolbar.setSubtitle(cidade.getString("municipio") + "-" + cidade.getString("UF"));

        }
    }

    private void verificaPush() {
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if(extras.getString("idPergunta") != null){
                //abrir a activity
                Intent intent = new Intent(this,DialogaActivity.class);

                intent.putExtra("perguntaId", extras.getString("idPergunta"));
                intent.putExtra("temaId", extras.getString("idTema"));
                startActivity(intent);
            }
        }
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        dialogaStore = DialogaStore.get(dispatcher);
        politicoStore = PoliticoStore.get(dispatcher);
        camaraStore = CamaraStore.get(dispatcher);
    }


    private void setupView() {
        pbAvaliacao = (ProgressBar) findViewById(R.id.pbAvaliacao);
        pbDialoga = (ProgressBar) findViewById(R.id.pbDialoga);

        //RecyclerView dos vereadores
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new PoliticoAdapter(actionsCreator);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerViewOnClickListenerHack(this);

        //buscar o grafico de gastos
        mChart = (BarChart)findViewById(R.id.chart1);

        buscaGastos();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               /* Intent intent = new AppInviteInvitation.IntentBuilder("compartilhe")
                        .setMessage("Conheça o app Participa Cidadão")
                        .setDeepLink(Uri.parse("participaCidadao://id23"))
                      //  .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                        .setCallToActionText("teste")
                        .build();
                startActivityForResult(intent, REQUEST_INVITE);*/

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Recomendo o app Participa Cidadão https://play.google.com/store/apps/details?id=com.monitorabrasil.participacidadao";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Participa Cidadão");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Compartilhar via"));

            }
        });

    }

    private void buscaGastos() {
        valorTotal.clear();
        xVals.clear();
        yVals1.clear();
        Iterator<ParseObject> gastoIt = camaraStore.getGastos().iterator();

        while (gastoIt.hasNext()) {

            ParseObject g = gastoIt.next();
            String mes = g.getString("mes");
            Float novoValor = Float.valueOf(g.getString("valor"));
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
        }
        for (int j = 0; j < xVals.size(); j++) {
            Float val = valorTotal.get(xVals.get(j));
            yVals1.add(new BarEntry(val, j));
        }
        Grafico grafico = new Grafico("Todos", yVals1, xVals, R.color.cor10);
        buildGraph(grafico);
    }

    private void buildGraph(Grafico grafico) {

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


        //setdata
        setData(grafico);

        mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
        mChart.getData().setDrawValues(false);
        mChart.notifyDataSetChanged();

    }

    private void setData(Grafico grafico) {



        BarDataSet set1 = new BarDataSet(grafico.getyAxis(), "Gastos Totais (R$)");
        set1.setBarSpacePercent(35f);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);
        try{
            set1.setColor(MyApp.getInstance().getResources().getColor(grafico.getCor()));

            BarData data = new BarData(grafico.getxVals(), dataSets);
            // data.setValueFormatter(new MyValueFormatter());
            data.setValueTextSize(10f);
//        data.setValueTypeface(mTf);
            mChart.setVisibility(View.VISIBLE);
            mChart.setData(data);



        }catch (Exception e){
            Crashlytics.log("GastosCamaraFragment " + e.toString());
        }

    }


    public void carregaList(){
        pbAvaliacao.setVisibility(View.GONE);
        mAdapter.setItems(politicoStore.getPoliticos());
    }

    /**
     * Monta o card Dialoga
     * @param pergunta
     */
    private void montaCardDialoga(Pergunta pergunta) {
        pbDialoga.setVisibility(View.GONE);
        //monta o topo
        final ParseObject mPergunta = pergunta.getPergunta();
        ParseObject mTema = (ParseObject) mPergunta.get("tema");

        TextView txtTema = (TextView) findViewById(R.id.txtNomeTema);
        txtTema.setText(mTema.getString("Nome"));
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        linearLayout.setBackgroundResource(Tema.buscaCor(mTema.getString("imagem")));
        LinearLayout llCardDialoga = (LinearLayout) findViewById(R.id.llCardDialoga);
        llCardDialoga.setBackgroundResource(Tema.buscaCor(mTema.getString("imagem")));
        ImageView imgIcone = (ImageView)findViewById(R.id.icone);
        imgIcone.setBackgroundResource(Tema.buscaIcone(mTema.getString("imagem")));

        //preenche a pergunta
        TextView txtPergunta = (TextView) findViewById(R.id.txtPergunta);
        txtPergunta.setText(pergunta.getPergunta().getString("texto"));

        //evento do botao para ir para activity dialoga
        Button btnDialoga = (Button) findViewById(R.id.btnDialoga);
        btnDialoga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ir para activity do dialoga e abrir na pergunta selecionada
                Intent intent = new Intent(MainActivity.this,DialogaActivity.class);
                try {
                    mPergunta.pin();
                    ((ParseObject) mPergunta.get("tema")).pin();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                intent.putExtra("perguntaId", mPergunta.getObjectId());
                intent.putExtra("temaId", ((ParseObject) mPergunta.get("tema")).getObjectId());
                startActivity(intent);
            }
        });

    }

    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(DialogaStore.DialogaStoreChangeEvent event) {
        switch (dialogaStore.getEvento()){
            case DialogaActions.DIALOGA_GET_PERGUNTA_ALETORIA:
                montaCardDialoga(dialogaStore.getPergunta());
                break;
        }
    }

    @Subscribe
    public void onTodoStoreChange(CamaraStore.CamaraStoreChangeEvent event) {
        buscaGastos();
    }



    @Subscribe
    public void onTodoStoreChange(PoliticoStore.PoliticoStoreChangeEvent event) {
        switch (event.getEvento()) {
            case PoliticoActions.POLITICO_GET_ALL:
                if(event.getStatus().equals("erro")){

                }else{
                    carregaList();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
        dispatcher.register(dialogaStore);
        dispatcher.register(politicoStore);
        dispatcher.register(camaraStore);

        atualizaTopo();



        //busca vereadores
        pbAvaliacao.setVisibility(View.VISIBLE);
        actionsCreator.getAllPoliticos("avaliacao");

        //busca uma pergunta aleatoria
        pbDialoga.setVisibility(View.VISIBLE);
        actionsCreator.getPerguntaAleatoria();

        //busca gastos
        camaraStore.limpaGastos();
        actionsCreator.getGastos();
    }

    private void atualizaTopo() {
        mToolbar.setTitle(getString(R.string.app_name));
        ParseObject cidade = actionsCreator.buscaCidade();
        if(cidade == null){
            //abrir a activity para escolher a cidade
            startActivity(new Intent(getApplicationContext(), CidadeActivity.class));

        }else {
            mToolbar.setSubtitle(cidade.getString("municipio") + "-" + cidade.getString("UF"));

        }
        //setup headerview
        if(ParseUser.getCurrentUser()!=null)
            setupHeader();

        //atualizaMenu
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_vereadores);
        if(cidade != null){
            if(cidade.getString("municipio").equals("Brasília")){
                item.setTitle("Dep. Distritais");
            }else{
                item.setTitle("Vereadores");
            }
        }



    }

    private void setupHeader() {
        TextView mNome = (TextView)headerView.findViewById(R.id.txtNome);
        ParseUser user = ParseUser.getCurrentUser();
        mNome.setText(user.getString("nome"));
        TextView mEmail = (TextView)headerView.findViewById(R.id.txtEmail);
        mEmail.setText(user.getEmail());

        if(user.getString("image_url") != null){
            ImageView img = (ImageView)headerView.findViewById(R.id.imgPerfil);
            DisplayImageOptions mDisplayImageOptions = new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(100)).cacheInMemory(true).build();
            String url = "https://twitter.com/"+user.getUsername()+"/profile_image?size=normal";
            MyApp.getInstance().getmImagemLoader().displayImage(user.getString("image_url"), img,mDisplayImageOptions);
        }

        ParseFile foto = (ParseFile)user.get("foto");
        if(foto!=null){
            foto.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        bitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, true);
                        ImageView img = (ImageView) headerView.findViewById(R.id.imgPerfil);
                        img.setImageBitmap(bitmap);

                    } else {
                        // something went wrong
                    }
                }
            });
        }

        headerView.setBackground(ContextCompat.getDrawable(this, Usuario.buscaImagemTopo()));
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(dialogaStore);
        dispatcher.unregister(politicoStore);
        dispatcher.unregister(camaraStore);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sobre) {
            startActivity(new Intent(getApplicationContext(), SobreActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_vereadores) {
            startActivity(new Intent(getApplicationContext(), VereadorListActivity.class));
        } else if (id == R.id.nav_dialoga) {
            startActivity(new Intent(getApplicationContext(), DialogaActivity.class));
        } else if (id == R.id.nav_projetos) {
            startActivity(new Intent(getApplicationContext(), ProposicoesListActivity.class));
        } else if (id == R.id.nav_camara) {
            startActivity(new Intent(getApplicationContext(), CamaraActivity.class));

        } else if (id == R.id.nav_trocar_cidade) {
            startActivity(new Intent(getApplicationContext(), CidadeActivity.class));
        } else if (id == R.id.nav_sobre) {
            startActivity(new Intent(getApplicationContext(), SobreActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClickListener(View view, int position) {
        ParseObject politico = politicoStore.getPoliticos().get(position);
        Intent intent = new Intent(this, VereadorDetailActivity.class);
        intent.putExtra(VereadorDetailFragment.ID_POLITICO,politico.getObjectId());
        intent.putExtra(VereadorDetailFragment.ID_IMAGEM, politico.getString("cpf"));
        intent.putExtra(VereadorDetailFragment.NM_POLITICO, politico.getString("nome"));
        startActivity(intent);
    }
}
