package br.senai.tcc.nursecare.telas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.modelos.Paciente;
import br.senai.tcc.nursecare.utilidades.Comum;
import br.senai.tcc.nursecare.utilidades.ServicosFirebase;
import br.senai.tcc.nursecare.telas.cadastro.*;

public class CadastroActivity extends AppCompatActivity implements View.OnClickListener, ServicosFirebase.ResultadoListener {

    private String email, senha;
    private Paciente paciente;
    private Bitmap foto;

    private TextView bVoltar, bProximo;
    private ImageView ivIndicador1, ivIndicador2, ivIndicador3, ivIndicador4;
    private ProgressDialog progress;
    private int i = 0;

    private DadosFragment dadosFragment;
    private EnderecoFragment enderecoFragment;
    private FotoFragment fotoFragment;
    private PagamentoFragment pagamentoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        paciente = new Paciente();

        ivIndicador1 = findViewById(R.id.ivIndicador1);
        ivIndicador2 = findViewById(R.id.ivIndicador2);
        ivIndicador3 = findViewById(R.id.ivIndicador3);
        ivIndicador4 = findViewById(R.id.ivIndicador4);
        bVoltar = findViewById(R.id.bAnterior);
        bProximo = findViewById(R.id.bProximo);

        bVoltar.setOnClickListener(this);
        bProximo.setOnClickListener(this);

        dadosFragment = new DadosFragment();
        enderecoFragment = new EnderecoFragment();
        fotoFragment = new FotoFragment();
        pagamentoFragment = new PagamentoFragment();

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
                if (atualizarCadastro() && i < 3) {
                    i++;
                    atualizarTela();
                }
        }
    }

    @Override
    public void onSucesso(Object objeto) {
        progress.dismiss();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onErro(String mensagem) {
        progress.dismiss();
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    private boolean atualizarCadastro() {
        boolean resultado = false;
        switch (i) {
            case 0:
                if (resultado = dadosFragment.validaForm()) {
                    paciente.setNome(dadosFragment.getNome());
                    paciente.setSobrenome(dadosFragment.getSobrenome());
                    email = dadosFragment.getEmail();
                    senha = dadosFragment.getSenha();
                    paciente.setCpf(dadosFragment.getCpf());
                    paciente.setNascimento(dadosFragment.getDataNascimento());
                    paciente.setCelular(dadosFragment.getTelefone());
                }
                break;
            case 1:
                if (resultado = enderecoFragment.validaForm()) {
                    paciente.setCep(enderecoFragment.getCep());
                    paciente.setLogradouro(enderecoFragment.getEndereco());
                    paciente.setNumero(enderecoFragment.getNumero());
                    paciente.setComplemento(enderecoFragment.getComplemento());
                    paciente.setBairro(enderecoFragment.getBairro());
                    paciente.setMunicipio(enderecoFragment.getCidade());
                    paciente.setUf(enderecoFragment.getUf());
                    paciente.setLatitude(enderecoFragment.getLatitude());
                    paciente.setLongitude(enderecoFragment.getLongitude());
                }
                break;
            case 2:
                if (resultado = fotoFragment.validaForm()) {
                    foto = fotoFragment.getBitmap();
                }
                break;
            case 3:
                if (resultado = pagamentoFragment.validaForm()) {
                    paciente.setCartao(pagamentoFragment.getNumeroCartao());
                    paciente.setTitular(pagamentoFragment.getNomeCartao());
                    paciente.setValidade(pagamentoFragment.getDataValidade());
                    paciente.setCvv(pagamentoFragment.getCvv());
                    progress.show();
                    new ServicosFirebase(this).cadastrarPaciente(email, senha, paciente, foto, this);
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
                fragmentTransaction.replace(R.id.fCadastro, dadosFragment).commit();
                break;
            case 1:
                ivIndicador1.setImageResource(R.drawable.indicador_aberto);
                ivIndicador2.setImageResource(R.drawable.indicador_fechado);
                ivIndicador3.setImageResource(R.drawable.indicador_aberto);
                bVoltar.setVisibility(View.VISIBLE);
                fragmentTransaction.replace(R.id.fCadastro, enderecoFragment).commit();
                break;
            case 2:
                ivIndicador2.setImageResource(R.drawable.indicador_aberto);
                ivIndicador3.setImageResource(R.drawable.indicador_fechado);
                ivIndicador4.setImageResource(R.drawable.indicador_aberto);
                bProximo.setText("Próximo");
                bProximo.setBackgroundResource(R.color.colorPrimary);
                bProximo.setPadding(0, 0, getResources().getDimensionPixelSize(R.dimen.dez_dp), 0);
                bProximo.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_forward, 0);
                fragmentTransaction.replace(R.id.fCadastro, fotoFragment).commit();
                break;
            case 3:
                ivIndicador3.setImageResource(R.drawable.indicador_aberto);
                ivIndicador4.setImageResource(R.drawable.indicador_fechado);
                bProximo.setText("Concluir");
                bProximo.setBackgroundColor(0xFF098200);
                bProximo.setPadding(getResources().getDimensionPixelSize(R.dimen.dez_dp), 0, 0, 0);
                bProximo.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check, 0, 0, 0);
                fragmentTransaction.replace(R.id.fCadastro, pagamentoFragment).commit();
        }
    }
}
