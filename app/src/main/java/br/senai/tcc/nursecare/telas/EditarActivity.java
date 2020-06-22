package br.senai.tcc.nursecare.telas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.modelos.Paciente;
import br.senai.tcc.nursecare.utilidades.Comum;
import br.senai.tcc.nursecare.utilidades.ServicosFirebase;
import br.senai.tcc.nursecare.utilidades.Usuario;
import br.senai.tcc.nursecare.telas.editar.*;

public class EditarActivity extends AppCompatActivity implements View.OnClickListener, ServicosFirebase.ResultadoListener<String> {

    private Fragment fragment;
    private Usuario usuario;
    private Paciente paciente;
    private ServicosFirebase servicosFirebase;
    private boolean pacienteOk, emailOk, senhaOk;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);

        usuario = Usuario.getInstance();
        paciente = usuario.getPaciente();

        servicosFirebase = new ServicosFirebase(this);

        pacienteOk = true;
        emailOk = true;
        senhaOk = true;

        String opcao = (String) getIntent().getExtras().getSerializable("fragment");

        if ("dados".equals(opcao)) fragment = new DadosFragment();
        else if ("endereco".equals(opcao)) fragment = new EnderecoFragment();
        else if ("pagamento".equals(opcao)) fragment = new PagamentoFragment();
        else return;

        getSupportFragmentManager().beginTransaction().replace(R.id.fEditar, fragment).commit();
        findViewById(R.id.bSalvar).setOnClickListener(this);
        if (fragment instanceof PagamentoFragment) {
            Button bApagar = findViewById(R.id.bApagar);
            bApagar.setVisibility(TextUtils.isEmpty(paciente.getCartao()) ? View.GONE : View.VISIBLE);
            bApagar.setOnClickListener(this);
        }

        progress = Comum.progress(this);
    }

    @Override
    public void onClick(View v) {
        boolean gravar = false;
        if (fragment instanceof DadosFragment) {
            DadosFragment fragment = (DadosFragment) this.fragment;
            if (fragment.validaForm()) {
                if (!paciente.getNome().equals(fragment.getNome())) {
                    paciente.setNome(fragment.getNome());
                    gravar = true;
                }
                if (!paciente.getSobrenome().equals(fragment.getSobrenome())) {
                    paciente.setSobrenome(fragment.getSobrenome());
                    gravar = true;
                }
                if (!usuario.getEmail().equals(fragment.getEmail())) {
                    emailOk = false;
                    progress.show();
                    servicosFirebase.trocarEmail(fragment.getEmail(), this);
                }
                if (fragment.getSenha().length() > 0) {
                    senhaOk = false;
                    progress.show();
                    servicosFirebase.trocarSenha(fragment.getSenha(), this);
                }
                if (!paciente.getCpf().equals(fragment.getCpf())) {
                    paciente.setCpf(fragment.getCpf());
                    gravar = true;
                }
                if (!paciente.getNascimento().equals(fragment.getDataNascimento())) {
                    paciente.setNascimento(fragment.getDataNascimento());
                    gravar = true;
                }
                if (!paciente.getCelular().equals(fragment.getTelefone())) {
                    paciente.setCelular(fragment.getTelefone());
                    gravar = true;
                }
            }
        } else if (fragment instanceof EnderecoFragment) {
            EnderecoFragment fragment = (EnderecoFragment) this.fragment;
            if (fragment.validaForm()) {
                if (!paciente.getCep().equals(fragment.getCep())) {
                    paciente.setCep(fragment.getCep());
                    gravar = true;
                }
                if (!paciente.getLogradouro().equals(fragment.getEndereco())) {
                    paciente.setLogradouro(fragment.getEndereco());
                    gravar = true;
                }
                if (!paciente.getNumero().equals(fragment.getNumero())) {
                    paciente.setNumero(fragment.getNumero());
                    gravar = true;
                }
                if (!paciente.getComplemento().equals(fragment.getComplemento())) {
                    paciente.setComplemento(fragment.getComplemento());
                    gravar = true;
                }
                if (!paciente.getBairro().equals(fragment.getBairro())) {
                    paciente.setBairro(fragment.getBairro());
                    gravar = true;
                }
                if (!paciente.getMunicipio().equals(fragment.getCidade())) {
                    paciente.setMunicipio(fragment.getCidade());
                    gravar = true;
                }
                if (!paciente.getUf().equals(fragment.getUf())) {
                    paciente.setUf(fragment.getUf());
                    gravar = true;
                }
            }
        } else if (fragment instanceof PagamentoFragment) {
            if (v.getId() == R.id.bSalvar) {
                PagamentoFragment fragment = (PagamentoFragment) this.fragment;
                if (fragment.validaForm()) {
                    if (!paciente.getCartao().equals(fragment.getNumeroCartao())) {
                        paciente.setCartao(fragment.getNumeroCartao());
                        gravar = true;
                    }
                    if (!paciente.getTitular().equals(fragment.getNomeCartao())) {
                        paciente.setTitular(fragment.getNomeCartao());
                        gravar = true;
                    }
                    if (!paciente.getValidade().equals(fragment.getDataValidade())) {
                        paciente.setValidade(fragment.getDataValidade());
                        gravar = true;
                    }
                    if (!paciente.getCvv().equals(fragment.getCvv())) {
                        paciente.setCvv(fragment.getCvv());
                        gravar = true;
                    }
                }
            } else {
                paciente.setCartao("");
                paciente.setTitular("");
                paciente.setValidade("");
                paciente.setCvv("");
                gravar = true;
            }
        }
        if (gravar) {
            pacienteOk = false;
            progress.show();
            servicosFirebase.gravarPaciente(paciente, this);
        }
    }

    @Override
    public void onSucesso(String ok) {
        if ("paciente".equals(ok)) {
            usuario.setPaciente(paciente);
            pacienteOk = true;
        } else if ("email".equals(ok)) {
            usuario.setEmail(((DadosFragment) fragment).getEmail());
            emailOk = true;
        } else if ("senha".equals(ok))
            senhaOk = true;
        if (pacienteOk && emailOk && senhaOk) {
            progress.dismiss();
            Toast.makeText(this, "Alterações salvas!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onErro(String mensagem) {
        progress.dismiss();
        switch (mensagem) {
            case "ERROR_INVALID_EMAIL":
                mensagem = "E-mail inválido.";
                break;
            case "ERROR_EMAIL_ALREADY_IN_USE":
                mensagem = "E-mail já está em uso.";
                break;
            case "ERROR_REQUIRES_RECENT_LOGIN":
                mensagem = "Para alterar e-mail e/ou senha, precisa ter feito o login recentemente.";
        }
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }
}
