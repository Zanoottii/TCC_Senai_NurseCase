package br.senai.tcc.nursecare.telas.editar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.SimpleMaskTextWatcher;
import com.google.android.material.textfield.TextInputEditText;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.modelos.Paciente;
import br.senai.tcc.nursecare.utilidades.Usuario;
import br.senai.tcc.nursecare.utilidades.Validacao;

public class DadosFragment extends Fragment {

    private TextInputEditText etNome, etSobrenome, etEmail, etSenha, etConfirmaSenha, etCpf, etDataNascimento, etTelefone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dados, container, false);

        Usuario usuario = Usuario.getInstance();
        Paciente paciente = usuario.getPaciente();

        etNome = view.findViewById(R.id.etNome);
        etSobrenome = view.findViewById(R.id.etSobrenome);
        etEmail = view.findViewById(R.id.etEmail);
        etSenha = view.findViewById(R.id.etSenha);
        etConfirmaSenha = view.findViewById(R.id.etConfirmaSenha);
        etCpf = view.findViewById(R.id.etCpf);
        etDataNascimento = view.findViewById(R.id.etDataNascimento);
        etTelefone = view.findViewById(R.id.etTelefone);

        etCpf.addTextChangedListener(new SimpleMaskTextWatcher(etCpf, new SimpleMaskFormatter("NNN.NNN.NNN-NN")));
        etDataNascimento.addTextChangedListener(new SimpleMaskTextWatcher(etDataNascimento, new SimpleMaskFormatter("NN/NN/NNNN")));
        etTelefone.addTextChangedListener(new SimpleMaskTextWatcher(etTelefone, new SimpleMaskFormatter("(NN) NNNN-NNNNN")));

        etNome.setText(paciente.getNome());
        etSobrenome.setText(paciente.getSobrenome());
        etEmail.setText(usuario.getEmail());
        etCpf.setText(paciente.getCpf());
        etDataNascimento.setText(paciente.getNascimento());
        etTelefone.setText(paciente.getCelular());

        return view;
    }

    public boolean validaForm() {
        return Validacao.nome(getNome(), etNome) &&
                Validacao.sobrenome(getSobrenome(), etSobrenome) &&
                Validacao.email(getEmail(), etEmail) &&
                (TextUtils.isEmpty(getSenha()) && TextUtils.isEmpty(getConfirmaSenha()) ||
                Validacao.senha(getSenha(), getConfirmaSenha(), etSenha, etConfirmaSenha)) &&
                Validacao.cpf(getCpf(), etCpf) &&
                Validacao.dataNascimento(getDataNascimento(), etDataNascimento) &&
                Validacao.telefone(getTelefone(), etTelefone);

    }

    public String getNome() {
        return etNome.getText().toString().trim();
    }

    public String getSobrenome() {
        return etSobrenome.getText().toString().trim();
    }

    public String getEmail() {
        return etEmail.getText().toString().trim();
    }

    public String getSenha() {
        return etSenha.getText().toString().trim();
    }

    private String getConfirmaSenha() {
        return etConfirmaSenha.getText().toString().trim();
    }

    public String getCpf() {
        return etCpf.getText().toString().trim();
    }

    public String getDataNascimento() {
        return etDataNascimento.getText().toString().trim();
    }

    public String getTelefone() {
        return etTelefone.getText().toString().trim();
    }
}
