package com.monitorabrasil.participacidadao.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.application.MyApp;

/**
 * An activity representing a single Proposições detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ProposicoesListActivity}.
 */
public class ProposicoesDetailActivity extends AppCompatActivity {

    private String idProjeto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proposicoes_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        idProjeto = getIntent().getStringExtra(ProposicoesDetailFragment.ID_PROPOSICAO);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent =new Intent(MyApp.getInstance().getApplicationContext(), ComentarioActivity.class);
                mIntent.putExtra("projeto",idProjeto);

                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                MyApp.getInstance().startActivity(mIntent);
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            ProposicoesDetailFragment fragment = ProposicoesDetailFragment
                    .newInstance(getIntent().getStringExtra(ProposicoesDetailFragment.ID_PROPOSICAO),
                            getIntent().getStringExtra(ProposicoesDetailFragment.NM_PROPOSICAO));

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.proposições_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, ProposicoesListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
