package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PerfilPetActivity extends AppCompatActivity {

    private EditText etNomePet, etRacaPet, etPortePet;
    private Button btnSalvarPet;
    private TextView tvNomePlanoSelecionado;
    private RecyclerView rvBeneficiosPlano;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_pet);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etNomePet = findViewById(R.id.etNomePet);
        etRacaPet = findViewById(R.id.etRacaPet);
        etPortePet = findViewById(R.id.etPortePet);
        btnSalvarPet = findViewById(R.id.btnSalvarPet);
        tvNomePlanoSelecionado = findViewById(R.id.tvNomePlanoSelecionado);
        rvBeneficiosPlano = findViewById(R.id.rvBeneficiosPlano);

        String nomePlano = getIntent().getStringExtra("NOME_PLANO");
        ArrayList<String> beneficios = getIntent().getStringArrayListExtra("BENEFICIOS");

        if (nomePlano != null) {
            tvNomePlanoSelecionado.setText(nomePlano);
        }

        if (beneficios != null) {
            BeneficiosAdapter adapter = new BeneficiosAdapter(beneficios);
            rvBeneficiosPlano.setLayoutManager(new LinearLayoutManager(this));
            rvBeneficiosPlano.setAdapter(adapter);
        }

        btnSalvarPet.setOnClickListener(v -> salvarPetESeguir());
    }

    private void salvarPetESeguir() {
        String nome = etNomePet.getText().toString().trim();
        String raca = etRacaPet.getText().toString().trim();
        String porte = etPortePet.getText().toString().trim();

        if (nome.isEmpty() || raca.isEmpty() || porte.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos do pet", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        Map<String, Object> pet = new HashMap<>();
        pet.put("nome", nome);
        pet.put("raca", raca);
        pet.put("porte", porte);
        pet.put("plano", getIntent().getStringExtra("NOME_PLANO"));

        db.collection("usuarios").document(userId).collection("pets").add(pet)
            .addOnSuccessListener(documentReference -> {
                Intent intent = new Intent(PerfilPetActivity.this, CheckoutActivity.class);
                startActivity(intent);
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar pet", Toast.LENGTH_SHORT).show());
    }
}
