package com.monitorabrasil.participacidadao.stores;

import com.monitorabrasil.participacidadao.actions.Action;
import com.monitorabrasil.participacidadao.actions.ProjetoActions;
import com.monitorabrasil.participacidadao.dispatcher.Dispatcher;
import com.parse.ParseObject;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geraldo on 13/08/2015.
 */
public class ProjetoStore extends Store{

    private static ProjetoStore instance;
    private String status;
    private String evento;
    private List<ParseObject> projetos;
    private List<ParseObject> tiposProjeto;

    public List<ParseObject> getTiposProjeto() {
        return tiposProjeto;
    }



    protected ProjetoStore(Dispatcher dispatcher) {
        super(dispatcher);
        projetos = new ArrayList<>();
    }

    public static ProjetoStore get(Dispatcher dispatcher) {
        if (instance == null) {
            instance = new ProjetoStore(dispatcher);
        }
        return instance;
    }

    public List<ParseObject> getProjetos(){
        return projetos;
    }


    @Override
    @Subscribe
    public void onAction(Action action) {
        status = "erro";
        this.evento = action.getType();

        switch (action.getType()) {
            case ProjetoActions.PROJETO_GET_ALL:
                projetos.addAll((List<ParseObject>) action.getData().get(ProjetoActions.KEY_TEXT));
                emitStoreChange();
                break;
            case ProjetoActions.PROJETO_GET_TIPOS:
                tiposProjeto = ((List<ParseObject>) action.getData().get(ProjetoActions.KEY_TEXT));
                emitStoreChange();
                break;
            case ProjetoActions.PROJETO_GET_PROCURA:
                projetos = ((List<ParseObject>) action.getData().get(ProjetoActions.KEY_TEXT));
                emitStoreChange();
                break;

        }
    }

    @Override
    StoreChangeEvent changeEvent() {
        ProjetoStoreChangeEvent mProjetoStoreChangeEvent = new ProjetoStoreChangeEvent();
        mProjetoStoreChangeEvent.status = this.status;
        mProjetoStoreChangeEvent.evento = this.evento;
        return mProjetoStoreChangeEvent;
    }

    public void limpaProjetos() {
        projetos.clear();
    }




    public class ProjetoStoreChangeEvent implements StoreChangeEvent {
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
