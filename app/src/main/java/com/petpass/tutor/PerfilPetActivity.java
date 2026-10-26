package com.petpass.tutor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PerfilPetActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 71;
    
    private EditText etNomePet, etRacaPet, etPortePet;
    private Spinner spTipoPet;
    private ImageView imgPetFoto;
    private Button btnSalvarPet;
    private TextView tvNomePlanoSelecionado;
    private RecyclerView rvBeneficiosPlano;
    
    private Uri filePath;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_pet);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        etNomePet = findViewById(R.id.etNomePet);
        etRacaPet = findViewById(R.id.etRacaPet);
        etPortePet = findViewById(R.id.etPortePet);
        spTipoPet = findViewById(R.id.spTipoPet);
        imgPetFoto = findViewById(R.id.imgPetFoto);
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

        imgPetFoto.setOnClickListener(v -> escolherImagem());
        btnSalvarPet.setOnClickListener(v -> salvarPetESeguir());
    }

    private void escolherImagem() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecione a foto do pet"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            imgPetFoto.setImageURI(filePath);
        }
    }

    private void salvarPetESeguir() {
        String nome = etNomePet.getText().toString().trim();
        String raca = etRacaPet.getText().toString().trim();
        String porte = etPortePet.getText().toString().trim();
        String tipo = spTipoPet.getSelectedItem().toString();

        if (nome.isEmpty() || raca.isEmpty() || porte.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos do pet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (filePath != null) {
            uploadImagemESalvar(nome, raca, porte, tipo);
        } else {
            salvarNoFirestore(nome, raca, porte, tipo, null);
        }
    }

    private void uploadImagemESalvar(String nome, String raca, String porte, String tipo) {
        StorageReference ref = storageReference.child("pets/" + UUID.randomUUID().toString());
        ref.putFile(filePath)
            .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                salvarNoFirestore(nome, raca, porte, tipo, uri.toString());
            }))
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Falha no upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                salvarNoFirestore(nome, raca, porte, tipo, null);
            });
    }

    private void salvarNoFirestore(String nome, String raca, String porte, String tipo, @Nullable String fotoUrl) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        Map<String, Object> pet = new HashMap<>();
        pet.put("nome", nome);
        pet.put("raca", raca);
        pet.put("porte", porte);
        pet.put("tipo", tipo);
        pet.put("fotoUrl", fotoUrl);
        String nomePlano = getIntent().getStringExtra("NOME_PLANO");
        pet.put("plano", nomePlano);

        db.collection("usuarios").document(userId).collection("pets").add(pet)
            .addOnSuccessListener(documentReference -> {
                Intent intent = new Intent(PerfilPetActivity.this, CheckoutActivity.class);
                intent.putExtra("NOME_PLANO", nomePlano); // Garante que o plano chegue no checkout
                startActivity(intent);
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar pet", Toast.LENGTH_SHORT).show());
    }
}
