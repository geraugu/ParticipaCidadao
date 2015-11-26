package com.monitorabrasil.participacidadao.actions;

import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.application.MyApp;
import com.monitorabrasil.participacidadao.dispatcher.Dispatcher;
import com.monitorabrasil.participacidadao.model.Pergunta;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 89741803168 on 13/08/2015.
 */
public class ActionsCreator {
    private static ActionsCreator instance;
    final Dispatcher dispatcher;

    ActionsCreator(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static ActionsCreator get(Dispatcher dispatcher) {
        if (instance == null) {
            instance = new ActionsCreator(dispatcher);
        }
        return instance;
    }

    /*
    #   ACTIONS DE DIALOGA
    **/

    /**
     * Busca uma lista de perguntas para serem sorteadas
     */
    public void getPerguntaAleatoria() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Questao");
        query.addDescendingOrder("createdAt");
        query.whereEqualTo("cidade", buscaCidade());
        query.include("tema");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_PERGUNTA_ALETORIA,
                            DialogaActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_PERGUNTA_ALETORIA,
                            DialogaActions.KEY_TEXT, "erro"
                    );
                }
            }


        });
    }

    /**
     * Busca a cidade selecionada
     * @return
     */
    public ParseObject buscaCidade(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Config");
        query.fromLocalDatastore();
        try {
            ParseObject config = query.getFirst();
            ParseObject cidade = (ParseObject) config.get("cidade");
            cidade.fetchFromLocalDatastore();
            return cidade;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Salva a cidade selecionada
     * @param cidade
     */
    public void salvaCidade(ParseObject cidade){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Config");
        query.fromLocalDatastore();
        try {
            ParseObject config = query.getFirst();
            config.put("cidade", cidade);
            //config.save();
            config.pin();
        } catch (ParseException e) {
            try {
                ParseObject config = new ParseObject("Config");
                config.put("cidade", cidade);
                cidade.pin();
                config.pin();
            } catch (ParseException e1) {
                e1.printStackTrace();
            }

        }
    }

    /**
     * Envia uma nova resposta
     * @param resposta
     * @param pergunta
     */
    public void enviarResposta(String resposta, final ParseObject pergunta) {
        ParseObject respostaObject = new ParseObject("Resposta");
        respostaObject.put("user",ParseUser.getCurrentUser());
        respostaObject.put("texto", resposta);
        respostaObject.put("questao", pergunta);
        respostaObject.put("qtd_sim", 0);
        respostaObject.put("qtd_nao", 0);
        respostaObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    pergunta.increment("qtd_resposta");
                    pergunta.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                //enviar push
                                ParsePush push = new ParsePush();
                                push.setChannel("p_" + pergunta.getObjectId());
                                JSONObject data = new JSONObject();
                                JSONObject json = new JSONObject();
                                try {
                                    ParseObject tema = (ParseObject) pergunta.get("tema");
                                    data.put("is_background", false);
                                    json.put("idTema", tema.getObjectId());
                                    json.put("pergunta", pergunta.getObjectId());
                                    json.put("cidade", buscaCidade().getObjectId());
                                    json.put("alerta", "Nova resposta para a pergunta: " + pergunta.getString("texto"));
                                    json.put("titulo", MyApp.getInstance().getString(R.string.title_activity_dialoga ));
                                    data.put("data", json);

                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                                push.setData(data);
                                push.sendInBackground(new SendCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.i("participa", e.toString());
                                        }

                                    }
                                });

                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_ENVIAR_RESPOSTA,
                                        DialogaActions.KEY_TEXT, "sucesso"
                                );
                            } else {
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_ENVIAR_RESPOSTA,
                                        DialogaActions.KEY_TEXT, "erro"
                                );
                            }

                        }
                    });

                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_ENVIAR_RESPOSTA,
                            DialogaActions.KEY_TEXT, "erro"
                    );
                }
            }
        });
    }

    /**
     * Busca o resultado das opnioes votadas
     * @param pergunta
     */
    public void getResultado(ParseObject pergunta){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Resposta");
        query.addDescendingOrder("qtd_sim");
        query.whereEqualTo("questao", pergunta);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_RESULTADO,
                            DialogaActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_RESULTADO,
                            DialogaActions.KEY_TEXT, "erro"
                    );
                }
            }


        });
    }

    /**
     * Busca todos os temas
     */
    public void getAllTemas(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Tema");
        query.addAscendingOrder("Nome");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_TEMAS,
                            DialogaActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_TEMAS,
                            DialogaActions.KEY_TEXT, "erro"
                    );
                }
            }


        });
    }

    /**
     * Buasca as perguntas do tema selecionado
     * @param idTema
     */
    public void getPerguntas(String idTema) {
        ParseObject tema = ParseObject.createWithoutData("Tema", idTema);
        tema.fetchFromLocalDatastoreInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    //busca primeiro a pergunta
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Questao");
                    query.addDescendingOrder("createdAt");
                    query.whereEqualTo("tema", object);
                    query.whereEqualTo("cidade", buscaCidade());
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> perguntas, ParseException e) {
                            dispatcher.dispatch(
                                    DialogaActions.DIALOGA_GET_PERGUNTAS,
                                    DialogaActions.KEY_TEXT, perguntas
                            );
                        }
                    });

                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_PERGUNTAS,
                            DialogaActions.KEY_TEXT, "erro"
                    );
                }
            }
        });
    }

    /**
     * Busca pergunta e suas respostas
     * @param idPergunta
     */
    public void getPerguntaRespostas(String idPergunta) {
        ParseObject tema = ParseObject.createWithoutData("Questao", idPergunta);
        tema.fetchInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    getRespostas(object);
                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_PERGUNTA_RESPOSTAS,
                            DialogaActions.KEY_TEXT, "erro"
                    );
                }
            }
        });

    }

    /**
     * Busca as respostas de uma pergunta. Chamada do metodo getPerguntaRespostas
     * @param pergunta
     */
    private void getRespostas(final ParseObject pergunta) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Resposta");
        query.addAscendingOrder("createdAt");
        query.whereEqualTo("questao", pergunta);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    Pergunta perguntaResposta = new Pergunta(pergunta, list);
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_PERGUNTA_RESPOSTAS,
                            DialogaActions.KEY_TEXT, perguntaResposta
                    );
                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_PERGUNTA_RESPOSTAS,
                            DialogaActions.KEY_TEXT, "erro"
                    );
                }
            }


        });
    }

    /**
     * Insere o voto sim para a resposta
     * @param resposta
     * @param voto
     */
    public void concordo(final ParseObject resposta, ParseObject voto) {
        if(null != voto){
            if(voto.getString("sim_nao").equals("n")){
                resposta.increment("qtd_nao",-1);
            }
            voto.put("sim_nao", "s");
            voto.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    resposta.increment("qtd_sim");
                    resposta.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "sucesso"
                                );
                            } else {
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "erro"
                                );
                            }
                        }
                    });
                }
            });
        }else{
            voto = new ParseObject("VotoDialoga");
            voto.put("user",ParseUser.getCurrentUser());
            voto.put("resposta", resposta);
            voto.put("sim_nao", "s");
            voto.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    //atualizar o contador da resposta
                    resposta.increment("qtd_sim");
                    resposta.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "sucesso"
                                );
                            } else {
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "erro"
                                );
                            }
                        }
                    });
                }
            });

        }
        voto.pinInBackground();

    }

    /**
     * Insere o voto nao para a resposta
     * @param resposta
     * @param voto
     */
    public void discordo(final ParseObject resposta, ParseObject voto) {
        if(null != voto){
            if(voto.getString("sim_nao").equals("s")){
                resposta.increment("qtd_sim",-1);
            }
            voto.put("sim_nao", "n");
            voto.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    resposta.increment("qtd_nao");
                    resposta.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "sucesso"
                                );
                            } else {
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "erro"
                                );
                            }
                        }
                    });
                }
            });
        }else {
            voto = new ParseObject("VotoDialoga");
            voto.put("user", ParseUser.getCurrentUser());
            voto.put("resposta", resposta);
            voto.put("sim_nao", "n");
            voto.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    //atualizar o contador da resposta
                    resposta.increment("qtd_nao");
                    resposta.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "sucesso"
                                );

                            } else {
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "erro"
                                );
                            }
                        }
                    });
                }
            });
        }
        voto.pinInBackground();
    }

    /**
     * Busca o voto para a resposta
     * @param respostaAtual
     * @return
     */
    public ParseObject getVoto(ParseObject respostaAtual) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("VotoDialoga");
        query.fromLocalDatastore();
        query.whereEqualTo("resposta", respostaAtual);
        ParseObject voto;
        try {
            voto = query.getFirst();

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return voto;
    }

    /**
     * Envia a pergunta
     * @param pergunta
     * @param tema
     */
    public void enviarPergunta(final String pergunta, String tema) {
        final ParseObject perguntaObject = new ParseObject("Questao");
        perguntaObject.put("user", ParseUser.getCurrentUser());
        perguntaObject.put("texto", pergunta);
        perguntaObject.put("cidade", buscaCidade());
        perguntaObject.put("tema", buscaTema(tema));
        perguntaObject.put("qtd_resposta", 0);
        perguntaObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {

                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_ENVIAR_PERGUNTA,
                            DialogaActions.KEY_TEXT, "sucesso"
                    );
                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_ENVIAR_PERGUNTA,
                            DialogaActions.KEY_TEXT, "erro"
                    );
                }
            }
        });

    }

    /**
     * Busca o objeto tema a partir do nome
     * @param tema
     * @return
     */
    private ParseObject buscaTema(String tema) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Tema");
        try {
            return query.get(tema);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    #   ACTIONS DE COMENTARIO
    **/


    public void getAllComentarios(String tipo, String idObject){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(tipo);
        if(tipo.equals("Comentario")){
            ParseObject projeto = ParseObject.createWithoutData("Projeto", idObject);
            query.whereEqualTo("projeto", projeto);
        }else{
            ParseObject politico = ParseObject.createWithoutData("Politico",idObject);
            query.whereEqualTo("politico", politico);
        }
        query.include("user");

        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    dispatcher.dispatch(
                            ComentarioActions.COMENTARIO_GET_ALL,
                            UserActions.KEY_TEXT, list);
                } else {
                    dispatcher.dispatch(
                            ComentarioActions.COMENTARIO_GET_ALL,
                            UserActions.KEY_TEXT, "erro");
                }
            }
        });
    }

    public void enviarMensagem (String mensagem, String tipo, String idObject){
        ParseUser user = ParseUser.getCurrentUser();
        if(user!= null){
            ParseObject comentario =new ParseObject(tipo);
            ParseObject object;
            if(!tipo.equals("Comentario")){
                //busca politico
                object = ParseObject.createWithoutData("Politico", idObject);

                comentario.put("politico",object);
            }else{
                object = ParseObject.createWithoutData("Projeto", idObject);
                comentario.put("projeto",object);
            }
            comentario.put("mensagem",mensagem);
            comentario.put("user", user);
            comentario.put("nome", user.getString("nome"));

            comentario.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    dispatcher.dispatch(
                            ComentarioActions.COMENTARIO_ENVIAR,
                            UserActions.KEY_TEXT, "sucesso"
                    );
                }
            });

            //incrementa o numero de cometarios
            object.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    object.increment("nr_comentarios");
                    object.saveInBackground();
                }
            });


        }else{
            dispatcher.dispatch(
                    ComentarioActions.COMENTARIO_ENVIAR,
                    UserActions.KEY_TEXT, "erro"
            );
            return;
        }
    }


    /*****
     * ACTIONS DE PROJETOS
     */

    public void buscaProjetoPorPalavra(String chave) {
        chave =  "(?i).*"+chave+".*";
        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Projeto");
        query1.whereMatches("descricao",chave);

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Projeto");
        query2.whereMatches("numero", chave);

        ParseQuery<ParseObject> query3 = ParseQuery.getQuery("Projeto");
        query3.whereMatches("indexacao", chave);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);
        queries.add(query3);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.whereEqualTo("cidade",buscaCidade());
        query.addDescendingOrder("createdAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    dispatcher.dispatch(
                            ProjetoActions.PROJETO_GET_PROCURA,
                            ProjetoActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            ProjetoActions.PROJETO_GET_PROCURA,
                            ProjetoActions.KEY_TEXT, "erro"
                    );
                }
            }


        });
    }

    public void getAllProjetos(String idPolitico, String tipoProjeto, int previousTotal) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Projeto");
        query.addDescendingOrder("createdAt");
        query.setLimit(15);
        query.setSkip(previousTotal);

        if(idPolitico!= null){
            ParseObject politico = ParseObject.createWithoutData("Politico",idPolitico);
            query.whereEqualTo("politico", politico);
        }

        query.whereEqualTo("cidade",buscaCidade());

        if(tipoProjeto != null){
            query.whereEqualTo("classificacao", tipoProjeto);
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName("ListaProjetosFragment")
                    .putContentType("Fragment")
                    .putCustomAttribute("tipoProjeto", tipoProjeto));
        }
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    dispatcher.dispatch(
                            ProjetoActions.PROJETO_GET_ALL,
                            ProjetoActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            ProjetoActions.PROJETO_GET_ALL,
                            ProjetoActions.KEY_TEXT, "erro"
                    );
                }
            }


        });
    }

    /*
    #  ACTIONS DE POLITICOS
    **/

    public void getAllPoliticos(String ordem){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Politico");
        if(ordem.equals("avaliacao")){
            query.addDescendingOrder(ordem);
        }else{
            query.addAscendingOrder(ordem);
        }
        query.whereEqualTo("cidade", buscaCidade());
        query.whereEqualTo("ativo",true);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    dispatcher.dispatch(
                            PoliticoActions.POLITICO_GET_ALL,
                            PoliticoActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            PoliticoActions.POLITICO_GET_ALL,
                            PoliticoActions.KEY_TEXT, "erro"
                    );
                }
            }


        });
    }

    public void getPolitico(String idPolitico) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Politico");
        query.getInBackground(idPolitico, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                dispatcher.dispatch(
                        PoliticoActions.POLITICO_GET_INFOS,
                        PoliticoActions.KEY_TEXT, parseObject
                );
            }
        });

    }

    /*
    #   ACTIONS DE USUARIOS
    **/

    /**
     * Actions do usuario - logar
     * @param inputUsuario
     * @param inputSenha
     */
    public void logar(String inputUsuario, String inputSenha){
        ParseUser.logInInBackground(inputUsuario, inputSenha, new LogInCallback() {

            @Override
            public void done(ParseUser parseUser, com.parse.ParseException e) {
                if (parseUser != null) {
                    dispatcher.dispatch(
                            UserActions.USER_LOGAR,
                            UserActions.KEY_TEXT, "sucesso"
                    );
                } else {
                    dispatcher.dispatch(
                            UserActions.USER_LOGAR,
                            UserActions.KEY_TEXT, "erro"
                    );
                }
            }


        });

    }

    /**
     * Actions do usuario - logout
     */
    public void logout(){
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                dispatcher.dispatch(
                        UserActions.USER_LOGOUT,
                        UserActions.KEY_TEXT, "sucesso"
                );
            }
        });
    }

    /**
     * Actions do usuario - cadastrar
     * @param nome
     * @param password
     * @param email
     * @param mParseFile
     */
    public void cadastrar(final String nome, final String password, final String email, final ParseFile mParseFile) {

        mParseFile.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                // If successful add file to user and signUpInBackground
                if (null == e) {
                    ParseUser user = new ParseUser();
                    user.setUsername(email);
                    user.setPassword(password);
                    user.setEmail(email);
                    user.put("nome", nome);
                    user.put("foto", mParseFile);
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(com.parse.ParseException e) {
                            if (e == null) {
                                dispatcher.dispatch(
                                        UserActions.USER_CADASTRO,
                                        UserActions.KEY_TEXT, "sucesso"
                                );
                            } else {
                                dispatcher.dispatch(
                                        UserActions.USER_CADASTRO,
                                        UserActions.KEY_TEXT, "erro"
                                );
                            }
                        }


                    });

                }

            }
        });





    }

    public void getAvaliacaoPolitico(ParseObject mPolitico) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Avaliacao");
        query.fromLocalDatastore();
        try {
            query.whereEqualTo("politico", mPolitico);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject avaliacao, ParseException e) {
                    if(avaliacao == null){
                        avaliacao = ParseObject.create("Avaliacao");
                    }
                    dispatcher.dispatch(
                            UserActions.USER_GET_AVALIACAO_POLITICO,
                            UserActions.KEY_TEXT, avaliacao
                    );
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void avaliar(ParseObject mPolitico, float rating, ParseObject mAvaliacao) {
        double ultimaAvaliacao=0;
        boolean jaVotou = false;
        //s
        if(mAvaliacao != null) {
            if(mAvaliacao.getObjectId()!= null)
                jaVotou = true;
        }
        if(!jaVotou){
            mAvaliacao = new ParseObject("Avaliacao");
            mAvaliacao.put("politico",mPolitico);
            mAvaliacao.put("user", ParseUser.getCurrentUser());
        }else{
            //guardar a ultima valor da avaliacao anterior
            ultimaAvaliacao = mAvaliacao.getDouble("avaliacao");
        }

        mAvaliacao.put("avaliacao", rating);
        mAvaliacao.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });
        mAvaliacao.pinInBackground();
        try {
            mPolitico.fetchFromLocalDatastore();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int nrAvalicao = mPolitico.getInt("qtd_avaliacao");
        double media =  mPolitico.getDouble("avaliacao");
        double total = nrAvalicao*media;
        if(jaVotou){
            total = total - ultimaAvaliacao;
        }else{
            nrAvalicao++;
            mPolitico.increment("qtd_avaliacao");
        }

        mPolitico.put("avaliacao", (rating + total) / nrAvalicao);

        mPolitico.saveInBackground();
        mPolitico.pinInBackground();
        dispatcher.dispatch(
                UserActions.USER_AVALIA_POLITICO,
                UserActions.KEY_TEXT, "sucesso"
        );
    }


    public void getCidades() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Cidade");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    dispatcher.dispatch(
                            UserActions.USER_GET_CIDADES,
                            UserActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            UserActions.USER_GET_CIDADES,
                            UserActions.KEY_TEXT, "erro"
                    );
                }
            }


        });
    }

    public void getAllTiposProjetos() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("TipoProjeto");
        query.addAscendingOrder("nome");
        query.whereEqualTo("cidade",buscaCidade());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    dispatcher.dispatch(
                            ProjetoActions.PROJETO_GET_TIPOS,
                            ProjetoActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            ProjetoActions.PROJETO_GET_TIPOS,
                            ProjetoActions.KEY_TEXT, "erro"
                    );
                }
            }


        });
    }

    public void getGastos() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("DespesaCamara");
        query.setLimit(200);
        query.whereEqualTo("cidade", buscaCidade());

        query.addAscendingOrder("mes_numero");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> gastos, ParseException e) {

                if (e == null) {
                    dispatcher.dispatch(
                            CamaraActions.CAMARA_GET_DESPESAS,
                            CamaraActions.KEY_TEXT, gastos
                    );
                } else {
                    dispatcher.dispatch(
                            CamaraActions.CAMARA_GET_DESPESAS,
                            CamaraActions.KEY_TEXT, "erro"
                    );
                }

            }
        });
    }


    public void getProjeto(String idProposicao) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Projeto");
        query.getInBackground(idProposicao, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                dispatcher.dispatch(
                        ProjetoActions.GET_PROJETO,
                        ProjetoActions.KEY_TEXT, parseObject
                );
            }
        });
    }
}
