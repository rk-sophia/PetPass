package com.petpass.tutor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.petpass.tutor.data.model.PetItem;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PerfilPetActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 71;

    private EditText etNomePet, etRacaPet;
    private Spinner spTipoPet, spPortePet, spPlanoPet;
    private ImageView imgPetFoto;
    private TextView tvResumoPets;
    private Button btnAdicionarOutroPet, btnSalvarPet;

    private Uri filePath;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private final List<PetItem> listaPets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_pet);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        etNomePet = findViewById(R.id.etNomePet);
        etRacaPet = findViewById(R.id.etRacaPet);
        spTipoPet = findViewById(R.id.spTipoPet);
        spPortePet = findViewById(R.id.spPortePet);
        spPlanoPet = findViewById(R.id.spPlanoPet);
        imgPetFoto = findViewById(R.id.imgPetFoto);
        tvResumoPets = findViewById(R.id.tvResumoPets);
        btnAdicionarOutroPet = findViewById(R.id.btnAdicionarOutroPet);
        btnSalvarPet = findViewById(R.id.btnSalvarPet);

        imgPetFoto.setOnClickListener(v -> escolherImagem());

        btnAdicionarOutroPet.setOnClickListener(v -> processarEAdicionarPet(false));
        btnSalvarPet.setOnClickListener(v -> processarEAdicionarPet(true));

        atualizarResumo();
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

    private void processarEAdicionarPet(boolean irParaCheckout) {
        String nome = etNomePet.getText().toString().trim();
        String raca = etRacaPet.getText().toString().trim();
        String tipo = spTipoPet.getSelectedItem() != null ? spTipoPet.getSelectedItem().toString() : "Cachorro";
        String porte = spPortePet.getSelectedItem() != null ? spPortePet.getSelectedItem().toString() : "Médio";
        String planoSelecionadoStr = spPlanoPet.getSelectedItem() != null ? spPlanoPet.getSelectedItem().toString() : "Plano Premium";

        // Se o formulário estiver preenchido, adiciona o pet atual
        if (!nome.isEmpty() && !raca.isEmpty()) {
            if (filePath != null) {
                StorageReference ref = storageReference.child("pets/" + UUID.randomUUID().toString());
                ref.putFile(filePath)
                        .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            adicionarPetNaLista(nome, tipo, raca, porte, planoSelecionadoStr, uri.toString());
                            concluirAcao(irParaCheckout);
                        }))
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Erro no upload da foto. Salvando sem foto.", Toast.LENGTH_SHORT).show();
                            adicionarPetNaLista(nome, tipo, raca, porte, planoSelecionadoStr, null);
                            concluirAcao(irParaCheckout);
                        });
                return;
            } else {
                adicionarPetNaLista(nome, tipo, raca, porte, planoSelecionadoStr, null);
            }
        } else if (irParaCheckout && listaPets.isEmpty()) {
            Toast.makeText(this, "Preencha o nome e raça do pet para continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        concluirAcao(irParaCheckout);
    }

    private void adicionarPetNaLista(String nome, String tipo, String raca, String porte, String planoStr, String fotoUrl) {
        String nomePlano = "Plano Premium";
        double valorBase = 89.90;

        if (planoStr.contains("Gold")) {
            nomePlano = "Plano Gold";
            valorBase = 139.90;
        } else if (planoStr.contains("Platina")) {
            nomePlano = "Plano Platina";
            valorBase = 199.90;
        }

        // Regra de Desconto por Mesmo Porte:
        // Verifica se já existe pet adicionado na lista com o mesmo porte.
        boolean mesmoPorteExiste = false;
        for (PetItem p : listaPets) {
            if (p.getPorte().equalsIgnoreCase(porte)) {
                mesmoPorteExiste = true;
                break;
            }
        }

        double valorFinal = valorBase;
        boolean temDesconto = false;
        if (mesmoPorteExiste) {
            valorFinal = valorBase * 0.80; // 20% de desconto no plano para pets do mesmo porte
            temDesconto = true;
        }

        PetItem petItem = new PetItem(nome, tipo, raca, porte, fotoUrl, nomePlano, valorBase, valorFinal, temDesconto);
        listaPets.add(petItem);

        limparFormulario();
        atualizarResumo();

        if (temDesconto) {
            Toast.makeText(this, "Pet " + nome + " adicionado com 20% de desconto (mesmo porte)!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Pet " + nome + " adicionado com sucesso!", Toast.LENGTH_SHORT).show();
        }
    }

    private void limparFormulario() {
        etNomePet.setText("");
        etRacaPet.setText("");
        imgPetFoto.setImageResource(android.R.drawable.ic_menu_camera);
        filePath = null;
    }

    private void atualizarResumo() {
        tvResumoPets.setText("Pets cadastrados até agora: " + listaPets.size());
    }

    private void concluirAcao(boolean irParaCheckout) {
        if (irParaCheckout) {
            if (listaPets.isEmpty()) {
                Toast.makeText(this, "Cadastre pelo menos um pet para avançar", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(PerfilPetActivity.this, CheckoutActivity.class);
            intent.putExtra("LISTA_PETS", new ArrayList<>(listaPets));
            startActivity(intent);
        }
    }
}
