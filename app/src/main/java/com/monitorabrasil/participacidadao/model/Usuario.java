package com.monitorabrasil.participacidadao.model;

import com.monitorabrasil.participacidadao.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by 89741803168 on 16/09/2015.
 */
public class Usuario {

    public Usuario(){};

    public static String buscaCidade(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Config");
        query.fromLocalDatastore();
        try {
            ParseObject config = query.getFirst();
            ParseObject cidade = (ParseObject) config.get("cidade");
            cidade.fetchFromLocalDatastore();
            return cidade.getString("municipio")+"-"+cidade.getString("UF");
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String buscaNomeVereadores(){
        if(buscaCidade().equals("Brasília-DF")){
            return "Dep. Distritais";
        }else{
            return "Vereadores";
        }
    }

    public static int buscaImagemTopo(){
        int imagem=0;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Config");
        query.fromLocalDatastore();
        try {
            ParseObject config = query.getFirst();
            ParseObject cidade = (ParseObject) config.get("cidade");
            cidade.fetchFromLocalDatastore();
            switch (cidade.getString("municipio")){
                case "Ouro Branco":
                    imagem =  R.drawable.rsz_ourobranco;
                    break;
                case "Brasília":
                    imagem = R.drawable.bsb;
                    break;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
       return imagem;
    }
}
