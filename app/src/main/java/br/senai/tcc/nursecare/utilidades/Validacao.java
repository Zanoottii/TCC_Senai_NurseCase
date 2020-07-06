package br.senai.tcc.nursecare.utilidades;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public abstract class Validacao {

    public static boolean isCPF(String cpf) {
        if (cpf.equals("000.000.000-00") || cpf.equals("111.111.111-11") ||
                cpf.equals("222.222.222-22") || cpf.equals("333.333.333-33") ||
                cpf.equals("444.444.444-44") || cpf.equals("555.555.555-55") ||
                cpf.equals("666.666.666-66") || cpf.equals("777.777.777-77") ||
                cpf.equals("888.888.888-88") || cpf.equals("999.999.999-99") ||
                cpf.length() != 14)
            return false;

        int d1 = 0;
        int d2 = 0;
        String numerosCpf = cpf.replaceAll("[.-]", "");

        for (int i = 0; i < numerosCpf.length() - 2; i++) {
            int digito = Integer.parseInt(numerosCpf.substring(i, i + 1));
            d1 += (10 - i) * digito;
            d2 += (11 - i) * digito;
        }

        int resto = d1 % 11;
        int digito1 = resto < 2 ? 0 : 11 - resto;
        d2 += 2 * digito1;
        resto = d2 % 11;
        int digito2 = resto < 2 ? 0 : 11 - resto;

        return numerosCpf.substring(numerosCpf.length() - 2).equals("" + digito1 + digito2);
    }

    public static boolean isData(String data, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, new Locale("pt", "BR"));
            sdf.setLenient(false);
            sdf.parse(data);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    public static boolean isDataExpirada(String data) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/yy", new Locale("pt", "BR"));
            long informada = sdf.parse(data).getTime();
            long atual = sdf.parse(sdf.format(Calendar.getInstance().getTime())).getTime();
            return informada < atual;
        } catch (ParseException e) {
            return true;
        }
    }

    public static boolean nome(String nome, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(nome)) mensagem = "Preencha o nome";
        else if (invalido = nome.length() < 4 || nome.matches("(.*)[^A-Za-zÀ-ú\\s\\.](.*)"))
            mensagem = "Nome inválido";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean sobrenome(String sobrenome, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(sobrenome)) mensagem = "Preencha o sobrenome";
        else if (invalido = sobrenome.length() < 4 || sobrenome.matches("(.*)[^A-Za-zÀ-ú\\s\\.](.*)"))
            mensagem = "Sobrenome inválido";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean email(String email, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(email)) mensagem = "Preencha o e-mail";
        else if (invalido = !Patterns.EMAIL_ADDRESS.matcher(email).matches())
            mensagem = "E-mail inválido";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean senha(String senha, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(senha)) mensagem = "Preencha a senha";
        else if (invalido = senha.length() < 6 || senha.length() > 15)
            mensagem = "A senha deve ter entre 6 e 15 caracteres";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean senha(String senha, String confirmaSenha, EditText campo, EditText campoConfirmacao) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(senha)) mensagem = "Preencha a senha";
        else if (invalido = senha.length() < 6 || senha.length() > 15)
            mensagem = "A senha deve ter entre 6 e 15 caracteres";
        else if (TextUtils.isEmpty(confirmaSenha)) {
            campoConfirmacao.setError("Preencha a confirmação");
            campoConfirmacao.requestFocus();
            return false;
        } else if (invalido = !senha.equals(confirmaSenha)) mensagem = "Senhas diferentes";
        campo.setError(null);
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean cpf(String cpf, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(cpf)) mensagem = "Preencha o CPF";
        else if (invalido = !isCPF(cpf)) mensagem = "CPF inválido";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean dataNascimento(String dataNascimento, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(dataNascimento))
            mensagem = "Preencha a data de nascimento";
        else if (invalido = dataNascimento.length() < 10) mensagem = "Insira no formato DD/MM/AAAA";
        else if (invalido = !isData(dataNascimento, "dd/MM/yyyy"))
            mensagem = "Data de nascimento inválida";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean telefone(String telefone, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(telefone)) mensagem = "Preencha o número de telefone";
        else if (invalido = telefone.length() < 14) mensagem = "Número de telefone incompleto";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean cep(String cep, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(cep)) mensagem = "Preencha o CEP";
        else if (invalido = cep.length() < 10) mensagem = "CEP incompleto";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean endereco(String endereco, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(endereco)) mensagem = "Preencha o endereço";
        else if (invalido = endereco.length() < 4) mensagem = "Endereço muito curto";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean numero(String numero, EditText campo) {
        boolean invalido = TextUtils.isEmpty(numero);
        if (invalido) {
            campo.setError("Preencha o número");
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean bairro(String bairro, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(bairro)) mensagem = "Preencha o bairro";
        else if (invalido = bairro.length() < 3) mensagem = "Bairro muito curto";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean cidade(String cidade, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(cidade)) mensagem = "Preencha a cidade";
        else if (invalido = cidade.length() < 3) mensagem = "Cidade muito curta";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean uf(String uf, Spinner campo) {
        boolean invalido = uf.equals("UF");
        if (invalido) ((TextView) campo.getChildAt(0)).setError("Escolha a UF");
        return !invalido;
    }

    public static boolean numeroCartao(String numeroCartao, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(numeroCartao)) mensagem = "Preencha o número do cartão";
        else if (invalido = numeroCartao.length() < 16) mensagem = "Número do cartão incompleto";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean nomeCartao(String nomeCartao, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(nomeCartao)) mensagem = "Preencha o nome";
        else if (invalido = nomeCartao.length() < 6 || nomeCartao.matches("(.*)[^A-Za-zÀ-ú\\s](.*)"))
            mensagem = "Nome inválido";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean dataValidade(String dataValidade, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(dataValidade)) mensagem = "Preencha a validade";
        else if (invalido = dataValidade.length() < 5) mensagem = "Insira no formato MM/AA";
        else if (invalido = !isData(dataValidade, "MM/yy")) mensagem = "Validade incorreta";
        else if (invalido = isDataExpirada(dataValidade)) mensagem = "Está expirado";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }

    public static boolean cvv(String cvv, EditText campo) {
        String mensagem = null;
        boolean invalido;
        if (invalido = TextUtils.isEmpty(cvv)) mensagem = "Preencha o código de segurança";
        else if (invalido = cvv.length() < 3) mensagem = "Código de segurança incompleto";
        if (invalido) {
            campo.setError(mensagem);
            campo.requestFocus();
        }
        return !invalido;
    }
}
