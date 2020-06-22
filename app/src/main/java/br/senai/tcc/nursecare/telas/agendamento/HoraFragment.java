package br.senai.tcc.nursecare.telas.agendamento;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import br.senai.tcc.nursecare.R;

public class HoraFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener {

    private Context context;
    private EditText etHora, etMinuto;
    private int horaAntes = 0, minutoAntes = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hora, container, false);

        context = getContext();

        etHora = view.findViewById(R.id.etHora);
        etMinuto = view.findViewById(R.id.etMinuto);

        view.findViewById(R.id.ivHoraCima).setOnClickListener(this);
        view.findViewById(R.id.ivMinutoCima).setOnClickListener(this);
        view.findViewById(R.id.ivHoraBaixo).setOnClickListener(this);
        view.findViewById(R.id.ivMinutoBaixo).setOnClickListener(this);

        etHora.setOnFocusChangeListener(this);
        etMinuto.setOnFocusChangeListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        limparFoco();
        int id = v.getId();
        if (id == R.id.ivHoraCima || id == R.id.ivHoraBaixo) {
            int hora = getHora();
            hora = (id == R.id.ivHoraCima) ? (hora + 1) % 24 : (hora + 23) % 24;
            etHora.setText(formatar(hora));
        } else if (id == R.id.ivMinutoCima || id == R.id.ivMinutoBaixo) {
            int minuto = getMinuto();
            minuto = (id == R.id.ivMinutoCima) ? (minuto / 15 + 1) % 4 * 15 : (minuto / 15 + 3) % 4 * 15;
            etMinuto.setText(formatar(minuto));
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int id = v.getId();
        if (id == R.id.etHora) {
            int hora = getHora();
            if (hasFocus) {
                horaAntes = hora;
                etHora.setText("");
            } else
                etHora.setText(formatar(hora > 23 ? horaAntes : hora));
        } else if (id == R.id.etMinuto) {
            int minuto = getMinuto();
            if (hasFocus) {
                minutoAntes = minuto;
                etMinuto.setText("");
            } else
                etMinuto.setText(formatar(minuto > 59 ? minutoAntes : (int) Math.round(minuto / 15.0) % 4 * 15));
        }
    }

    public void limparFoco() {
        if (etHora != null && etMinuto != null) {
            if (etHora.hasFocus()) etHora.clearFocus();
            if (etMinuto.hasFocus()) etMinuto.clearFocus();
        }
    }

    public boolean validaForm() {
        limparFoco();
        return validaHora(getHora()) && validaMinuto(getMinuto());
    }

    public long getHorario() {
        return (getHora() * 3600 + getMinuto() * 60) * 1000;
    }

    private int getHora() {
        int hora = horaAntes;
        try {
            hora = Integer.parseInt(etHora.getText().toString());
        } catch (NumberFormatException e) {
        }
        return hora;
    }

    private int getMinuto() {
        int minuto = minutoAntes;
        try {
            minuto = Integer.parseInt(etMinuto.getText().toString());
        } catch (NumberFormatException e) {
        }
        return minuto;
    }

    private String formatar(int numero) {
        return String.format("%02d", numero);
    }

    private boolean validaHora(int hora) {
        boolean invalido;
        if (invalido = hora < 0 || hora > 24)
            Toast.makeText(context, "Preencha a hora corretamente", Toast.LENGTH_SHORT).show();
        return !invalido;
    }

    private boolean validaMinuto(int minuto) {
        boolean invalido;
        if (invalido = minuto != 0 && minuto != 15 && minuto != 30 && minuto != 45)
            Toast.makeText(context, "Preencha o minuto corretamente", Toast.LENGTH_SHORT).show();
        return !invalido;
    }
}
