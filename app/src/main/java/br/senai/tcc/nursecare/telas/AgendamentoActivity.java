package br.senai.tcc.nursecare.telas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.modelos.Requisicao;
import br.senai.tcc.nursecare.utilidades.Comum;
import br.senai.tcc.nursecare.utilidades.ServicosFirebase;
import br.senai.tcc.nursecare.utilidades.Usuario;
import br.senai.tcc.nursecare.telas.agendamento.*;

public class AgendamentoActivity extends AppCompatActivity implements View.OnClickListener, ServicosFirebase.ResultadoListener {

    private Requisicao requisicao;

    private TextView bVoltar, bProximo;
    private ImageView ivIndicador1, ivIndicador2, ivIndicador3, ivIndicador4, ivIndicador5, ivIndicador6;
    private ProgressDialog progress;
    private int i = 0;

    private DataFragment dataFragment;
    private HoraFragment horaFragment;
    private LocalFragment localFragment;
    private ServicosFragment servicosFragment;
    private PagarFragment pagarFragment;
    private RevisaoFragment revisaoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendamento);

        requisicao = new Requisicao();

        ivIndicador1 = findViewById(R.id.ivIndicador1);
        ivIndicador2 = findViewById(R.id.ivIndicador2);
        ivIndicador3 = findViewById(R.id.ivIndicador3);
        ivIndicador4 = findViewById(R.id.ivIndicador4);
        ivIndicador5 = findViewById(R.id.ivIndicador5);
        ivIndicador6 = findViewById(R.id.ivIndicador6);
        bVoltar = findViewById(R.id.bAnterior);
        bProximo = findViewById(R.id.bProximo);

        bVoltar.setOnClickListener(this);
        bProximo.setOnClickListener(this);

        dataFragment = new DataFragment();
        horaFragment = new HoraFragment();
        localFragment = new LocalFragment();
        servicosFragment = new ServicosFragment();
        pagarFragment = new PagarFragment();
        revisaoFragment = new RevisaoFragment();

        progress = Comum.progress(this);

        atualizarTela();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bAnterior:
                i--;
                atualizarTela();
                break;
            case R.id.bProximo:
                if (atualizarAgendamento() && i < 6) {
                    i++;
                    atualizarTela();
                }
        }
    }

    @Override
    public void onSucesso(Object objeto) {
        progress.dismiss();
        finish();
    }

    @Override
    public void onErro(String mensagem) {
        progress.dismiss();
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    public Requisicao getRequisicao() {
        return requisicao;
    }

    private boolean atualizarAgendamento() {
        boolean resultado = false;
        switch (i) {
            case 0:
                resultado = dataFragment.validaForm();
                break;
            case 1:
                if (resultado = horaFragment.validaForm()) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                    long datahora = dataFragment.getData() + horaFragment.getHorario();
                    if (datahora > calendar.getTimeInMillis())
                        requisicao.setDatahora(datahora);
                    else {
                        Toast.makeText(this, "Não é possível o agendamento com este horário. O horário mínimo aceito é em 1 hora.", Toast.LENGTH_SHORT).show();
                        resultado = false;
                    }
                }
                break;
            case 2:
                if (resultado = localFragment.validaForm()) {
                    requisicao.setEndereco(localFragment.getEnderecoCompleto());
                    requisicao.setLatitude(localFragment.getLatitude());
                    requisicao.setLongitude(localFragment.getLongitude());
                }
                break;
            case 3:
                if (resultado = servicosFragment.validaForm()) {
                    requisicao.setServico(servicosFragment.getServicosSelecionados());
                }
                break;
            case 4:
                if (resultado = pagarFragment.validaForm()) {
                    requisicao.setCartao(pagarFragment.getCartao());
                    requisicao.setPagamento(pagarFragment.getPagamento());
                    requisicao.setValor(pagarFragment.getValor());
                }
                break;
            case 5:
                if (resultado = revisaoFragment.validaForm()) {
                    Usuario usuario = Usuario.getInstance();
                    requisicao.setPaciente(usuario.getUid());
                    requisicao.setEnfermeiro("");
                    requisicao.setCooperativa("");
                    new ServicosFirebase(this).agendarRequisicao(requisicao, this);
                }
        }
        return resultado;
    }

    private void atualizarTela() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (i) {
            case 0:
                ivIndicador1.setImageResource(R.drawable.indicador_fechado);
                ivIndicador2.setImageResource(R.drawable.indicador_aberto);
                bVoltar.setVisibility(View.GONE);
                horaFragment.limparFoco();
                fragmentTransaction.replace(R.id.fAgendamento, dataFragment).commit();
                break;
            case 1:
                ivIndicador1.setImageResource(R.drawable.indicador_aberto);
                ivIndicador2.setImageResource(R.drawable.indicador_fechado);
                ivIndicador3.setImageResource(R.drawable.indicador_aberto);
                bVoltar.setVisibility(View.VISIBLE);
                fragmentTransaction.replace(R.id.fAgendamento, horaFragment).commit();
                break;
            case 2:
                ivIndicador2.setImageResource(R.drawable.indicador_aberto);
                ivIndicador3.setImageResource(R.drawable.indicador_fechado);
                ivIndicador4.setImageResource(R.drawable.indicador_aberto);
                fragmentTransaction.replace(R.id.fAgendamento, localFragment).commit();
                break;
            case 3:
                ivIndicador3.setImageResource(R.drawable.indicador_aberto);
                ivIndicador4.setImageResource(R.drawable.indicador_fechado);
                ivIndicador5.setImageResource(R.drawable.indicador_aberto);
                fragmentTransaction.replace(R.id.fAgendamento, servicosFragment).commit();
                break;
            case 4:
                ivIndicador4.setImageResource(R.drawable.indicador_aberto);
                ivIndicador5.setImageResource(R.drawable.indicador_fechado);
                ivIndicador6.setImageResource(R.drawable.indicador_aberto);
                bProximo.setText("Próximo");
                bProximo.setBackgroundResource(R.color.colorPrimary);
                bProximo.setPadding(0, 0, getResources().getDimensionPixelSize(R.dimen.dez_dp), 0);
                bProximo.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_forward, 0);
                fragmentTransaction.replace(R.id.fAgendamento, pagarFragment).commit();
                break;
            case 5:
                ivIndicador5.setImageResource(R.drawable.indicador_aberto);
                ivIndicador6.setImageResource(R.drawable.indicador_fechado);
                bProximo.setText("Agendar");
                bProximo.setBackgroundColor(0xFF098200);
                bProximo.setPadding(getResources().getDimensionPixelSize(R.dimen.dez_dp), 0, 0, 0);
                bProximo.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_event_available, 0, 0, 0);
                fragmentTransaction.replace(R.id.fAgendamento, revisaoFragment).commit();
        }
    }
}
