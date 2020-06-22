package br.senai.tcc.nursecare.utilidades;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import java.text.NumberFormat;
import java.util.Locale;

import br.senai.tcc.nursecare.R;

public class Comum {

    public static String[] ESTADO_MSG = {
            "Não atendido",
            "Aguardando aceitação",
            "Cancelado",
            "Atendido",
            "Atendimento agendado"
    };

    public static int[] ESTADO_IMG = {
            R.drawable.ic_event_busy,
            R.drawable.ic_help_outline,
            R.drawable.ic_event_busy,
            R.drawable.ic_assignment_turned_in,
            R.drawable.ic_event_available
    };

    public static ProgressDialog progress(Context context) {
        ProgressDialog progress = new ProgressDialog(context);
        progress.setTitle("Carregando");
        progress.setMessage("Aguarde...");
        progress.setCancelable(false);
        return progress;
    }

    public static String formatoReais(String valor) {
        return formatoReais(Double.parseDouble(valor));
    }

    public static String formatoReais(double valor) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        return "R$ " + numberFormat.format(valor);
    }

    public static void infoCvv(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_info_cvv, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setCancelable(false).setView(view);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        view.findViewById(R.id.bEntendido).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }
}
