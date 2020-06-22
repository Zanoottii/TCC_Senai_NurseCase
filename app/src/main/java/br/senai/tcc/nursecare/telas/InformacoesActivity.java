package br.senai.tcc.nursecare.telas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import br.senai.tcc.nursecare.R;

public class InformacoesActivity extends AppCompatActivity implements View.OnClickListener {

    int[] tvLabelServicoId = {R.id.tvLabelServico01, R.id.tvLabelServico02, R.id.tvLabelServico03,
            R.id.tvLabelServico04, R.id.tvLabelServico05, R.id.tvLabelServico06,
            R.id.tvLabelServico07, R.id.tvLabelServico08, R.id.tvLabelServico09,
            R.id.tvLabelServico10, R.id.tvLabelServico11, R.id.tvLabelServico12};
    int[] ivLabelServicoId = {R.id.ivLabelServico01, R.id.ivLabelServico02, R.id.ivLabelServico03,
            R.id.ivLabelServico04, R.id.ivLabelServico05, R.id.ivLabelServico06,
            R.id.ivLabelServico07, R.id.ivLabelServico08, R.id.ivLabelServico09,
            R.id.ivLabelServico10, R.id.ivLabelServico11, R.id.ivLabelServico12};
    int[] llServicoId = {R.id.llServico01, R.id.llServico02, R.id.llServico03,
            R.id.llServico04, R.id.llServico05, R.id.llServico06,
            R.id.llServico07, R.id.llServico08, R.id.llServico09,
            R.id.llServico10, R.id.llServico11, R.id.llServico12};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informacoes);

        String[] servicos = getResources().getStringArray(R.array.servicos);
        for (int i = 0; i < tvLabelServicoId.length; i++) {
            ((TextView) findViewById(tvLabelServicoId[i])).setText(servicos[i]);
            ImageView imageView = findViewById(ivLabelServicoId[i]);
            imageView.setTag(new Estado(i, false));
            imageView.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        Estado e = (Estado) v.getTag();
        findViewById(llServicoId[e.id]).setVisibility(e.visivel ? View.GONE : View.VISIBLE);
        ((ImageView) v).setImageDrawable(getDrawable(e.visivel ? R.drawable.ic_arrow_right : R.drawable.ic_arrow_down));
        v.setTag(new Estado(e.id, !e.visivel));
    }

    private static class Estado {
        int id;
        boolean visivel;

        Estado(int id, boolean visivel) {
            this.id = id;
            this.visivel = visivel;
        }
    }
}
