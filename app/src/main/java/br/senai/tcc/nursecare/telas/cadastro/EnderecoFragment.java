package br.senai.tcc.nursecare.telas.cadastro;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.SimpleMaskTextWatcher;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.utilidades.Comum;
import br.senai.tcc.nursecare.utilidades.Validacao;

public class EnderecoFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnKeyListener, Response.Listener<JSONObject>, Response.ErrorListener {

    private TextInputEditText etCep, etEndereco, etNumero, etComplemento, etBairro, etCidade;
    private Spinner sUf;
    private TextView tvUf;
    private ArrayAdapter<CharSequence> adapter;
    private ProgressDialog progress;
    private Context context;
    private String endereco;
    private double latitude, longitude;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_endereco, container, false);

        context = getContext();

        etCep = view.findViewById(R.id.etCep);
        etEndereco = view.findViewById(R.id.etEndereco);
        etNumero = view.findViewById(R.id.etNumero);
        etComplemento = view.findViewById(R.id.etComplemento);
        etBairro = view.findViewById(R.id.etBairro);
        etCidade = view.findViewById(R.id.etCidade);
        tvUf = view.findViewById(R.id.tvUf);
        sUf = view.findViewById(R.id.sUf);

        adapter = ArrayAdapter.createFromResource(context, R.array.uf, R.layout.spinner_item_gray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sUf.setAdapter(adapter);
        sUf.setSelection(0, false);
        sUf.setOnItemSelectedListener(this);

        etCep.addTextChangedListener(new SimpleMaskTextWatcher(etCep, new SimpleMaskFormatter("NN.NNN-NNN")));
        etCep.setOnKeyListener(this);
        etCep.requestFocus();

        progress = Comum.progress(context);

        return view;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        TextInputEditText campo = (TextInputEditText) v;
        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (campo.length() == 10) {
                progress.show();
                campo.setEnabled(false);
                Volley.newRequestQueue(context).add(new JsonObjectRequest(Request.Method.GET, "https://viacep.com.br/ws/" + campo.getText().toString().replaceAll("[^0-9]", "") + "/json/", null, this, this));
            }
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            ((TextView) view).setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            tvUf.setVisibility(View.INVISIBLE);
            sUf.setPaddingRelative(0, getResources().getDimensionPixelSize(R.dimen.quatro_dp), 0, getResources().getDimensionPixelSize(R.dimen.quatro_dp));
        } else {
            ((TextView) view).setTextColor(ContextCompat.getColor(context, android.R.color.black));
            tvUf.setVisibility(View.VISIBLE);
            sUf.setPadding(0, getResources().getDimensionPixelSize(R.dimen.seis_dp), 0, getResources().getDimensionPixelSize(R.dimen.dois_dp));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            etCep.clearFocus();
            etCep.setText(response.getString("cep"));
            etEndereco.setText(response.getString("logradouro"));
            etBairro.setText(response.getString("bairro"));
            etCidade.setText(response.getString("localidade"));
            sUf.setSelection(adapter.getPosition(response.getString("uf")));
            etNumero.requestFocus();
        } catch (JSONException e) {
            etCep.setText("");
            etEndereco.setText("");
            etBairro.setText("");
            etCidade.setText("");
            tvUf.setVisibility(View.INVISIBLE);
            sUf.setSelection(0);
            etCep.setError("CEP inválido");
        }
        etCep.setEnabled(true);
        progress.dismiss();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        etCep.setEnabled(true);
        progress.dismiss();
    }

    public boolean validaForm() {
        return Validacao.cep(getCep(), etCep) &&
                Validacao.endereco(getEndereco(), etEndereco) &&
                Validacao.numero(getNumero(), etNumero) &&
                Validacao.bairro(getBairro(), etBairro) &&
                Validacao.cidade(getCidade(), etCidade) &&
                Validacao.uf(getUf(), sUf) &&
                validaCoordenadas();
    }

    public String getCep() {
        return etCep.getText().toString().trim();
    }

    public String getEndereco() {
        return etEndereco.getText().toString().trim();
    }

    public String getNumero() {
        return etNumero.getText().toString().trim();
    }

    public String getComplemento() {
        return etComplemento.getText().toString().trim();
    }

    public String getBairro() {
        return etBairro.getText().toString().trim();
    }

    public String getCidade() {
        return etCidade.getText().toString().trim();
    }

    public String getUf() {
        return sUf.getSelectedItem().toString();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    private String getEnderecoCompleto() {
        return getEndereco() + ", " + getNumero() + " - " + getBairro() + " - " + getCidade() + " - " + getUf();
    }

    private boolean validaCoordenadas() {
        atualizaEndereco(getEnderecoCompleto());
        boolean invalido = latitude == 0 || longitude == 0;
        if (invalido) Toast.makeText(context, "Este endereço não é válido", Toast.LENGTH_SHORT).show();
        return !invalido;
    }

    private void atualizaEndereco(String enderecoNovo) {
        if (!enderecoNovo.equals(endereco)) {
            latitude = 0;
            longitude = 0;
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = null;
            progress.show();
            try {
                addresses = geocoder.getFromLocationName(enderecoNovo, 1);
            } catch (IOException e) {
            }
            progress.dismiss();
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                latitude = address.getLatitude();
                longitude = address.getLongitude();
                endereco = enderecoNovo;
            }
        }
    }
}
