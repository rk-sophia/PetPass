package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.petpass.tutor.data.api.EnderecoResponse;
import com.petpass.tutor.data.api.RetrofitClient;
import com.petpass.tutor.data.api.ViaCepService;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etNomeCompleto, etCpf, etCep, etRua, etNumero, etBairro, etCidade;
    private Button btnFinalizarCompra;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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
        btnFinalizarCompra = findViewById(R.id.btnFinalizarCompra);

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
        if (userId == null) return;

        Map<String, Object> dadosCheckout = new HashMap<>();
        dadosCheckout.put("nomeCompleto", nome);
        dadosCheckout.put("cpf", cpf);
        dadosCheckout.put("cep", cep);
        dadosCheckout.put("enderecoCompleto", rua + ", " + numero + " - " + bairro + ", " + cidade);
        dadosCheckout.put("statusAssinatura", "ATIVO");

        db.collection("usuarios").document(userId).update(dadosCheckout)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(CheckoutActivity.this, "Assinatura ativada!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CheckoutActivity.this, MapaParceirosActivity.class);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Erro ao finalizar compra", Toast.LENGTH_SHORT).show());
    }
}
