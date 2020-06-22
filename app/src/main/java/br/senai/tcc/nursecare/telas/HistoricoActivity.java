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

public class HistoricoActivity extends AppCompatActivity implements ServicosFirebase.ResultadoListener<ArrayList<Requisicao>>, View.OnClickListener {

    private ServicosFirebase servicosFirebase;
    private RecyclerView rvHistorico;
    private TextView tvMes, tvNenhum, tvTotal;
    private ProgressDialog progress;
    private List<Requisicao> requisicoes;
    private Calendar calendar;
    private String[] meses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        servicosFirebase = new ServicosFirebase(this);

        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        tvMes = findViewById(R.id.tvMes);
        tvNenhum = findViewById(R.id.tvNenhum);
        tvTotal = findViewById(R.id.tvTotal);

        rvHistorico = findViewById(R.id.rvHistorico);
        rvHistorico.setLayoutManager(new LinearLayoutManager(this));
        rvHistorico.setItemAnimator(new DefaultItemAnimator());
        rvHistorico.setHasFixedSize(true);
        rvHistorico.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));

        findViewById(R.id.ivMesAnterior).setOnClickListener(this);
        findViewById(R.id.ivMesProximo).setOnClickListener(this);

        meses = getResources().getStringArray(R.array.mes);

        progress = Comum.progress(this);
        buscar();
    }

    @Override
    public void onSucesso(ArrayList<Requisicao> requisicoes) {
        progress.dismiss();
        this.requisicoes = requisicoes;
        rvHistorico.setAdapter(new HistoricoAdapter());
        tvNenhum.setVisibility(requisicoes.size() > 0 ? View.GONE : View.VISIBLE);
        tvTotal.setText("Total no mês: " + Comum.formatoReais(total()));
    }

    @Override
    public void onErro(String mensagem) {
        progress.dismiss();
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivMesAnterior) calendar.add(Calendar.MONTH, -2);
        buscar();
    }

    private void buscar() {
        long dataInicial = calendar.getTimeInMillis();
        long dataFinal = Calendar.getInstance().getTimeInMillis();
        if (dataFinal > dataInicial) {
            tvMes.setText(String.format("%s / %4d", meses[calendar.get(Calendar.MONTH)], calendar.get(Calendar.YEAR)));
            calendar.add(Calendar.MONTH, 1);
            progress.show();
            servicosFirebase.historicoRequisicao(dataInicial, Math.min(calendar.getTimeInMillis(), dataFinal), this);
        }
    }

    private double total() {
        double total = 0;
        for (Requisicao requisicao : requisicoes)
            total += requisicao.getValor();
        return total;
    }

    class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.ViewHolder> {

        private SimpleDateFormat sdfDia;

        HistoricoAdapter() {
            sdfDia = new SimpleDateFormat("dd");
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_historico, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final Requisicao requisicao = requisicoes.get(position);
            requisicao.setEstado(3);
            holder.tvDia.setText(sdfDia.format(requisicao.getDatahora()));
            holder.ivForma.setImageResource("Dinheiro".equals(requisicao.getPagamento()) ? R.drawable.ic_money : R.drawable.ic_credit_card);
            holder.tvValorPago.setText(Comum.formatoReais(requisicao.getValor()));
            ConstraintLayout constraintLayout = (ConstraintLayout) holder.tvValorPago.getParent();
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
            TextView tvDia, tvValorPago;
            ImageView ivForma;

            ViewHolder(@NonNull View view) {
                super(view);
                tvDia = view.findViewById(R.id.tvDia);
                tvValorPago = view.findViewById(R.id.tvValorPago);
                ivForma = view.findViewById(R.id.ivForma);
            }
        }
    }
}
