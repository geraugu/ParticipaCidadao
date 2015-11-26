package com.monitorabrasil.participacidadao.views;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.actions.ActionsCreator;
import com.monitorabrasil.participacidadao.dispatcher.Dispatcher;
import com.monitorabrasil.participacidadao.stores.PoliticoStore;
import com.monitorabrasil.participacidadao.stores.ProjetoStore;
import com.parse.ParseObject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * A fragment representing a single Proposições detail screen.
 * This fragment is either contained in a {@link ProposicoesListActivity}
 * in two-pane mode (on tablets) or a {@link ProposicoesDetailActivity}
 * on handsets.
 */
public class ProposicoesDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ID_PROPOSICAO = "id_proposicao";
    public static final String NM_PROPOSICAO = "nm_proposicao";
    private String idProposicao ;
    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private ProjetoStore projetoStore;
    private ParseObject projeto;

    private TextView txtTipoProjeto;
    private TextView txtEmenta;
    private TextView txtAutor;
    private TextView txtLocal;
    private TextView txtSituacao;
    private TextView txtNormaGerada;
    private TextView txtPublicacoes;
    private TextView txtLink;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProposicoesDetailFragment() {
    }

    public static ProposicoesDetailFragment newInstance( String idProp,String nomeProp) {
        ProposicoesDetailFragment fragment = new ProposicoesDetailFragment();
        Bundle args = new Bundle();
        args.putString(ID_PROPOSICAO, idProp);
        args.putString(NM_PROPOSICAO, nomeProp);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ID_PROPOSICAO)) {
            idProposicao = getArguments().getString(ID_PROPOSICAO);
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(getArguments().getString(NM_PROPOSICAO));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.proposicoes_detail, container, false);

        initDependencies();
        setupView(rootView);

        actionsCreator.getProjeto(idProposicao);

        return rootView;
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        projetoStore = ProjetoStore.get(dispatcher);
    }


    private void setupView(View view) {

        txtTipoProjeto = (TextView)view.findViewById(R.id.txtTipoProjeto);
        txtEmenta= (TextView)view.findViewById(R.id.txtEmenta);
        txtAutor= (TextView)view.findViewById(R.id.txtAutor);
        txtLocal= (TextView)view.findViewById(R.id.txtLocal);
        txtSituacao= (TextView)view.findViewById(R.id.txtSituacao);
        txtNormaGerada= (TextView)view.findViewById(R.id.txtNormaGerada);
        txtPublicacoes= (TextView)view.findViewById(R.id.txtPublicacoes);
        txtLink= (TextView)view.findViewById(R.id.txtLink);
    }


    private void updateUI() {
        projeto = projetoStore.getProjeto();
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(projeto.getString("numero"))
                .putContentType("Projeto"));

        txtTipoProjeto.setText(projeto.getString("tipo"));
        txtEmenta.setText(projeto.getString("descricao"));
        ParseObject autor = projeto.getParseObject("politico");
        txtAutor.setText(autor.getString("nome"));
        if(!projeto.getString("localizacao").isEmpty()){
            txtLocal.setText("Localização:"+ projeto.getString("localizacao"));
            txtLocal.setVisibility(View.VISIBLE);
        }
        if(!projeto.getString("situacao").isEmpty()){
            txtSituacao.setText("Situação:"+ projeto.getString("situacao"));
            txtSituacao.setVisibility(View.VISIBLE);
        }
        if(!projeto.getString("norma_gerada").isEmpty()){
            txtNormaGerada.setText("Norma gerada:"+ projeto.getString("norma_gerada"));
            txtNormaGerada.setVisibility(View.VISIBLE);
        }
        if(!projeto.getString("publicacoes").isEmpty()){
            txtPublicacoes.setText("Publicações:"+ projeto.getString("publicacoes"));
            txtPublicacoes.setVisibility(View.VISIBLE);
        }
        if(!projeto.getString("link").isEmpty()){
            txtLink.setText("http://legislacao.cl.df.gov.br"
                    +projeto.getString("link"));
            txtLink.setVisibility(View.VISIBLE);
        }



    }

    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(PoliticoStore.PoliticoStoreChangeEvent event) {
        updateUI();
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
