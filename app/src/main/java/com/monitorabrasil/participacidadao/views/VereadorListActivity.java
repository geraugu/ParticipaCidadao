package com.monitorabrasil.participacidadao.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.actions.ActionsCreator;
import com.monitorabrasil.participacidadao.actions.PoliticoActions;
import com.monitorabrasil.participacidadao.dispatcher.Dispatcher;
import com.monitorabrasil.participacidadao.stores.PoliticoStore;
import com.monitorabrasil.participacidadao.views.adapters.PoliticoAdapter;
import com.monitorabrasil.participacidadao.views.interfaces.RecyclerViewOnClickListenerHack;
import com.parse.ParseObject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;

/**
 * An activity representing a list of Vereadores. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link VereadorDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class VereadorListActivity extends AppCompatActivity implements RecyclerViewOnClickListenerHack {
    private RecyclerView mRecyclerView;
    private PoliticoAdapter mAdapter;
   // private ProgressBar pb;

    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private PoliticoStore politicoStore;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private FloatingActionButton fab;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vereador_list);
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("PoliticosActivity")
                .putContentType("Activity"));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);





        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }




        initDependencies();
        setupView();
        if (findViewById(R.id.vereador_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

        }
        mSwipeRefreshLayout.setRefreshing(true);
        actionsCreator.getAllPoliticos("nome");

        ParseObject cidade = actionsCreator.buscaCidade();
        toolbar.setSubtitle(cidade.getString("municipio") + "-" + cidade.getString("UF"));



        //atualizaMenu
        if(cidade.getString("municipio").equals("Brasília")){
            setTitle("Dep. Distritais");
        }else{
            setTitle("Vereadores");
        }
    }

    private void setupViewPager(ViewPager viewPager, String idPolitico) {
        Adapter adapter = new Adapter(getSupportFragmentManager());

        //ficha
        VereadorDetailFragment ficha = VereadorDetailFragment.newInstance(idPolitico);
        ficha.setArguments(getIntent().getExtras());
        adapter.addFragment(ficha, "Ficha");

        //gastos - grafico
//        VereadorDetailFragment gastos = new VereadorDetailFragment();
//        gastos.setArguments(getIntent().getExtras());
//        adapter.addFragment(gastos, "Gastos");

        //projetos
        VereadorProjetosFragment listaProjetosFragment = VereadorProjetosFragment.newInstance(idPolitico);
        adapter.addFragment(listaProjetosFragment, "Projetos");

        viewPager.setAdapter(adapter);
        viewPager.getAdapter().notifyDataSetChanged();


        tabLayout.setupWithViewPager(viewPager);
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        politicoStore = PoliticoStore.get(dispatcher);
    }


    private void setupView() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);

        //tableview
        mRecyclerView = (RecyclerView) findViewById(R.id.vereador_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new PoliticoAdapter(actionsCreator);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerViewOnClickListenerHack(this);

        if(mTwoPane){
            viewPager = (ViewPager) findViewById(R.id.viewpager);
            tabLayout = (TabLayout) findViewById(R.id.tabLayout);
            fab = (FloatingActionButton) findViewById(R.id.fab);

        }

    }

    private void updateUI() {
        mAdapter.setItems(politicoStore.getPoliticos());
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(PoliticoStore.PoliticoStoreChangeEvent event) {
        switch (event.getEvento()) {
            case PoliticoActions.POLITICO_GET_ALL:
                if(event.getStatus().equals("erro")){

                }
                break;
        }
        updateUI();
    }

    @Override
    public void onClickListener(View view, int position) {
        final ParseObject politico = politicoStore.getPoliticos().get(position);
        if (mTwoPane) {
//            Bundle arguments = new Bundle();
//            arguments.putString(VereadorDetailFragment.ID_POLITICO,politico.getObjectId());
//            arguments.putString(VereadorDetailFragment.ID_IMAGEM,politico.getString("cpf"));
//            arguments.putString(VereadorDetailFragment.NM_POLITICO,politico.getString("nome"));

            setupViewPager(viewPager, politico.getObjectId());

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent mIntent = new Intent(getApplicationContext(), ComentarioActivity.class);
                    mIntent.putExtra(VereadorDetailFragment.ID_POLITICO, politico.getObjectId());
                    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mIntent);
                }
            });


//            VereadorDetailFragment fragment = new VereadorDetailFragment();
//            fragment.setArguments(arguments);
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.proposições_detail_container, fragment)
//                    .commit();
        } else {
            Intent intent = new Intent(this, VereadorDetailActivity.class);
            intent.putExtra(VereadorDetailFragment.ID_POLITICO,politico.getObjectId());
            intent.putExtra(VereadorDetailFragment.ID_IMAGEM, politico.getString("cpf"));
            intent.putExtra(VereadorDetailFragment.NM_POLITICO, politico.getString("nome"));
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
        dispatcher.register(politicoStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(politicoStore);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class Adapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {

            return mFragments.get(position);
        }


        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

}
