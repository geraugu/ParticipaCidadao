package com.monitorabrasil.participacidadao.model.cidade;

/**
 * Created by geral_000 on 07/10/2015.
 */
public class CidadeFactory {



    public static Cidade getCidade(String tipo){
        if (null == tipo)
            return null;
        if(tipo.equals("Brasília")){
            return  new Brasilia();
        }else if(tipo.equals("comum")){
            return new Comum();
        }else if(tipo.equals("sapl")){
            return new Sapl();
        }
        return null;
    }
}
