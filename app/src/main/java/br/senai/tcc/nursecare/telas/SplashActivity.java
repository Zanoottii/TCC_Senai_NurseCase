package br.senai.tcc.nursecare.telas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.utilidades.ServicosFirebase;

public class SplashActivity extends AppCompatActivity implements Runnable, ServicosFirebase.ResultadoListener {

    private ServicosFirebase servicosFirebase;
    private ImageView ivLogo, ivProgresso;
    private Handler handler;
    private int contagem = 0;
    private boolean pronto = true;
    private ArrayList<String> permissoes = new ArrayList<>(Arrays.asList(
            "android.permission.INTERNET",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.RECEIVE_BOOT_COMPLETED"));
    private int[] progresso = {R.drawable.seringa1, R.drawable.seringa2, R.drawable.seringa3, R.drawable.seringa4,
            R.drawable.seringa5, R.drawable.seringa6, R.drawable.seringa7, R.drawable.seringa8};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler = new Handler();

        servicosFirebase = new ServicosFirebase(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            permissoes.add("android.permission.FOREGROUND_SERVICE");

        ivLogo = findViewById(R.id.ivLogo);
        ivProgresso = findViewById(R.id.ivProgresso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (servicosFirebase.isLogado()) {
            pronto = false;
            servicosFirebase.carregarUsuario(this);
        }
        animaLogo();
        handler.postDelayed(this, 1500);
    }

    @Override
    public void onSucesso(Object objeto) {
        pronto = true;
    }

    @Override
    public void onErro(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void run() {
        if (contagem < progresso.length) {
            if (contagem == 0) ivProgresso.setVisibility(View.VISIBLE);
            if (contagem == 0 || solicitarPermissao()) handler.postDelayed(this, 200);
            ivProgresso.setImageResource(progresso[contagem]);
        } else {
            if (pronto) {
                if (servicosFirebase.isLogado()) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
                else {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
            }
            else {
                contagem = -1;
                handler.postDelayed(this, 200);
            }
        }
        contagem++;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            handler.postDelayed(this, 200);
        } else if (!shouldShowRequestPermissionRationale(permissions[0])) {
            new AlertDialog.Builder(this)
                    .setTitle("Permissões necessárias")
                    .setMessage("Por favor, entre nas configurações e libere manualmente.")
                    .setCancelable(false)
                    .setPositiveButton("Configurações", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                            startActivityForResult(intent, 100);
                        }
                    }).create().show();
        } else if (ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_DENIED) {
            new AlertDialog.Builder(this)
                    .setTitle("Permissões necessárias")
                    .setMessage("Este aplicativo precisa de permissões para funcionar.")
                    .setCancelable(false)
                    .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{permissoes.get(0)}, 10);
                        }
                    })
                    .setNegativeButton("Sair", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            dialog.cancel();
                        }
                    }).create().show();
        }
    }

    private void animaLogo() {
        ObjectAnimator aAlpha = ObjectAnimator.ofFloat(ivLogo, "alpha", 1f);
        aAlpha.setDuration(1200);

        ObjectAnimator aTranslationY = ObjectAnimator.ofFloat(ivLogo, "translationY", 0f);
        aTranslationY.setDuration(800);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(aAlpha, aTranslationY);
        animatorSet.start();
    }

    private boolean solicitarPermissao() {
        for (int i = permissoes.size() - 1; i >= 0; i--)
            if (ContextCompat.checkSelfPermission(this, permissoes.get(i)) == PackageManager.PERMISSION_GRANTED)
                permissoes.remove(i);
        if (permissoes.size() > 0) {
            ActivityCompat.requestPermissions(this, new String[]{permissoes.get(0)}, 10);
            return false;
        }
        return true;
    }
}
