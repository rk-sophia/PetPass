package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.petpass.tutor.data.api.EnderecoResponse;
import com.petpass.tutor.data.api.RetrofitClient;
import com.petpass.tutor.data.api.ViaCepService;
import com.petpass.tutor.data.model.PetItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etNomeCompleto, etCpf, etCep, etRua, etNumero, etBairro, etCidade;
    private TextView tvTotalMensalCheckout;
    private LinearLayout llContainerPetsCheckout;
    private Button btnFinalizarCompra;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<PetItem> listaPets = new ArrayList<>();
    private double valorTotalMensal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etNomeCompleto = findViewById(R.id.etNomeCompleto);
        etCpf = findViewById(R.id.etCpf);
        etCep = findViewById(R.id.etCep);
        etRua = findViewById(R.id.etRua);
        etNumero = findViewById(R.id.etNumero);
        etBairro = findViewById(R.id.etBairro);
        etCidade = findViewById(R.id.etCidade);
        tvTotalMensalCheckout = findViewById(R.id.tvTotalMensalCheckout);
        llContainerPetsCheckout = findViewById(R.id.llContainerPetsCheckout);
        btnFinalizarCompra = findViewById(R.id.btnFinalizarCompra);

        if (getIntent().hasExtra("LISTA_PETS")) {
            //noinspection unchecked
            listaPets = (List<PetItem>) getIntent().getSerializableExtra("LISTA_PETS");
        }

        renderizarResumoPets();

        btnFinalizarCompra.setOnClickListener(v -> finalizarAssinatura());

        etCep.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 8) {
                    buscarEndereco(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void renderizarResumoPets() {
        llContainerPetsCheckout.removeAllViews();
        valorTotalMensal = 0.0;

        if (listaPets == null || listaPets.isEmpty()) {
            TextView tvVazio = new TextView(this);
            tvVazio.setText("Nenhum pet cadastrado.");
            tvVazio.setTextColor(0xFF888888);
            llContainerPetsCheckout.addView(tvVazio);
            tvTotalMensalCheckout.setText("R$ 0,00");
            return;
        }

        for (PetItem pet : listaPets) {
            valorTotalMensal += pet.getValorFinal();

            TextView tvItem = new TextView(this);
            tvItem.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            String textoPet = String.format(Locale.getDefault(),
                    "• %s (%s, Porte: %s)\n  %s: R$ %.2f",
                    pet.getNome(), pet.getTipo(), pet.getPorte(), pet.getNomePlano(), pet.getValorFinal());

            if (pet.isTemDesconto()) {
                textoPet += " (20% de desconto mesmo porte!)";
            }

            tvItem.setText(textoPet);
            tvItem.setTextSize(14);
            tvItem.setTextColor(0xFF333333);
            tvItem.setPadding(0, 8, 0, 8);

            llContainerPetsCheckout.addView(tvItem);
        }

        tvTotalMensalCheckout.setText(String.format(Locale.getDefault(), "R$ %.2f", valorTotalMensal));
    }

    private void buscarEndereco(String cep) {
        ViaCepService service = RetrofitClient.getViaCepService();
        service.buscarCEP(cep).enqueue(new Callback<EnderecoResponse>() {
            @Override
            public void onResponse(Call<EnderecoResponse> call, Response<EnderecoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EnderecoResponse endereco = response.body();
                    etRua.setText(endereco.getLogradouro());
                    etBairro.setText(endereco.getBairro());
                    etCidade.setText(endereco.getLocalidade() + " - " + endereco.getUf());
                    etNumero.requestFocus();
                }
            }

            @Override
            public void onFailure(Call<EnderecoResponse> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Erro ao buscar CEP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void finalizarAssinatura() {
        String nome = etNomeCompleto.getText().toString().trim();
        String cpf = etCpf.getText().toString().trim();
        String cep = etCep.getText().toString().trim();
        String rua = etRua.getText().toString().trim();
        String numero = etNumero.getText().toString().trim();
        String bairro = etBairro.getText().toString().trim();
        String cidade = etCidade.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(cpf) || TextUtils.isEmpty(cep)) {
            Toast.makeText(this, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> dadosCheckout = new HashMap<>();
        dadosCheckout.put("nomeCompleto", nome);
        dadosCheckout.put("cpf", cpf);
        dadosCheckout.put("cep", cep);
        dadosCheckout.put("enderecoCompleto", rua + ", " + numero + " - " + bairro + ", " + cidade);
        dadosCheckout.put("valorTotalMensal", valorTotalMensal);
        dadosCheckout.put("quantidadePets", listaPets.size());
        dadosCheckout.put("statusAssinatura", "ATIVO");

        db.collection("usuarios").document(userId).set(dadosCheckout, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Salva a subcoleção de pets no Firestore
                    salvarSubcolecaoPets(userId);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao finalizar compra: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void salvarSubcolecaoPets(String userId) {
        if (listaPets.isEmpty()) {
            concluirAssinatura();
            return;
        }

        int[] salvasCount = {0};
        int total = listaPets.size();

        for (PetItem pet : listaPets) {
            Map<String, Object> petMap = new HashMap<>();
            petMap.put("nome", pet.getNome());
            petMap.put("tipo", pet.getTipo());
            petMap.put("raca", pet.getRaca());
            petMap.put("porte", pet.getPorte());
            petMap.put("fotoUrl", pet.getFotoUrl());
            petMap.put("nomePlano", pet.getNomePlano());
            petMap.put("valorBase", pet.getValorBase());
            petMap.put("valorFinal", pet.getValorFinal());
            petMap.put("temDesconto", pet.isTemDesconto());

            db.collection("usuarios").document(userId).collection("pets").add(petMap)
                    .addOnCompleteListener(task -> {
                        salvasCount[0]++;
                        if (salvasCount[0] >= total) {
                            concluirAssinatura();
                        }
                    });
        }
    }

    private void concluirAssinatura() {
        Toast.makeText(CheckoutActivity.this, "Assinatura e cadastros ativados com sucesso!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CheckoutActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
