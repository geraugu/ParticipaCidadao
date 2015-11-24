package com.monitorabrasil.participacidadao.views.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.actions.ActionsCreator;
import com.monitorabrasil.participacidadao.views.interfaces.RecyclerViewOnClickListenerHack;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geral_000 on 23/07/2015.
 */
public class TipoProjetoAdapter extends RecyclerView.Adapter<TipoProjetoAdapter.ViewHolder> {

    private static ActionsCreator actionsCreator;
    private List<ParseObject> tiposProjeto;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;
    private  FragmentActivity mActivity;
    int[] cores = new int[]{R.color.cor1, R.color.cor2,R.color.cor3, R.color.cor4,R.color.cor5, R.color.cor6,
            R.color.cor7, R.color.cor8,R.color.cor9, R.color.cor10};


    public TipoProjetoAdapter(ActionsCreator actionsCreator) {
        tiposProjeto = new ArrayList<>();
        TipoProjetoAdapter.actionsCreator = actionsCreator;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case

        public TextView txtTipoProjeto;
        public ParseObject tipoProjeto;

        public ViewHolder(View v) {
            super(v);
            txtTipoProjeto = (TextView) v.findViewById(R.id.txtTipoProjeto);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mRecyclerViewOnClickListenerHack != null){
                mRecyclerViewOnClickListenerHack.onClickListener(v, getPosition());
            }
        }
    }

    public void setItems(List<ParseObject> tiposProjeto) {
        this.tiposProjeto = tiposProjeto;
        notifyDataSetChanged();
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_tipo_projeto, viewGroup, false);
        // set the view's size, margins, paddings and layout parameter
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        final ParseObject  tipoProjeto = tiposProjeto.get(i);
        viewHolder.tipoProjeto = tipoProjeto;

        viewHolder.txtTipoProjeto.setText(tipoProjeto.getString("nome"));
        int posCor=i;
        if(i > 19)
            posCor = i-20;
        else
            if(i > 9)
                posCor = i-10;

        viewHolder.txtTipoProjeto.setBackgroundResource(cores[posCor]);
    }


    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r){
        mRecyclerViewOnClickListenerHack = r;
    }

    @Override
    public int getItemCount() {
        return tiposProjeto.size();
    }

    public String getItem(int i){
        return tiposProjeto.get(i).getString("nome");
    }
}
