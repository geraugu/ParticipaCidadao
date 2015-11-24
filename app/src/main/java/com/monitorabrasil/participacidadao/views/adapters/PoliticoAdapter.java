package com.monitorabrasil.participacidadao.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.actions.ActionsCreator;
import com.monitorabrasil.participacidadao.application.MyApp;
import com.monitorabrasil.participacidadao.views.interfaces.RecyclerViewOnClickListenerHack;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geraugu on 6/7/15.
 */
public class PoliticoAdapter extends RecyclerView.Adapter<PoliticoAdapter.ViewHolder> {

    private static ActionsCreator actionsCreator;
    private List<ParseObject> politicos;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

    public PoliticoAdapter(ActionsCreator actionsCreator) {
        politicos = new ArrayList<>();
        PoliticoAdapter.actionsCreator = actionsCreator;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_politico, viewGroup, false);
        // set the view's size, margins, paddings and layout parameter
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r){
        mRecyclerViewOnClickListenerHack = r;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        ParseObject  vereador = politicos.get(i);
        vereador.pinInBackground();
        viewHolder.mTextView.setText(vereador.get("nome").toString());
        viewHolder.txtPartido.setText(vereador.get("partido").toString());
        viewHolder.rb.setRating((float)vereador.getDouble("avaliacao"));
        MyApp.getInstance().getmImagemLoader().displayImage(MyApp.URL_FOTO + vereador.get("cpf") + ".jpg", viewHolder.foto);

    }

    @Override
    public int getItemCount() {
        return politicos.size();
    }

    public void setItems(List<ParseObject> politicos) {
        this.politicos = politicos;
        notifyDataSetChanged();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public TextView mTextView;
        public TextView txtPartido;
        public ImageView foto;
        public RatingBar rb;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.txtNome);
            txtPartido = (TextView) v.findViewById(R.id.txtPartido);
            foto  = (ImageView)v.findViewById(R.id.foto);
            rb = (RatingBar)v.findViewById(R.id.ratingBar);

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
