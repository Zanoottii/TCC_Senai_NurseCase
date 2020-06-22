package br.senai.tcc.nursecare.telas.agendamento;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.senai.tcc.nursecare.R;

public class ServicosFragment extends Fragment implements AdapterView.OnItemClickListener {

    private Context context;
    private List<Servico> listaServicos;
    private ArrayAdapter<Servico> listaServicosAdapter;
    private List<String> servicosSelecionados;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_servicos, container, false);

        context = getContext();

        if (listaServicos == null) {
            listaServicos = new ArrayList<>();
            servicosSelecionados = new ArrayList<>();
            for (String servico : getResources().getStringArray(R.array.servicos))
                listaServicos.add(new Servico(servico));
            listaServicosAdapter = new ListaServicosAdapter();
        }

        ListView lvServicos = view.findViewById(R.id.lvServicos);
        lvServicos.setAdapter(listaServicosAdapter);
        lvServicos.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        listaServicos.get(position).inverter();
        atualizaServicosSelecionados();
        listaServicosAdapter.notifyDataSetChanged();
    }

    public boolean validaForm() {
        if (servicosSelecionados.size() > 0) return true;
        Toast.makeText(context, "Selecione pelo menos um serviço", Toast.LENGTH_SHORT).show();
        return false;
    }

    public List<String> getServicosSelecionados() {
        return servicosSelecionados;
    }

    private void atualizaServicosSelecionados() {
        servicosSelecionados.clear();
        for (Servico servico : listaServicos)
            if (servico.isMarcado())
                servicosSelecionados.add(servico.getDescricao());
    }

    private class ListaServicosAdapter extends ArrayAdapter<Servico> {
        ListaServicosAdapter() {
            super(context, R.layout.layout_servico, listaServicos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View listView = LayoutInflater.from(context).inflate(R.layout.layout_servico, null, true);

            ImageView imageView = listView.findViewById(R.id.imageView);
            TextView textView = listView.findViewById(R.id.textView);

            Servico servico = listaServicos.get(position);
            imageView.setImageResource((servico.isMarcado()) ? R.drawable.indicador_checked : R.drawable.indicador_unchecked);
            textView.setText(servico.getDescricao());

            return listView;
        }
    }

    private class Servico {
        private String descricao;
        private boolean marcado;

        Servico(String descricao) {
            this.descricao = descricao;
            this.marcado = false;
        }

        String getDescricao() {
            return descricao;
        }

        boolean isMarcado() {
            return marcado;
        }

        void inverter() {
            this.marcado = !this.marcado;
        }
    }
}




