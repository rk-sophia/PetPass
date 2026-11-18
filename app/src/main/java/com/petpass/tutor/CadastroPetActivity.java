package com.petpass.tutor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.petpass.tutor.data.api.*;
import com.petpass.tutor.util.SessionManager;

import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroPetActivity extends AppCompatActivity {

    private ImageView imgFotoPet;
    private EditText etNomePet;
    private Spinner spinnerEspecie, spinnerPorte;
    private LinearLayout layoutPorteCondicional;
    private Uri fotoUriSelecionada;
    private SessionManager session;

    private final ActivityResultLauncher<String> selecionarImagemLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    fotoUriSelecionada = uri;
                    imgFotoPet.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_pet);

        session = new SessionManager(this);
        imgFotoPet = findViewById(R.id.imgFotoPet);
        etNomePet = findViewById(R.id.etNomePet);
        spinnerEspecie = findViewById(R.id.spinnerEspecie);
        spinnerPorte = findViewById(R.id.spinnerPorte);
        layoutPorteCondicional = findViewById(R.id.layoutPorteCondicional);

        findViewById(R.id.btnEscolherFoto).setOnClickListener(v -> selecionarImagemLauncher.launch("image/*"));
        findViewById(R.id.btnSalvarPet).setOnClickListener(v -> salvarPet());

        configurarSpinners();
    }

    private void configurarSpinners() {
        String[] especies = {"Cão", "Gato"};
        spinnerEspecie.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, especies));

        String[] portes = {"Pequeno", "Médio", "Grande", "Gigante"};
        spinnerPorte.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, portes));

        spinnerEspecie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                layoutPorteCondicional.setVisibility(especies[position].equals("Cão") ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void salvarPet() {
        String nome = etNomePet.getText().toString().trim();
        if (nome.isEmpty()) {
            Toast.makeText(this, "Informe o nome do pet.", Toast.LENGTH_SHORT).show();
            return;
        }

        String especie = spinnerEspecie.getSelectedItem().toString();
        String porte = especie.equals("Cão") ? spinnerPorte.getSelectedItem().toString() : "N/A";

        RequestBody tutorIdPart = RequestBody.create(MediaType.parse("text/plain"), session.getTutorId());
        RequestBody nomePart = RequestBody.create(MediaType.parse("text/plain"), nome);
        RequestBody especiePart = RequestBody.create(MediaType.parse("text/plain"), especie);
        RequestBody portePart = RequestBody.create(MediaType.parse("text/plain"), porte);

        MultipartBody.Part fotoPart = null;
        if (fotoUriSelecionada != null && fotoUriSelecionada.getPath() != null) {
            File file = new File(fotoUriSelecionada.getPath());
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            fotoPart = MultipartBody.Part.createFormData("foto", file.getName(), reqFile);
        }

        ApiClient.getService().cadastrarPetComFoto(tutorIdPart, nomePart, especiePart, portePart, fotoPart)
                .enqueue(new Callback<Pet>() {
                    @Override
                    public void onResponse(Call<Pet> call, Response<Pet> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Intent intent = new Intent(CadastroPetActivity.this, PlanosActivity.class);
                            intent.putExtra("PET_OBJ", response.body());
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(CadastroPetActivity.this, "Erro ao salvar pet: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Pet> call, Throwable t) {
                        Toast.makeText(CadastroPetActivity.this, "Falha na rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}