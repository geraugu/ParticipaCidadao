package com.monitorabrasil.participacidadao.views.interfaces;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.actions.ActionsCreator;
import com.monitorabrasil.participacidadao.dispatcher.Dispatcher;
import com.monitorabrasil.participacidadao.stores.ProjetoStore;
import com.monitorabrasil.participacidadao.views.ProposicoesDetailActivity;
import com.monitorabrasil.participacidadao.views.ProposicoesDetailFragment;
import com.monitorabrasil.participacidadao.views.VereadorDetailActivity;
import com.monitorabrasil.participacidadao.views.VereadorListActivity;
import com.monitorabrasil.participacidadao.views.adapters.ProjetoAdapter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * A fragment representing a single Vereador detail screen.
 * This fragment is either contained in a {@link VereadorListActivity}
 * in two-pane mode (on tablets) or a {@link VereadorDetailActivity}
 * on handsets.
 */
public class VereadorProjetosFragment extends Fragment implements RecyclerViewOnClickListenerHack {

    public static final String ID_POLITICO = "idPolitico";
    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private ProjetoStore projetoStore;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 2;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private RecyclerView mRecyclerView;
    private ProjetoAdapter mAdapter;
    private String idPolitico;


    public VereadorProjetosFragment() {
    }

    public static VereadorProjetosFragment newInstance( String idPolitico) {
        VereadorProjetosFragment fragment = new VereadorProjetosFragment();
        Bundle args = new Bundle();
        args.putString(ID_POLITICO, idPolitico);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idPolitico = getArguments().getString(ID_POLITICO);
        }

        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
//        if (appBarLayout != null) {
//            appBarLayout.setTitle(mItem.content);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.proposicoes_list, container, false);

        initDependencies();
        setupView(rootView);

        actionsCreator.getAllProjetos(idPolitico, null, previousTotal);
        return rootView;
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        projetoStore = ProjetoStore.get(dispatcher);
    }


    private void setupView(View view) {

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe);

        //tableview
        mRecyclerView = (RecyclerView) view.findViewById(R.id.proposicoes_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        final LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new ProjetoAdapter(actionsCreator);
        mRecyclerView.setAdapter(mAdapter);
//        mAdapter.setRecyclerViewOnClickListenerHack(getContext());

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
                    actionsCreator.getAllProjetos(idPolitico, null, previousTotal);

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

        Intent intent = new Intent(getContext(), ProposicoesDetailActivity.class);
        intent.putExtra(ProposicoesDetailFragment.ID_PROPOSICAO,projetoStore.getProjetos().get(position).getObjectId());
        intent.putExtra(ProposicoesDetailFragment.NM_PROPOSICAO,projetoStore.getProjetos().get(position).getString("numero"));
        startActivity(intent);


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

}
