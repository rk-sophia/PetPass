package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.petpass.tutor.models.Pet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PerfilTutorActivity extends AppCompatActivity {

    private EditText edtNome, edtEndereco;
    private TextView txtListaPets;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_tutor);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        edtNome = findViewById(R.id.edtNomePerfil);
        edtEndereco = findViewById(R.id.edtEnderecoPerfil);
        txtListaPets = findViewById(R.id.txtListaPetsPerfil);
        Button btnSalvar = findViewById(R.id.btnSalvarPerfil);
        Button btnNovoPet = findViewById(R.id.btnAdicionarNovoPet);

        carregarDados();

        btnSalvar.setOnClickListener(v -> salvarAlteracoes());
        btnNovoPet.setOnClickListener(v -> startActivity(new Intent(this, CadastroPetActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarPets();
    }

    private void carregarDados() {
        db.collection("usuarios").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                edtNome.setText(doc.getString("nomeCompleto"));
                edtEndereco.setText(doc.getString("endereco"));
            }
        });
    }

    private void carregarPets() {
        db.collection("usuarios").document(uid).collection("pets").get().addOnSuccessListener(snap -> {
            StringBuilder sb = new StringBuilder();
            for (DocumentSnapshot doc : snap) {
                Pet p = doc.toObject(Pet.class);
                if (p != null) {
                    sb.append("• ").append(p.getNome())
                            .append(" (").append(p.getEspecie()).append(")\n")
                            .append("  Plano: ").append(p.getPlanoAtivo())
                            .append(" | Check-ins: ").append(p.getCheckinsRealizados())
                            .append("\n\n");
                }
            }
            txtListaPets.setText(sb.length() > 0 ? sb.toString().trim() : "Nenhum pet cadastrado.");
        });
    }

    private void salvarAlteracoes() {
        Map<String, Object> dados = new HashMap<>();
        dados.put("nomeCompleto", edtNome.getText().toString().trim());
        dados.put("endereco", edtEndereco.getText().toString().trim());

        db.collection("usuarios").document(uid).update(dados)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Perfil atualizado!", Toast.LENGTH_SHORT).show());
    }
}