package com.monitorabrasil.participacidadao.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.actions.ActionsCreator;
import com.monitorabrasil.participacidadao.actions.UserActions;
import com.monitorabrasil.participacidadao.dispatcher.Dispatcher;
import com.monitorabrasil.participacidadao.stores.UserStore;
import com.monitorabrasil.participacidadao.views.adapters.CidadeAdpater;
import com.monitorabrasil.participacidadao.views.interfaces.RecyclerViewOnClickListenerHack;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class CidadeActivity extends AppCompatActivity implements RecyclerViewOnClickListenerHack {

    private RecyclerView mRecyclerView;
    private CidadeAdpater mAdapter;
    private ProgressBar pb;

    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private UserStore userStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cidade);
        initDependencies();
        setupView();
        actionsCreator.getCidades();
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        userStore = UserStore.get(dispatcher);
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
        mAdapter = new CidadeAdpater();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerViewOnClickListenerHack(this);

    }

    private void updateUI() {
        mAdapter.setItems(userStore.getCidades());
        pb.setVisibility(View.INVISIBLE);
    }

    /**
     * Atualiza a UI depois de uma action
     *
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(UserStore.UserStoreChangeEvent event) {
        String evento = userStore.getEvento();
        switch (evento) {
            case UserActions.USER_GET_CIDADES:
                if (evento.equals("erro")) {

                }
                break;
        }
        updateUI();
    }

    @Override
    public void onClickListener(View view, int position) {
        ParseObject cidade = userStore.getCidades().get(position);
        actionsCreator.salvaCidade(cidade);
        if (ParseUser.getCurrentUser() == null)
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
        dispatcher.register(userStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(userStore);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_cidade, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}