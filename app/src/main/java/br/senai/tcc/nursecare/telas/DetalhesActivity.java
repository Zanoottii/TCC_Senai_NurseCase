package br.senai.tcc.nursecare.telas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.List;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.modelos.Cooperativa;
import br.senai.tcc.nursecare.modelos.Enfermeiro;
import br.senai.tcc.nursecare.modelos.Requisicao;
import br.senai.tcc.nursecare.utilidades.Comum;
import br.senai.tcc.nursecare.utilidades.ServicosFirebase;

public class DetalhesActivity extends AppCompatActivity implements View.OnClickListener {

    private ServicosFirebase servicosFirebase;
    private Requisicao requisicao;
    private ConstraintLayout clAtendimento;
    private CircularImageView civFotoEnfermeiro;
    private TextView tvNomeEnfermeiro, tvCorenEnfermeiro, tvLabelMaisInfo, tvMaisInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes);

        servicosFirebase = new ServicosFirebase(this);

        ImageView ivEstado = findViewById(R.id.ivEstado);
        TextView tvDataHora = findViewById(R.id.tvDataHora);
        TextView tvEstado = findViewById(R.id.tvEstado);

        TextView tvLabelDtServico = findViewById(R.id.tvLabelDtServico);
        TextView tvDtServico = findViewById(R.id.tvDtServico);

        clAtendimento = findViewById(R.id.clAtendimento);
        civFotoEnfermeiro = findViewById(R.id.civFotoEnfermeiro);
        tvNomeEnfermeiro = findViewById(R.id.tvNomeEnfermeiro);
        tvCorenEnfermeiro = findViewById(R.id.tvCorenEnfermeiro);

        TextView tvTipo = findViewById(R.id.tvTipo);
        tvLabelMaisInfo = findViewById(R.id.tvLabelMaisInfo);
        tvMaisInfo = findViewById(R.id.tvMaisInfo);
        TextView tvLocalAtendimento = findViewById(R.id.tvLocalAtendimento);

        Button bCancelar = findViewById(R.id.bCancelar);
        bCancelar.setOnClickListener(this);

        requisicao = (Requisicao) getIntent().getExtras().getSerializable("requisicao");

        List<String> servico = requisicao.getServico();
        if (servico.size() > 1) tvLabelDtServico.setText("Serviços");
        tvDtServico.setText("\u2022 " + TextUtils.join("\n\u2022 ", servico));

        tvDataHora.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(requisicao.getDatahora()));

        int estado = requisicao.getEstado();

        tvEstado.setText(Comum.ESTADO_MSG[estado]);
        ivEstado.setImageResource(Comum.ESTADO_IMG[estado]);

        if (estado == 1 || estado == 4) bCancelar.setVisibility(View.VISIBLE);
        if (estado == 3 || estado == 4) {
            String enfermeiroId = requisicao.getEnfermeiro();
            servicosFirebase.downloadFoto(enfermeiroId, new ServicosFirebase.ResultadoListener<Bitmap>() {
                @Override
                public void onSucesso(Bitmap bitmap) {
                    civFotoEnfermeiro.setImageBitmap(bitmap);
                }

                @Override
                public void onErro(String mensagem) {
                    Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_SHORT).show();
                }
            });
            servicosFirebase.carregarEnfermeiro(enfermeiroId, new ServicosFirebase.ResultadoListener<Enfermeiro>() {
                @Override
                public void onSucesso(Enfermeiro enfermeiro) {
                    tvNomeEnfermeiro.setText("Enf. " + enfermeiro.getNome() + " " + enfermeiro.getSobrenome());
                    tvCorenEnfermeiro.setText("COREN " + enfermeiro.getCoren());
                    clAtendimento.setVisibility(View.VISIBLE);
                }

                @Override
                public void onErro(String mensagem) {
                    Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_SHORT).show();
                }
            });
        }
        String cooperativaId = requisicao.getCooperativa();
        if (TextUtils.isEmpty(cooperativaId)) {
            tvTipo.setText("Pago");
            tvLabelMaisInfo.setText("Forma de pagamento");
            String pagamento = requisicao.getPagamento();
            if ("Dinheiro".equals(pagamento)) {
                tvMaisInfo.setText(pagamento + " (" + Comum.formatoReais(requisicao.getValor()) + ")");
            } else {
                tvMaisInfo.setText("Cartão de crédito (Final " + requisicao.getCartao().substring(12) + ")\n" +
                        pagamento + "\n" +
                        "Total: " + Comum.formatoReais(requisicao.getValor()));
            }
            tvLabelMaisInfo.setVisibility(View.VISIBLE);
            tvMaisInfo.setVisibility(View.VISIBLE);
        } else {
            tvTipo.setText("Conveniado");
            tvLabelMaisInfo.setText("Cooperativa");
            servicosFirebase.carregarCooperativa(cooperativaId, new ServicosFirebase.ResultadoListener<Cooperativa>() {
                @Override
                public void onSucesso(Cooperativa cooperativa) {
                    tvMaisInfo.setText(cooperativa.getNome() + "\n" + cooperativa.getMunicipio() + " - " + cooperativa.getUf());
                    tvLabelMaisInfo.setVisibility(View.VISIBLE);
                    tvMaisInfo.setVisibility(View.VISIBLE);
                }

                @Override
                public void onErro(String mensagem) {
                    Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_SHORT).show();
                }
            });
        }
        tvLocalAtendimento.setText(requisicao.getEndereco());
    }

    @Override
    public void onClick(View v) {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle("Cancelar")
                .setMessage("Deseja realmente cancelar?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        servicosFirebase.cancelarRequisicao(requisicao.getId(), new ServicosFirebase.ResultadoListener() {
                            @Override
                            public void onSucesso(Object objeto) {
                                Toast.makeText(getApplicationContext(), "Cancelado com sucesso", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onErro(String mensagem) {
                                Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
