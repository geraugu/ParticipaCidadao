package com.monitorabrasil.participacidadao.stores;

import com.monitorabrasil.participacidadao.actions.Action;
import com.monitorabrasil.participacidadao.actions.CamaraActions;
import com.monitorabrasil.participacidadao.dispatcher.Dispatcher;
import com.parse.ParseObject;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geraldo on 13/08/2015.
 */
public class CamaraStore extends Store{

    private static CamaraStore instance;
    private String status;
    private String evento;
    private List<ParseObject> projetos;
    private List<ParseObject> gastos;

    protected CamaraStore(Dispatcher dispatcher) {
        super(dispatcher);
        projetos = new ArrayList<>();
        gastos = new ArrayList<>();
    }

    public static CamaraStore get(Dispatcher dispatcher) {
        if (instance == null) {
            instance = new CamaraStore(dispatcher);
        }
        return instance;
    }

    public List<ParseObject> getProjetos(){
        return projetos;
    }

    public List<ParseObject> getGastos(){return gastos;}

    @Override
    @Subscribe
    public void onAction(Action action) {
        status = "erro";
        this.evento = action.getType();

        switch (action.getType()) {

            case CamaraActions.CAMARA_GET_DESPESAS:
                gastos = ((List<ParseObject>) action.getData().get(CamaraActions.KEY_TEXT));
                emitStoreChange();
                break;

        }
    }

    @Override
    StoreChangeEvent changeEvent() {
        CamaraStoreChangeEvent mCamaraStoreChangeEvent = new CamaraStoreChangeEvent();
        mCamaraStoreChangeEvent.status = this.status;
        mCamaraStoreChangeEvent.evento = this.evento;
        return mCamaraStoreChangeEvent;
    }


    public class CamaraStoreChangeEvent implements StoreChangeEvent {
        private String status;
        private String evento;

        public String getEvento() {
            return evento;
        }

        public String getStatus() {
            return status;
        }
    }
}
