package br.senai.tcc.nursecare.telas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.IOException;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.utilidades.Comum;
import br.senai.tcc.nursecare.utilidades.ServicosFirebase;
import br.senai.tcc.nursecare.utilidades.Usuario;

public class UsuarioActivity extends AppCompatActivity implements View.OnClickListener {

    private Usuario usuario;
    private ServicosFirebase servicosFirebase;
    private LinearLayout llFotoCheia;
    private CircularImageView civFotoUsuario;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        usuario = Usuario.getInstance();

        servicosFirebase = new ServicosFirebase(this);

        civFotoUsuario = findViewById(R.id.civFotoUsuario);
        llFotoCheia = findViewById(R.id.llFotoCheia);

        civFotoUsuario.setImageBitmap(usuario.getFoto());
        ((ImageView) findViewById(R.id.ivFotoCheia)).setImageBitmap(usuario.getFoto());

        civFotoUsuario.setOnClickListener(this);
        findViewById(R.id.ivFotoEditar).setOnClickListener(this);
        findViewById(R.id.ivDadosUsuario).setOnClickListener(this);
        findViewById(R.id.ivEnderecoUsuario).setOnClickListener(this);
        findViewById(R.id.ivPagamentoUsuario).setOnClickListener(this);
        findViewById(R.id.bDeslogar).setOnClickListener(this);
        llFotoCheia.setOnClickListener(this);

        progressDialog = Comum.progress(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.civFotoUsuario:
                llFotoCheia.setVisibility(View.VISIBLE);
                break;
            case R.id.ivFotoEditar:
                intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Escolha uma foto"), 1);
                break;
            case R.id.ivDadosUsuario:
                intent = new Intent(this, EditarActivity.class);
                intent.putExtra("fragment", "dados");
                startActivity(intent);
                break;
            case R.id.ivEnderecoUsuario:
                intent = new Intent(this, EditarActivity.class);
                intent.putExtra("fragment", "endereco");
                startActivity(intent);
                break;
            case R.id.ivPagamentoUsuario:
                intent = new Intent(this, EditarActivity.class);
                intent.putExtra("fragment", "pagamento");
                startActivity(intent);
                break;
            case R.id.bDeslogar:
                servicosFirebase.deslogar();
                break;
            case R.id.llFotoCheia:
                llFotoCheia.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                if (data == null) {
                    return;
                }
                new AlertDialog.Builder(this)
                        .setCancelable(true)
                        .setTitle("Foto do paciente")
                        .setMessage("Deseja trocar a foto por esta?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    final Bitmap bitmap = ThumbnailUtils.extractThumbnail(MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData()), 300, 300, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                                    if (bitmap != null) {
                                        progressDialog.show();
                                        servicosFirebase.uploadFoto(bitmap, new ServicosFirebase.ResultadoListener() {
                                            @Override
                                            public void onSucesso(Object objeto) {
                                                progressDialog.dismiss();
                                                usuario.setFoto(bitmap);
                                                civFotoUsuario.setImageBitmap(bitmap);
                                            }

                                            @Override
                                            public void onErro(String mensagem) {
                                                progressDialog.dismiss();
                                                Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } catch (IOException e) {
                                    Toast.makeText(getApplicationContext(), "Erro ao manipular a foto", Toast.LENGTH_SHORT).show();
                                }
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
    }
}
