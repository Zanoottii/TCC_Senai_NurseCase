package br.senai.tcc.nursecare.telas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.modelos.Requisicao;
import br.senai.tcc.nursecare.utilidades.Comum;
import br.senai.tcc.nursecare.utilidades.ServicosFirebase;

public class AtendimentoActivity extends AppCompatActivity implements ServicosFirebase.ResultadoListener<ArrayList<Requisicao>> {

    private ServicosFirebase servicosFirebase;
    private RecyclerView rvAtendimento;
    private TextView tvVazio;
    private ProgressDialog progress;
    private List<Requisicao> requisicoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atendimento);

        tvVazio = findViewById(R.id.tvVazio);

        rvAtendimento = findViewById(R.id.rvAtendimento);
        rvAtendimento.setLayoutManager(new LinearLayoutManager(this));
        rvAtendimento.setItemAnimator(new DefaultItemAnimator());
        rvAtendimento.setHasFixedSize(true);
        rvAtendimento.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));

        progress = Comum.progress(this);
        progress.show();

        servicosFirebase = new ServicosFirebase(this);
        servicosFirebase.listarRequisicao(this);
    }

    @Override
    protected void onDestroy() {
        servicosFirebase.listarRequisicaoRemover();
        super.onDestroy();
    }

    @Override
    public void onSucesso(ArrayList<Requisicao> requisicoes) {
        progress.dismiss();
        this.requisicoes = requisicoes;
        rvAtendimento.setAdapter(new AtendimentoAdapter());
        tvVazio.setVisibility(requisicoes.size() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onErro(String mensagem) {
        progress.dismiss();
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    class AtendimentoAdapter extends RecyclerView.Adapter<AtendimentoAdapter.ViewHolder> {

        private long datahoraAtual;
        private SimpleDateFormat sdfData;
        private SimpleDateFormat sdfHora;

        AtendimentoAdapter() {
            datahoraAtual = Calendar.getInstance().getTimeInMillis();
            sdfData = new SimpleDateFormat("dd/MM");
            sdfHora = new SimpleDateFormat("HH:mm");
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_atendimento, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final Requisicao requisicao = requisicoes.get(position);
            long datahora = requisicao.getDatahora();
            holder.tvDataRequisicao.setText(sdfData.format(datahora));
            holder.tvHoraRequisicao.setText(sdfHora.format(datahora));
            List<String> servico = requisicao.getServico();
            int tamanho = servico.size();
            holder.tvServicoRequisicao.setText(tamanho > 1 ? tamanho + " serviços" : servico.get(0));
            String enfermeiro = requisicao.getEnfermeiro();
            int estado = TextUtils.isEmpty(enfermeiro) ? (datahora < datahoraAtual ? 0 : 1) : ("0".equals(enfermeiro) ? 2 : (datahora < datahoraAtual ? 3 : 4));
            requisicao.setEstado(estado);
            holder.tvEstadoRequisicao.setText(Comum.ESTADO_MSG[estado]);
            holder.ivEstadoRequisicao.setImageResource(Comum.ESTADO_IMG[estado]);
            ConstraintLayout constraintLayout = (ConstraintLayout) holder.ivEstadoRequisicao.getParent();
            constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), DetalhesActivity.class);
                    intent.putExtra("requisicao", requisicao);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return requisicoes.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDataRequisicao, tvHoraRequisicao, tvServicoRequisicao, tvEstadoRequisicao;
            ImageView ivEstadoRequisicao;

            ViewHolder(@NonNull View view) {
                super(view);
                tvDataRequisicao = view.findViewById(R.id.tvDataRequisicao);
                tvHoraRequisicao = view.findViewById(R.id.tvHoraRequisicao);
                tvServicoRequisicao = view.findViewById(R.id.tvServicoRequisicao);
                tvEstadoRequisicao = view.findViewById(R.id.tvEstadoRequisicao);
                ivEstadoRequisicao = view.findViewById(R.id.ivEstadoRequisicao);
            }
        }
    }
}
