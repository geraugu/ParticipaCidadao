package com.monitorabrasil.participacidadao.views;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.actions.ActionsCreator;
import com.monitorabrasil.participacidadao.application.MyApp;
import com.monitorabrasil.participacidadao.dispatcher.Dispatcher;
import com.monitorabrasil.participacidadao.stores.PoliticoStore;
import com.monitorabrasil.participacidadao.views.dialogs.DialogAvaliacao;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * A fragment representing a single Vereador detail screen.
 * This fragment is either contained in a {@link VereadorListActivity}
 * in two-pane mode (on tablets) or a {@link VereadorDetailActivity}
 * on handsets.
 */
public class VereadorDetailFragment extends Fragment {

    private static final String ID_POLITICO = "idPolitico";
    private ParseObject politico;
    private View mView;
    private RatingBar mRatingBar;
    private Button btnAvaliar;
    private String idPolitico;

    private ImageView foto;
    private TextView txtNome;
    private  TextView telefone ;
    private TextView txtPartido ;
    private TextView email ;
    private TextView facebook ;
    private  TextView gastos ;
    private  TextView bens;

    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private PoliticoStore politicoStore;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public VereadorDetailFragment() {
    }

    public static VereadorDetailFragment newInstance( String idPolitico) {
        VereadorDetailFragment fragment = new VereadorDetailFragment();
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
        View rootView = inflater.inflate(R.layout.vereador_detail, container, false);

        initDependencies();
        setupView(rootView);

        actionsCreator.getPolitico(idPolitico);

        return rootView;
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        politicoStore = PoliticoStore.get(dispatcher);
    }


    private void setupView(View view) {

        mView=view;
        mRatingBar = (RatingBar)view.findViewById(R.id.ratingBar);

        Bundle bundle = getArguments();
//
        btnAvaliar = (Button) view.findViewById(R.id.btnAvalie);
        foto = (ImageView)view.findViewById(R.id.foto);
        txtNome = (TextView) view.findViewById(R.id.txtNome);
        telefone = (TextView) view.findViewById(R.id.txtTelefone);
        txtPartido = (TextView) view.findViewById(R.id.txtPartido);
        email = (TextView) view.findViewById(R.id.email);
        facebook = (TextView) view.findViewById(R.id.facebook);
        gastos = (TextView) view.findViewById(R.id.gastos);
        bens = (TextView) view.findViewById(R.id.bens);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            LayerDrawable stars = (LayerDrawable) mRatingBar.getProgressDrawable();
            stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.coloLink), PorterDuff.Mode.SRC_ATOP);
        }
    }


    private void updateUI() {
        politico = politicoStore.getPolitico();

//
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("PoliticoFichaFragment")
                .putContentType("Fragment")
                .putCustomAttribute("vereador",politico.getString("nome")));


        mRatingBar.setRating((float) politico.getDouble("avaliacao"));

        //btnAvaliar
        btnAvaliar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (ParseUser.getCurrentUser() != null) {
                    DialogAvaliacao avaliacao = new DialogAvaliacao(politico, "Avalie");
                    avaliacao.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            //atualizar a avaliacao
                            try {
                                politico.fetchFromLocalDatastore();
                                mRatingBar.setRating((float) politico.getDouble("avaliacao"));
                                Snackbar.make(v, "Avaliação salva.", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    avaliacao.show(getActivity().getFragmentManager(), "dialogAvaliar");
                } else {
                    Snackbar.make(v, "É necessário logar para avaliar.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        });

        //foto

        MyApp.getInstance().getmImagemLoader().displayImage(MyApp.URL_FOTO + politico.get("cpf") + ".jpg", foto);

        //nome

        txtNome.setText(politico.get("nome").toString());

        //telefone
        if(politico.get("telefone") != null)
            telefone.setText(politico.get("telefone").toString());
        else
            telefone.setText("-");
        //telefone

        txtPartido.setText(politico.get("partido").toString());
        //email

        email.setText(politico.get("email").toString());
        //facebook

        if(politico.get("facebook") != null)
            facebook.setText(politico.get("facebook").toString());
        else
            facebook.setText("-");
        //gastos
        if(politico.get("gasto_campanha") != null)
            gastos.setText(politico.get("gasto_campanha").toString().replace(",", "").replace(".", ","));
        else
            gastos.setText("-");

        //bens declarados

        if(politico.get("bens_declarados") != null)
            bens.setText(politico.get("bens_declarados").toString().replace(",", "").replace(".",","));
        else
            bens.setText("-");

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
        dispatcher.register(politicoStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(politicoStore);
    }

}
