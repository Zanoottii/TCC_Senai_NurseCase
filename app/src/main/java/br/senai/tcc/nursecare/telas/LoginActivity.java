package br.senai.tcc.nursecare.telas;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.utilidades.Comum;
import br.senai.tcc.nursecare.utilidades.ServicosFirebase;
import br.senai.tcc.nursecare.utilidades.Validacao;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, ServicosFirebase.ResultadoListener {

    private EditText etEmailLogin, etSenhaLogin;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmailLogin = findViewById(R.id.etEmailLogin);
        etSenhaLogin = findViewById(R.id.etSenhaLogin);

        findViewById(R.id.bEntrar).setOnClickListener(this);
        findViewById(R.id.bCadastrar).setOnClickListener(this);

        progress = Comum.progress(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bEntrar:
                if (validaForm()) {
                    progress.show();
                    new ServicosFirebase(this).logar(getEmail(), getSenha(), this);
                }
                break;
            case R.id.bCadastrar:
                startActivity(new Intent(this, CadastroActivity.class));
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
        switch (mensagem) {
            case "ERROR_INVALID_EMAIL":
                etEmailLogin.setError("E-mail inválido");
                etEmailLogin.requestFocus();
                break;
            case "ERROR_USER_NOT_FOUND":
                etEmailLogin.setError("E-mail não cadastrado");
                etEmailLogin.requestFocus();
                break;
            case "ERROR_WRONG_PASSWORD":
                etSenhaLogin.setError("Senha incorreta");
                etSenhaLogin.setText("");
                etSenhaLogin.requestFocus();
        }
    }

    private boolean validaForm() {
        return Validacao.email(getEmail(), etEmailLogin) && Validacao.senha(getSenha(), etSenhaLogin);
    }

    private String getEmail() {
        return etEmailLogin.getText().toString().trim();
    }

    private String getSenha() {
        return etSenhaLogin.getText().toString().trim();
    }

}
