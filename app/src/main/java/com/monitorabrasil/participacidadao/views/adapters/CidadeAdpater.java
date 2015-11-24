package com.monitorabrasil.participacidadao.views.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.views.interfaces.RecyclerViewOnClickListenerHack;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geral_000 on 23/07/2015.
 */
public class CidadeAdpater extends RecyclerView.Adapter<CidadeAdpater.ViewHolder> {

    private List<ParseObject> mDataset;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;
    private  FragmentActivity mActivity;


    public CidadeAdpater() {
        mDataset = new ArrayList<>();
    }

    public void setItems(List<ParseObject> temas) {
        this.mDataset = temas;
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_cidade, viewGroup, false);
        // set the view's size, margins, paddings and layout parameter
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        final ParseObject  cidade = mDataset.get(i);
        viewHolder.cidade = cidade;

        viewHolder.txtCidade.setText(cidade.getString("UF")+"-"+cidade.getString("municipio"));
    }

    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r){
        mRecyclerViewOnClickListenerHack = r;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case

        public TextView txtCidade;
        public ParseObject cidade;

        public ViewHolder(View v) {
            super(v);
            txtCidade = (TextView) v.findViewById(R.id.txtNome);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mRecyclerViewOnClickListenerHack != null){
                mRecyclerViewOnClickListenerHack.onClickListener(v, getPosition());
            }
        }
    }
}
