package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.petpass.tutor.models.Pet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CadastroPetActivity extends AppCompatActivity {

    private Spinner spnEspecie, spnPorte;
    private EditText edtNome, edtIdade, edtRaca, edtEspecieOutro;
    private TextView txtAvisoOutros;
    private Button btnSalvarPet;
    private FirebaseFirestore db;
    private String uid;
    private boolean primeiroCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_pet);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        primeiroCadastro = getIntent().getBooleanExtra("PRIMEIRO_CADASTRO", false);

        spnEspecie = findViewById(R.id.spnEspecie);
        spnPorte = findViewById(R.id.spnPortePet);
        edtNome = findViewById(R.id.edtNomePet);
        edtIdade = findViewById(R.id.edtIdadePet);
        edtRaca = findViewById(R.id.edtRacaPet);
        edtEspecieOutro = findViewById(R.id.edtEspecieOutro);
        txtAvisoOutros = findViewById(R.id.txtAvisoOutros);
        btnSalvarPet = findViewById(R.id.btnSalvarPet);

        configurarSpinners();

        btnSalvarPet.setOnClickListener(v -> salvarPetNoFirestore());
    }

    private void configurarSpinners() {
        String[] especies = {"Cachorro", "Gato", "Outros"};
        ArrayAdapter<String> adapterEspecie = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, especies);
        spnEspecie.setAdapter(adapterEspecie);

        String[] portes = {"Porte Pequeno", "Porte Médio", "Porte Grande"};
        ArrayAdapter<String> adapterPorte = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, portes);
        spnPorte.setAdapter(adapterPorte);

        spnEspecie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selecionada = especies[position];
                if ("Cachorro".equals(selecionada)) {
                    edtRaca.setVisibility(View.VISIBLE);
                    spnPorte.setVisibility(View.VISIBLE);
                    edtEspecieOutro.setVisibility(View.GONE);
                    txtAvisoOutros.setVisibility(View.GONE);
                } else if ("Gato".equals(selecionada)) {
                    edtRaca.setVisibility(View.VISIBLE);
                    spnPorte.setVisibility(View.GONE);
                    edtEspecieOutro.setVisibility(View.GONE);
                    txtAvisoOutros.setVisibility(View.GONE);
                } else {
                    edtRaca.setVisibility(View.GONE);
                    spnPorte.setVisibility(View.GONE);
                    edtEspecieOutro.setVisibility(View.VISIBLE);
                    txtAvisoOutros.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void salvarPetNoFirestore() {
        String tipo = spnEspecie.getSelectedItem().toString();
        String nome = edtNome.getText().toString().trim();
        String idade = edtIdade.getText().toString().trim();
        String raca = edtRaca.getText().toString().trim();
        String porte = "Cachorro".equals(tipo) ? spnPorte.getSelectedItem().toString() : "";
        String especieFinal = "Outros".equals(tipo) ? edtEspecieOutro.getText().toString().trim() : tipo;

        if (nome.isEmpty() || idade.isEmpty() || especieFinal.isEmpty()) {
            Toast.makeText(this, "Preencha os dados obrigatórios do pet.", Toast.LENGTH_SHORT).show();
            return;
        }

        String petId = db.collection("usuarios").document(uid).collection("pets").document().getId();
        Pet pet = new Pet(petId, nome, especieFinal, raca, porte, idade);

        db.collection("usuarios").document(uid).collection("pets").document(petId)
                .set(pet)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, nome + " cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    if (primeiroCadastro) {
                        // Fluxo guiado: após cadastrar 1º pet, direciona para oferta de planos
                        Intent i = new Intent(this, PlanosActivity.class);
                        i.putExtra("PET_ID", petId);
                        startActivity(i);
                    }
                    finish();
                });
    }
}