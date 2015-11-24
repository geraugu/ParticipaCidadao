package com.monitorabrasil.participacidadao.views.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

import com.monitorabrasil.participacidadao.R;
import com.monitorabrasil.participacidadao.actions.ActionsCreator;
import com.monitorabrasil.participacidadao.dispatcher.Dispatcher;
import com.monitorabrasil.participacidadao.stores.UserStore;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

@SuppressLint("ValidFragment")
public class DialogAvaliacao extends DialogFragment {

	int idUser;
	int idPolitico;
	private String titulo;
	private ParseObject mPolitico;
	private ParseObject mAvaliacao;
    private RatingBar rb;

	private DialogInterface.OnDismissListener onDismissListener;

	private boolean jaVotou;

	private Dispatcher dispatcher;
	private ActionsCreator actionsCreator;
    private UserStore userStore;

	public DialogAvaliacao(ParseObject politico, String titulo) {
		this.titulo=titulo;
		this.mPolitico = politico;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.dialog_avaliacao, container, false);
		initDependencies();
		setupView(view);

		return view;
	}

	private void initDependencies() {
		dispatcher = Dispatcher.get(new Bus());
		actionsCreator = ActionsCreator.get(dispatcher);
        userStore = UserStore.get(dispatcher);
	}


	private void setupView(View view) {
		rb = (RatingBar) view.findViewById(R.id.ratingBar1);

		jaVotou = false;

		//rb.setRating(new UserDAO(getActivity()).buscaAvaliacaoSalva(idPolitico));

		Button btnCancelar = (Button) view.findViewById(R.id.cancel);
		btnCancelar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getDialog().dismiss();

			}
		});

		Button btnOk = (Button) view.findViewById(R.id.ok);
		btnOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                if (ParseUser.getCurrentUser() != null) {
                    actionsCreator.avaliar(mPolitico, rb.getRating(), userStore.getmAvaliacao());
                }
                getDialog().dismiss();
            }
        });

        //verificar se j� foi feita a avaliacao
        actionsCreator.getAvaliacaoPolitico(mPolitico);

	}
    /**
     * Atualiza a UI depois de uma action
     * @param event
     */

    @Subscribe
    public void onTodoStoreChange(UserStore.UserStoreChangeEvent event) {

        rb.setRating((float)userStore.getmAvaliacao().getDouble("avaliacao"));
    }


    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		Dialog dialog = super.onCreateDialog(savedInstanceState);
		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setTitle(titulo);

		// WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		// lp.copyFrom(dialog.getWindow().getAttributes());
		// lp.width = WindowManager.LayoutParams.MATCH_PARENT-20;
		// dialog.show();
		// dialog.getWindow().setAttributes(lp);

		return dialog;
	}



	public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
		this.onDismissListener = onDismissListener;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (onDismissListener != null) {
			onDismissListener.onDismiss(dialog);
		}
        //dispatcher.register(this);
        //dispatcher.register(userStore);
	}
    @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
        dispatcher.register(userStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(userStore);
    }


}