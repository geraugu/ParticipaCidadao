package com.monitorabrasil.participacidadao.views;

import android.content.Intent;
import android.os.Bundle;
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
        toolbar.setTitle(getTitle());



        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        if (findViewById(R.id.vereador_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        initDependencies();
        setupView();
        mSwipeRefreshLayout.setRefreshing(true);
        actionsCreator.getAllPoliticos("nome");

        ParseObject cidade = actionsCreator.buscaCidade();
        toolbar.setSubtitle(cidade.getString("municipio") + "-" + cidade.getString("UF"));
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
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(ProposicoesDetailFragment.ARG_ITEM_ID,"1");
            VereadorDetailFragment fragment = new VereadorDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.proposições_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, VereadorDetailActivity.class);
            intent.putExtra(ProposicoesDetailFragment.ARG_ITEM_ID,"5");

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

}
