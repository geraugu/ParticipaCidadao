package com.monitorabrasil.participacidadao.views;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.actions.ActionsCreator;
import com.monitorabrasil.participacidadao.dispatcher.Dispatcher;
import com.monitorabrasil.participacidadao.stores.ProjetoStore;
import com.monitorabrasil.participacidadao.views.adapters.ProjetoAdapter;
import com.monitorabrasil.participacidadao.views.interfaces.RecyclerViewOnClickListenerHack;
import com.parse.ParseObject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;

/**
 * An activity representing a list of Proposição. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ProposicoesDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ProposicoesListActivity extends AppCompatActivity implements RecyclerViewOnClickListenerHack {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private ProjetoStore projetoStore;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 2;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private static final String TIPO_PROJETO = "tipoProjeto";
    private static final String ID_POLITICO = "idPolitico";
    private String tipoProjeto;
    private String idPolitico;

    private RecyclerView mRecyclerView;
    private ProjetoAdapter mAdapter;

    private boolean realizouBusca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proposicoes_list);
        realizouBusca = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());



        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }



        if (findViewById(R.id.proposições_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        if (getIntent().getExtras() != null) {
            tipoProjeto = getIntent().getStringExtra(TIPO_PROJETO);
            idPolitico = getIntent().getStringExtra(ID_POLITICO);
        }

        initDependencies();
        setupView();
        projetoStore.limpaProjetos();
        actionsCreator.getAllProjetos(idPolitico, tipoProjeto, previousTotal);
        mSwipeRefreshLayout.setRefreshing(true);

        ParseObject cidade = actionsCreator.buscaCidade();
        toolbar.setSubtitle(cidade.getString("municipio") + "-" + cidade.getString("UF"));
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        projetoStore = ProjetoStore.get(dispatcher);
    }


    private void setupView() {


        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);

        //tableview
        mRecyclerView = (RecyclerView) findViewById(R.id.proposicoes_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        final LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new ProjetoAdapter(actionsCreator);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerViewOnClickListenerHack(this);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mRecyclerView.getChildCount();
                totalItemCount = llm.getItemCount();
                firstVisibleItem = llm.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    //carregar mais projetos
                    actionsCreator.getAllProjetos(idPolitico, tipoProjeto, previousTotal);

                    loading = true;
                }
            }
        });

    }

    private void updateUI() {
        mAdapter.setItems(projetoStore.getProjetos());
        mSwipeRefreshLayout.setRefreshing(false);
    }


    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(ProjetoStore.ProjetoStoreChangeEvent event) {
        updateUI();
    }

    @Override
    public void onClickListener(View view, int position) {
        ParseObject projeto = projetoStore.getProjetos().get(position);
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(ProposicoesDetailFragment.ID_PROPOSICAO,projeto.getObjectId());
            arguments.putString(ProposicoesDetailFragment.NM_PROPOSICAO, projeto.getString("numero"));
            ProposicoesDetailFragment fragment = ProposicoesDetailFragment
                    .newInstance(projeto.getObjectId(),projeto.getString("numero"));
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.proposições_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, ProposicoesDetailActivity.class);
            intent.putExtra(ProposicoesDetailFragment.ID_PROPOSICAO, projeto.getObjectId());
            intent.putExtra(ProposicoesDetailFragment.NM_PROPOSICAO, projeto.getString("numero"));

            startActivity(intent);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
        dispatcher.register(projetoStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(projetoStore);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_projetos, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!query.isEmpty()) {

                        mSwipeRefreshLayout.setRefreshing(true);
                        //realizar a busca
                        actionsCreator.buscaProjetoPorPalavra(query);
                        realizouBusca = true;
                    }

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if(realizouBusca && newText.isEmpty()){
                        projetoStore.limpaProjetos();
                        actionsCreator.getAllProjetos(idPolitico, tipoProjeto, 0);
                        realizouBusca = false;
                    }
                    return false;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);

    }
}
