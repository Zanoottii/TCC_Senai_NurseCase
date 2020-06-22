package br.senai.tcc.nursecare.telas.agendamento;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.SimpleMaskTextWatcher;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.modelos.Paciente;
import br.senai.tcc.nursecare.utilidades.Comum;
import br.senai.tcc.nursecare.utilidades.Usuario;
import br.senai.tcc.nursecare.utilidades.Validacao;
import br.senai.tcc.nursecare.telas.AgendamentoActivity;

public class PagarFragment extends Fragment implements RadioGroup.OnCheckedChangeListener, View.OnTouchListener {

    private AgendamentoActivity activity;
    private Paciente paciente;
    private LinearLayout llMeuCartao, llDinheiro, llOutroCartao;
    private Spinner sMeuCartaoParcelas, sOutroCartaoParcelas;
    private TextInputEditText etOutroNumeroCartao, etOutroNomeCartao, etOutroDataValidade, etOutroCvv;
    private String cartao, pagamento;
    private double valor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pagar, container, false);

        activity = (AgendamentoActivity) getActivity();
        paciente = Usuario.getInstance().getPaciente();
        valor = activity.getRequisicao().getServico().size() * 60;

        llMeuCartao = view.findViewById(R.id.llMeuCartao);
        llDinheiro = view.findViewById(R.id.llDinheiro);
        llOutroCartao = view.findViewById(R.id.llOutroCartao);
        sMeuCartaoParcelas = view.findViewById(R.id.sMeuCartaoParcelas);
        sOutroCartaoParcelas = view.findViewById(R.id.sOutroCartaoParcelas);
        etOutroNumeroCartao = view.findViewById(R.id.etOutroNumeroCartao);
        etOutroNomeCartao = view.findViewById(R.id.etOutroNomeCartao);
        etOutroDataValidade = view.findViewById(R.id.etOutroDataValidade);
        etOutroCvv = view.findViewById(R.id.etOutroCvv);

        etOutroNumeroCartao.addTextChangedListener(new SimpleMaskTextWatcher(etOutroNumeroCartao, new SimpleMaskFormatter("NNNN NNNN NNNN NNNN")));
        etOutroDataValidade.addTextChangedListener(new SimpleMaskTextWatcher(etOutroDataValidade, new SimpleMaskFormatter("NN/NN")));

        etOutroCvv.setOnTouchListener(this);

        TextView tvMeuCartao = view.findViewById(R.id.tvMeuCartao);
        TextView tvDinheiro = view.findViewById(R.id.tvDinheiro);
        RadioGroup rgPagar = view.findViewById(R.id.rgPagar);
        RadioButton rbMeuCartao = view.findViewById(R.id.rbMeuCartao);
        RadioButton rbOutroCartao = view.findViewById(R.id.rbOutroCartao);
        rgPagar.setOnCheckedChangeListener(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.spinner_item_black, processaParcelas(valor));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sMeuCartaoParcelas.setAdapter(adapter);
        sOutroCartaoParcelas.setAdapter(adapter);

        tvDinheiro.setText("Valor: " + Comum.formatoReais(valor));

        String cartaoCadastrado = (paciente != null) ? paciente.getCartao() : null;
        if (TextUtils.isEmpty(cartaoCadastrado)) {
            rgPagar.check(R.id.rbDinheiro);
            rbMeuCartao.setVisibility(View.GONE);
            rbOutroCartao.setText("Cartão de crédito");
        } else
            tvMeuCartao.setText("Cartão: **** **** **** " + cartaoCadastrado.substring(12));

        return view;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rbMeuCartao:
                llMeuCartao.setVisibility(View.VISIBLE);
                llDinheiro.setVisibility(View.GONE);
                llOutroCartao.setVisibility(View.GONE);
                break;
            case R.id.rbDinheiro:
                llMeuCartao.setVisibility(View.GONE);
                llDinheiro.setVisibility(View.VISIBLE);
                llOutroCartao.setVisibility(View.GONE);
                break;
            case R.id.rbOutroCartao:
                llMeuCartao.setVisibility(View.GONE);
                llDinheiro.setVisibility(View.GONE);
                llOutroCartao.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            TextInputEditText textInputEditText = (TextInputEditText) v;
            if (event.getX() >= textInputEditText.getWidth() - textInputEditText.getCompoundPaddingEnd()) {
                Comum.infoCvv(activity);
                return true;
            }
        }
        return false;
    }

    public boolean validaForm() {
        if (llMeuCartao.getVisibility() == View.VISIBLE) {
            cartao = paciente.getCartao();
            pagamento = sMeuCartaoParcelas.getSelectedItem().toString();
            valor = calculaValor(sMeuCartaoParcelas.getSelectedItemPosition() + 1);
            return true;
        } else if (llDinheiro.getVisibility() == View.VISIBLE) {
            cartao = "";
            pagamento = "Dinheiro";
            return true;
        } else if (llOutroCartao.getVisibility() == View.VISIBLE) {
            if (!Validacao.numeroCartao(getNumeroCartao(), etOutroNumeroCartao) ||
                    !Validacao.nomeCartao(getNomeCartao(), etOutroNomeCartao) ||
                    !Validacao.dataValidade(getDataValidade(), etOutroDataValidade) ||
                    !Validacao.cvv(getCvv(), etOutroCvv))
                return false;
            cartao = getNumeroCartao();
            pagamento = sOutroCartaoParcelas.getSelectedItem().toString();
            valor = calculaValor(sOutroCartaoParcelas.getSelectedItemPosition() + 1);
            return true;
        }
        return false;
    }

    public String getCartao() {
        return cartao;
    }

    public String getPagamento() {
        return pagamento;
    }

    public double getValor() {
        return valor;
    }

    private String getNumeroCartao() {
        return etOutroNumeroCartao.getText().toString().replace(" ", "");
    }

    private String getNomeCartao() {
        return etOutroNomeCartao.getText().toString().trim();
    }

    private String getDataValidade() {
        return etOutroDataValidade.getText().toString().trim();
    }

    private String getCvv() {
        return etOutroCvv.getText().toString().trim();
    }

    private ArrayList<String> processaParcelas(double valor) {
        int quantidade = Math.min((int) valor / 20, 12);
        ArrayList<String> parcelas = new ArrayList<>();
        parcelas.add("À vista por " + Comum.formatoReais(calculaValor(1)) + " (-5%)");
        for (int i = 2; i <= quantidade; i++)
            parcelas.add(String.format("Em %dX de %s %s juros", i, Comum.formatoReais(calculaValor(i) / i), (i > 6) ? "com" : "sem"));
        return parcelas;
    }

    private double calculaValor(int parcelas) {
        if (parcelas == 1)
            return valor * .95;
        else if (parcelas > 6)
            return valor * (1 + parcelas * .01);
        return valor;
    }
}
