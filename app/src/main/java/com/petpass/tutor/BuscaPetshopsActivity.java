package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.petpass.tutor.models.Petshop;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BuscaPetshopsActivity extends AppCompatActivity {

    private RecyclerView rvPetshops;
    private EditText edtFiltro;
    private List<Petshop> todosPetshops = new ArrayList<>();
    private List<Petshop> filtrados = new ArrayList<>();
    private PetshopAdapter adapter;

    private String petId, petNome, planoAtivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busca_petshops);

        petId = getIntent().getStringExtra("PET_ID");
        petNome = getIntent().getStringExtra("PET_NOME");
        planoAtivo = getIntent().getStringExtra("PLANO_ATIVO");
        if (planoAtivo == null) planoAtivo = "NENHUM";

        TextView txtInfo = findViewById(R.id.txtInfoPlanoBusca);
        txtInfo.setText("Pet: " + petNome + " • Plano: " + planoAtivo);

        edtFiltro = findViewById(R.id.edtFiltroEndereco);
        rvPetshops = findViewById(R.id.rvPetshops);
        rvPetshops.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PetshopAdapter();
        rvPetshops.setAdapter(adapter);

        carregarPetshops();

        edtFiltro.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarLista(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void carregarPetshops() {
        FirebaseFirestore.getInstance().collection("petshops")
                .whereEqualTo("ativo", true)
                .get()
                .addOnSuccessListener(snap -> {
                    todosPetshops.clear();
                    for (DocumentSnapshot doc : snap) {
                        Petshop p = doc.toObject(Petshop.class);
                        if (p != null) {
                            if (p.getId() == null) p.setId(doc.getId());
                            todosPetshops.add(p);
                        }
                    }
                    filtrarLista("");
                });
    }

    private void filtrarLista(String termo) {
        filtrados.clear();
        String busca = termo.toLowerCase().trim();
        for (Petshop p : todosPetshops) {
            if (busca.isEmpty()
                    || (p.getNome() != null && p.getNome().toLowerCase().contains(busca))
                    || (p.getEndereco() != null && p.getEndereco().toLowerCase().contains(busca))) {
                filtrados.add(p);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private int nivelPlano(String plano) {
        if ("PLATINUM".equalsIgnoreCase(plano)) return 3;
        if ("GOLD".equalsIgnoreCase(plano)) return 2;
        if ("SILVER".equalsIgnoreCase(plano)) return 1;
        return 0;
    }

    class PetshopAdapter extends RecyclerView.Adapter<PetshopAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_petshop, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Petshop loja = filtrados.get(position);
            String exigido = loja.getPlanoMinimoExigido() != null ? loja.getPlanoMinimoExigido() : "SILVER";

            holder.txtNome.setText(loja.getNome());
            holder.txtEndereco.setText(loja.getEndereco());
            holder.txtPlano.setText("PLANO " + exigido);

            boolean permitido = nivelPlano(planoAtivo) >= nivelPlano(exigido);

            if (permitido) {
                holder.btnAcao.setText("AGENDAR HORÁRIO");
                holder.btnAcao.setBackgroundColor(getColor(R.color.roxo_neon));
                holder.btnAcao.setOnClickListener(v -> {
                    Intent i = new Intent(BuscaPetshopsActivity.this, AgendamentoActivity.class);
                    i.putExtra("PET_ID", petId);
                    i.putExtra("PET_NOME", petNome);
                    i.putExtra("PETSHOP_ID", loja.getId());
                    i.putExtra("PETSHOP_NOME", loja.getNome());
                    i.putStringArrayListExtra("SERVICOS", loja.getServicosDisponiveis() != null ? new ArrayList<>(loja.getServicosDisponiveis()) : new ArrayList<>());
                    i.putStringArrayListExtra("HORARIOS", loja.getHorariosDisponiveis() != null ? new ArrayList<>(loja.getHorariosDisponiveis()) : new ArrayList<>());
                    startActivity(i);
                });
            } else {
                holder.btnAcao.setText("FAZER UPGRADE PARA " + exigido);
                holder.btnAcao.setBackgroundColor(getColor(R.color.ouro_nobre));
                holder.btnAcao.setOnClickListener(v -> {
                    Intent i = new Intent(BuscaPetshopsActivity.this, PlanosActivity.class);
                    i.putExtra("PET_ID", petId);
                    startActivity(i);
                });
            }
        }

        @Override
        public int getItemCount() { return filtrados.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtNome, txtEndereco, txtPlano;
            Button btnAcao;
            ViewHolder(View itemView) {
                super(itemView);
                txtNome = itemView.findViewById(R.id.txtNomeLojaItem);
                txtEndereco = itemView.findViewById(R.id.txtEnderecoLojaItem);
                txtPlano = itemView.findViewById(R.id.txtPlanoExigidoItem);
                btnAcao = itemView.findViewById(R.id.btnAcaoLojaItem);
            }
        }
    }
}