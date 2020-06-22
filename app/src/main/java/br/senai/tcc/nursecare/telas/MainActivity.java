package br.senai.tcc.nursecare.telas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.modelos.Paciente;
import br.senai.tcc.nursecare.modelos.Requisicao;
import br.senai.tcc.nursecare.servicos.NotificacaoService;
import br.senai.tcc.nursecare.utilidades.ServicosFirebase;
import br.senai.tcc.nursecare.utilidades.Usuario;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServicosFirebase.ResultadoListener<Requisicao> {

    private ServicosFirebase servicosFirebase;
    private Paciente paciente;
    private Intent serviceIntent;

    private CircularImageView civUsuario;
    private ConstraintLayout clRecado;
    private TextView tvRecado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        servicosFirebase = new ServicosFirebase(this);
        Usuario usuario = Usuario.getInstance();
        paciente = usuario.getPaciente();
        if (paciente == null) {
            servicosFirebase.deslogar();
            return;
        }

        civUsuario = findViewById(R.id.civUsuario);
        clRecado = findViewById(R.id.clRecado);
        tvRecado = findViewById(R.id.tvRecado);

        civUsuario.setOnClickListener(this);
        findViewById(R.id.ivFechar).setOnClickListener(this);
        findViewById(R.id.b1).setOnClickListener(this);
        findViewById(R.id.b2).setOnClickListener(this);
        findViewById(R.id.b3).setOnClickListener(this);
        findViewById(R.id.b4).setOnClickListener(this);

        civUsuario.setImageBitmap(usuario.getFoto());

        servicosFirebase.proximoAtendimento(this);

        serviceIntent = new Intent(getApplicationContext(), NotificacaoService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(serviceIntent);
        else
            startService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        servicosFirebase.proximoAtendimentoRemover();
        //stopService(serviceIntent);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.civUsuario:
                startActivity(new Intent(this, UsuarioActivity.class));
                break;
            case R.id.ivFechar:
                fecharRecado();
                break;
            case R.id.b1:
                startActivity(new Intent(this, AgendamentoActivity.class));
                break;
            case R.id.b2:
                startActivity(new Intent(this, AtendimentoActivity.class));
                break;
            case R.id.b3:
                startActivity(new Intent(this, HistoricoActivity.class));
                break;
            case R.id.b4:
                startActivity(new Intent(this, InformacoesActivity.class));
        }
    }

    @Override
    public void onSucesso(Requisicao requisicao) {
        exibeRecado(requisicao.getDatahora());
    }

    @Override
    public void onErro(String mensagem) {
    }

    private void fecharRecado() {
        final int maxHeight = clRecado.getHeight();
        clRecado.setMaxHeight(maxHeight);
        ObjectAnimator aMaxHeight = ObjectAnimator.ofInt(clRecado, "maxHeight", 0);
        aMaxHeight.setDuration(1000);

        ObjectAnimator aAlpha = ObjectAnimator.ofFloat(clRecado, "alpha", 0f);
        aAlpha.setDuration(800);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(aMaxHeight, aAlpha);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                clRecado.setVisibility(View.GONE);
                clRecado.setMaxHeight(maxHeight);
                clRecado.setAlpha(1f);
            }
        });
        animatorSet.start();
    }

    private void exibeRecado(long datahora) {
        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        tvRecado.setText(String.format("%s, %s.\nSeu próximo atendimento será dia %s às %s.",
                hora < 6 || hora >= 18 ? "Boa noite" : (hora >= 12 ? "Boa tarde" : "Bom dia"),
                paciente.getNome(),
                new SimpleDateFormat("dd/MM").format(datahora),
                new SimpleDateFormat("HH:mm").format(datahora)
        ));
        clRecado.setVisibility(View.VISIBLE);
    }
}
