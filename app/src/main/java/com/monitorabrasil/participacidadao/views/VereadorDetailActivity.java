package com.monitorabrasil.participacidadao.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.monitorabrasil.participacidadao.R;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a single Vereador detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link VereadorListActivity}.
 */
public class VereadorDetailActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private String idPolitico;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vereador_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(VereadorDetailActivity.this, ComentarioActivity.class);
                mIntent.putExtra("politico", idPolitico);

                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(mIntent);
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
//        if (savedInstanceState == null) {
//            // Create the detail fragment and add it to the activity
//            // using a fragment transaction.
//            Bundle arguments = new Bundle();
//            arguments.putString(VereadorDetailFragment.ARG_ITEM_ID,
//                    getIntent().getStringExtra(VereadorDetailFragment.ARG_ITEM_ID));
//            VereadorDetailFragment fragment = new VereadorDetailFragment();
//            fragment.setArguments(arguments);
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.vereador_detail_container, fragment)
//                    .commit();
//        }



        Bundle bundle = getIntent().getExtras();
        idPolitico = bundle.getString("idPolitico");
        setTitle(bundle.getString("nome"));



        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());

        //ficha
        VereadorDetailFragment ficha = VereadorDetailFragment.newInstance(idPolitico);
        ficha.setArguments(getIntent().getExtras());
        adapter.addFragment(ficha, "Ficha");

        //gastos - grafico
        VereadorDetailFragment gastos = new VereadorDetailFragment();
        gastos.setArguments(getIntent().getExtras());
        adapter.addFragment(gastos, "Gastos");

        //projetos
        VereadorDetailFragment listaProjetosFragment = new VereadorDetailFragment();
        adapter.addFragment(listaProjetosFragment, "Projetos");

        viewPager.setAdapter(adapter);
        viewPager.getAdapter().notifyDataSetChanged();

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
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
            navigateUpTo(new Intent(this, VereadorListActivity.class));
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
