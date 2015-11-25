package com.monitorabrasil.participacidadao.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.actions.ActionsCreator;
import com.monitorabrasil.participacidadao.dispatcher.Dispatcher;
import com.monitorabrasil.participacidadao.views.fragments.DialogaActivityFragment;
import com.parse.ParseObject;
import com.squareup.otto.Bus;

public class DialogaActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialoga);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Dispatcher dispatcher = Dispatcher.get(new Bus());
        ActionsCreator actionsCreator = ActionsCreator.get(dispatcher);
        ParseObject cidade = actionsCreator.buscaCidade();
        if(cidade == null){
            //abrir a activity para escolher a cidade
            startActivity(new Intent(getApplicationContext(), CidadeActivity.class));

        }else {
            toolbar.setSubtitle(cidade.getString("municipio") + "-" + cidade.getString("UF"));

        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DialogaActivityFragment frag = new DialogaActivityFragment();
        frag.setArguments(getIntent().getExtras());
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment, frag, "dialogaVoto");
        ft.commit();
    }

}
