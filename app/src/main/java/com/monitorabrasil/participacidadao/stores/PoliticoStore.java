package com.monitorabrasil.participacidadao.stores;

import com.monitorabrasil.participacidadao.actions.Action;
import com.monitorabrasil.participacidadao.actions.PoliticoActions;
import com.monitorabrasil.participacidadao.dispatcher.Dispatcher;
import com.parse.ParseObject;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geraldo on 13/08/2015.
 */
public class PoliticoStore extends Store{

    private static PoliticoStore instance;
    private String status;
    private String evento;
    private List<ParseObject> politicos;
    private ParseObject politico;

    protected PoliticoStore(Dispatcher dispatcher) {
        super(dispatcher);
        politicos = new ArrayList<>();
    }

    public static PoliticoStore get(Dispatcher dispatcher) {
        if (instance == null) {
            instance = new PoliticoStore(dispatcher);
        }
        return instance;
    }

    public ParseObject getPolitico(){return politico;}

    public List<ParseObject> getPoliticos(){
        return politicos;
    }


    @Override
    @Subscribe
    public void onAction(Action action) {
        status = "erro";
        this.evento = action.getType();

        switch (action.getType()) {
            case PoliticoActions.POLITICO_GET_ALL:
                politicos = ((List<ParseObject>) action.getData().get(PoliticoActions.KEY_TEXT));
                status = "sucesso";
                emitStoreChange();
                break;
            case PoliticoActions.POLITICO_GET_INFOS:
                politico = ((ParseObject) action.getData().get(PoliticoActions.KEY_TEXT));
                emitStoreChange();
                break;
        }
    }

    @Override
    StoreChangeEvent changeEvent() {
        PoliticoStoreChangeEvent mPoliticoStoreChangeEvent = new PoliticoStoreChangeEvent();
        mPoliticoStoreChangeEvent.status = this.status;
        mPoliticoStoreChangeEvent.evento = this.evento;
        return mPoliticoStoreChangeEvent;
    }




    public class PoliticoStoreChangeEvent implements StoreChangeEvent {
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
