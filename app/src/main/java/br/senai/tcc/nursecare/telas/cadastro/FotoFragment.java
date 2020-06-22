package br.senai.tcc.nursecare.telas.cadastro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.IOException;

import br.senai.tcc.nursecare.R;

public class FotoFragment extends Fragment implements View.OnClickListener {

    private CircularImageView civFoto;
    private Bitmap bitmap;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_foto, container, false);

        context = getContext();

        civFoto = view.findViewById(R.id.civFoto);
        civFoto.setImageBitmap(getBitmap());

        view.findViewById(R.id.bEscolherFoto).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Escolha uma foto"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                if (data == null) {
                    return;
                }
                try {
                    bitmap = ThumbnailUtils.extractThumbnail(MediaStore.Images.Media.getBitmap(context.getContentResolver(), data.getData()), 300, 300, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                    civFoto.setImageBitmap(bitmap);
                } catch (IOException e) {}
            }
        }
    }

    public boolean validaForm() {
        return true;
    }

    public Bitmap getBitmap() {
        if (bitmap == null) bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.paciente);
        return bitmap;
    }
}
