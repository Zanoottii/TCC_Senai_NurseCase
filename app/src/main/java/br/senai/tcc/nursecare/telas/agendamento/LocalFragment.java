package br.senai.tcc.nursecare.telas.agendamento;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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

import br.senai.tcc.nursecare.telas.AgendamentoActivity;

import br.senai.tcc.nursecare.R;
import br.senai.tcc.nursecare.modelos.Paciente;
import br.senai.tcc.nursecare.utilidades.Comum;
import br.senai.tcc.nursecare.utilidades.Usuario;
import br.senai.tcc.nursecare.utilidades.Validacao;

public class LocalFragment extends Fragment implements RadioGroup.OnCheckedChangeListener, LocationListener, View.OnKeyListener, Response.Listener<JSONObject>, Response.ErrorListener, AdapterView.OnItemSelectedListener {

    private AgendamentoActivity activity;
    private Paciente paciente;
    private ProgressDialog progress;
    private Geocoder geocoder;
    private String endereco;
    private double latitude, longitude;
    private LinearLayout llMeuEndereco, llGpsEndereco, llOutroEndereco;
    private TextView tvMeuEndereco, tvGpsEndereco, tvOutroUf;
    private TextInputEditText etOutroCep, etOutroNumero, etOutroComplemento, etOutroEndereco, etOutroBairro, etOutroCidade;
    private Spinner sOutroUf;
    private ArrayAdapter<CharSequence> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local, container, false);

        activity = (AgendamentoActivity) getActivity();
        paciente = Usuario.getInstance().getPaciente();
        geocoder = new Geocoder(activity, Locale.getDefault());

        RadioGroup rgLocal = view.findViewById(R.id.rgLocal);
        rgLocal.setOnCheckedChangeListener(this);

        llMeuEndereco = view.findViewById(R.id.llMeuEndereco);
        llGpsEndereco = view.findViewById(R.id.llGpsEndereco);
        llOutroEndereco = view.findViewById(R.id.llOutroEndereco);
        tvMeuEndereco = view.findViewById(R.id.tvMeuEndereco);
        tvGpsEndereco = view.findViewById(R.id.tvGpsEndereco);
        etOutroCep = view.findViewById(R.id.etOutroCep);
        etOutroNumero = view.findViewById(R.id.etOutroNumero);
        etOutroComplemento = view.findViewById(R.id.etOutroComplemento);
        etOutroEndereco = view.findViewById(R.id.etOutroEndereco);
        etOutroBairro = view.findViewById(R.id.etOutroBairro);
        etOutroCidade = view.findViewById(R.id.etOutroCidade);
        tvOutroUf = view.findViewById(R.id.tvOutroUf);
        sOutroUf = view.findViewById(R.id.sOutroUf);

        adapter = ArrayAdapter.createFromResource(activity, R.array.uf, R.layout.spinner_item_gray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sOutroUf.setAdapter(adapter);
        sOutroUf.setSelection(0, false);
        sOutroUf.setOnItemSelectedListener(this);

        etOutroCep.addTextChangedListener(new SimpleMaskTextWatcher(etOutroCep, new SimpleMaskFormatter("NN.NNN-NNN")));
        etOutroCep.setOnKeyListener(this);

        progress = Comum.progress(activity);

        preencheMeuEndereco();

        return view;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rbMeuEndereco:
                llMeuEndereco.setVisibility(View.VISIBLE);
                llGpsEndereco.setVisibility(View.GONE);
                llOutroEndereco.setVisibility(View.GONE);
                preencheMeuEndereco();
                break;
            case R.id.rbGpsEndereco:
                llMeuEndereco.setVisibility(View.GONE);
                llGpsEndereco.setVisibility(View.VISIBLE);
                llOutroEndereco.setVisibility(View.GONE);
                preencheGpsEndereco();
                break;
            case R.id.rbOutroEndereco:
                llMeuEndereco.setVisibility(View.GONE);
                llGpsEndereco.setVisibility(View.GONE);
                llOutroEndereco.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        List<Address> addresses = null;
        progress.show();
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
        }
        progress.dismiss();
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            endereco = address.getThoroughfare() + ", " + address.getFeatureName() + " - " + address.getSubLocality() + " - " + address.getSubAdminArea() + " - " + address.getAdminArea();
            latitude = lat;
            longitude = lng;
            tvGpsEndereco.setText(endereco);
        } else {
            limpaEndereco();
            tvGpsEndereco.setText("Endereço não disponível");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        TextInputEditText campo = (TextInputEditText) v;
        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (campo.length() == 10) {
                progress.show();
                campo.setEnabled(false);
                Volley.newRequestQueue(activity).add(new JsonObjectRequest(Request.Method.GET, "https://viacep.com.br/ws/" + campo.getText().toString().replaceAll("[^0-9]", "") + "/json/", null, this, this));
            }
        }
        return false;
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            etOutroCep.clearFocus();
            etOutroCep.setText(response.getString("cep"));
            etOutroEndereco.setText(response.getString("logradouro"));
            etOutroBairro.setText(response.getString("bairro"));
            etOutroCidade.setText(response.getString("localidade"));
            sOutroUf.setSelection(adapter.getPosition(response.getString("uf")));
            etOutroNumero.requestFocus();
        } catch (JSONException e) {
            etOutroCep.setText("");
            etOutroEndereco.setText("");
            etOutroBairro.setText("");
            etOutroCidade.setText("");
            tvOutroUf.setVisibility(View.INVISIBLE);
            sOutroUf.setSelection(0);
            etOutroCep.setError("CEP inválido");
        }
        etOutroCep.setEnabled(true);
        progress.dismiss();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        etOutroCep.setEnabled(true);
        progress.dismiss();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            ((TextView) view).setTextColor(ContextCompat.getColor(activity, android.R.color.darker_gray));
            tvOutroUf.setVisibility(View.INVISIBLE);
        } else {
            ((TextView) view).setTextColor(ContextCompat.getColor(activity, android.R.color.black));
            tvOutroUf.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public boolean validaForm() {
        return (llOutroEndereco.getVisibility() != View.VISIBLE ||
                (Validacao.numero(getNumero(), etOutroNumero) &&
                        Validacao.endereco(getEndereco(), etOutroEndereco) &&
                        Validacao.bairro(getBairro(), etOutroBairro) &&
                        Validacao.cidade(getCidade(), etOutroCidade) &&
                        Validacao.uf(getUf(), sOutroUf) &&
                        atualizaOutroEndereco(getOutroEnderecoParcial()))) &&
                validaCoordenadas();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getEnderecoCompleto() {
        return endereco;
    }

    private String getEndereco() {
        return etOutroEndereco.getText().toString().trim();
    }

    private String getNumero() {
        return etOutroNumero.getText().toString().trim();
    }

    private String getComplemento() {
        return etOutroComplemento.getText().toString().trim();
    }

    private String getBairro() {
        return etOutroBairro.getText().toString().trim();
    }

    private String getCidade() {
        return etOutroCidade.getText().toString().trim();
    }

    private String getUf() {
        return sOutroUf.getSelectedItem().toString();
    }

    private String getOutroEnderecoParcial() {
        return getEndereco() + ", " + getNumero() + " - " + getBairro() + " - " + getCidade() + " - " + getUf();
    }

    private String getOutroEnderecoCompleto() {
        return getEndereco() + ", " + getNumero() + " " + getComplemento() + " - " + getBairro() + " - " + getCidade() + " - " + getUf();
    }

    private boolean validaCoordenadas() {
        boolean invalido = latitude == 0 || longitude == 0;
        if (invalido)
            Toast.makeText(activity, "Este endereço não é válido", Toast.LENGTH_SHORT).show();
        return !invalido;
    }

    private boolean atualizaOutroEndereco(String enderecoParcial) {
        limpaEndereco();
        List<Address> addresses = null;
        progress.show();
        try {
            addresses = geocoder.getFromLocationName(enderecoParcial, 1);
        } catch (IOException e) {}
        progress.dismiss();
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            latitude = address.getLatitude();
            longitude = address.getLongitude();
            endereco = getOutroEnderecoCompleto();
            return true;
        }
        return false;
    }

    private void limpaEndereco() {
        latitude = 0;
        longitude = 0;
        endereco = "";
    }

    private void preencheMeuEndereco() {
        if (paciente != null) {
            endereco = paciente.getLogradouro() + ", " + paciente.getNumero() +
                    (paciente.getComplemento().length() > 0 ? " " + paciente.getComplemento() : "") + " - " +
                    paciente.getBairro() + " - " + paciente.getMunicipio() + " - " + paciente.getUf();
            latitude = paciente.getLatitude();
            longitude = paciente.getLongitude();
            tvMeuEndereco.setText(endereco);
        } else {
            limpaEndereco();
            tvMeuEndereco.setText("Endereço não encontrado");
        }
    }

    private void preencheGpsEndereco() {
        limpaEndereco();
        tvGpsEndereco.setText("Aguardando localização...");
        progress.show();
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            boolean gpsIsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (gpsIsEnabled) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    return;
                else {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) onLocationChanged(location);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1, this);
                }
            } else {
                Toast.makeText(activity, "GPS está desabilitado", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, "Sem serviço de localização", Toast.LENGTH_SHORT).show();
        }
        progress.dismiss();
    }
}
