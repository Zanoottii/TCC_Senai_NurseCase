package br.senai.tcc.nursecare.telas.agendamento;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.modelos.Paciente;
import br.senai.tcc.nursecare.modelos.Requisicao;
import br.senai.tcc.nursecare.utilidades.Comum;
import br.senai.tcc.nursecare.utilidades.Usuario;
import br.senai.tcc.nursecare.telas.AgendamentoActivity;

public class RevisaoFragment extends Fragment {

    TextView tvNome, tvLocal, tvDataHora, tvLabelServico, tvServico, tvPagamento, tvValor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_revisao, container, false);

        AgendamentoActivity activity = (AgendamentoActivity) getActivity();
        Requisicao requisicao = activity.getRequisicao();
        Paciente paciente = Usuario.getInstance().getPaciente();
        List<String> servico = requisicao.getServico();

        tvNome = view.findViewById(R.id.tvNome);
        tvLocal = view.findViewById(R.id.tvLocal);
        tvDataHora = view.findViewById(R.id.tvDataHora);
        tvLabelServico = view.findViewById(R.id.tvLabelServico);
        tvServico = view.findViewById(R.id.tvServico);
        tvPagamento = view.findViewById(R.id.tvPagamento);
        tvValor = view.findViewById(R.id.tvValor);

        tvNome.setText(paciente.getNome() + " " + paciente.getSobrenome());
        tvLocal.setText(requisicao.getEndereco());
        tvDataHora.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(requisicao.getDatahora())));
        if (servico.size() > 1) tvLabelServico.setText("Serviços solicitados");
        tvServico.setText("\u2022 " + TextUtils.join("\n\u2022 ", servico));
        tvPagamento.setText(requisicao.getCartao().isEmpty() ? requisicao.getPagamento() : "Cartão de crédito (Final " + requisicao.getCartao().substring(12) + ")\n" + requisicao.getPagamento());
        tvValor.setText(Comum.formatoReais(requisicao.getValor()));

        return view;
    }

    public boolean validaForm() {
        return true;
    }
}
