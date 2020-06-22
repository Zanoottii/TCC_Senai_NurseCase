package br.senai.tcc.nursecare.telas.cadastro;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.SimpleMaskTextWatcher;
import com.google.android.material.textfield.TextInputEditText;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.utilidades.Comum;
import br.senai.tcc.nursecare.utilidades.Validacao;

public class PagamentoFragment extends Fragment implements View.OnTouchListener {

    private TextInputEditText etNumeroCartao, etNomeCartao, etDataValidade, etCvv;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pagamento, container, false);

        context = getContext();

        etNumeroCartao = view.findViewById(R.id.etNumeroCartao);
        etNomeCartao = view.findViewById(R.id.etNomeCartao);
        etDataValidade = view.findViewById(R.id.etDataValidade);
        etCvv = view.findViewById(R.id.etCvv);

        etNumeroCartao.addTextChangedListener(new SimpleMaskTextWatcher(etNumeroCartao, new SimpleMaskFormatter("NNNN NNNN NNNN NNNN")));
        etDataValidade.addTextChangedListener(new SimpleMaskTextWatcher(etDataValidade, new SimpleMaskFormatter("NN/NN")));

        etCvv.setOnTouchListener(this);

        return view;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            TextInputEditText textInputEditText = (TextInputEditText) v;
            if(event.getX() >= textInputEditText.getWidth() - textInputEditText.getCompoundPaddingEnd()) {
                Comum.infoCvv(context);
                return true;
            }
        }
        return false;
    }

    public boolean validaForm() {
        if (!TextUtils.isEmpty(getNumeroCartao()) ||
                !TextUtils.isEmpty(getNomeCartao()) ||
                !TextUtils.isEmpty(getDataValidade()) ||
                !TextUtils.isEmpty(getCvv()))
            return Validacao.numeroCartao(getNumeroCartao(), etNumeroCartao) &&
                    Validacao.nomeCartao(getNomeCartao(), etNomeCartao) &&
                    Validacao.dataValidade(getDataValidade(), etDataValidade) &&
                    Validacao.cvv(getCvv(), etCvv);
        return true;
    }

    public String getNumeroCartao() {
        return etNumeroCartao.getText().toString().replace(" ", "");
    }

    public String getNomeCartao() {
        return etNomeCartao.getText().toString().trim();
    }

    public String getDataValidade() {
        return etDataValidade.getText().toString().trim();
    }

    public String getCvv() {
        return etCvv.getText().toString().trim();
    }
}
