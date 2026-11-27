package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.petpass.tutor.HomeActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PagamentoActivity extends AppCompatActivity {

    private EditText edtNome, edtNumero, edtValidade, edtCvv;
    private String petId, planoEscolhido, valorPlano;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagamento);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        petId = getIntent().getStringExtra("PET_ID");
        planoEscolhido = getIntent().getStringExtra("PLANO_ESCOLHIDO");
        valorPlano = getIntent().getStringExtra("VALOR_PLANO");

        TextView txtResumo = findViewById(R.id.txtResumoPlanoPagamento);
        txtResumo.setText("Assinatura: PLANO " + planoEscolhido + " (" + valorPlano + ")");

        edtNome = findViewById(R.id.edtNomeCartao);
        edtNumero = findViewById(R.id.edtNumeroCartao);
        edtValidade = findViewById(R.id.edtValidadeCartao);
        edtCvv = findViewById(R.id.edtCvvCartao);
        Button btnConfirmar = findViewById(R.id.btnConfirmarPagamento);

        btnConfirmar.setOnClickListener(v -> processarPagamentoSimulado());
    }

    private void processarPagamentoSimulado() {
        String nome = edtNome.getText().toString().trim();
        String numero = edtNumero.getText().toString().trim();
        String validade = edtValidade.getText().toString().trim();
        String cvv = edtCvv.getText().toString().trim();

        if (nome.isEmpty() || numero.length() < 12 || validade.isEmpty() || cvv.length() < 3) {
            Toast.makeText(this, "Preencha os dados do cartão corretamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tokenização: descarta o número real e gera apenas um token de transação
        String tokenSeguro = "tok_petpass_" + UUID.randomUUID().toString().substring(0, 12);

        Map<String, Object> atualizacao = new HashMap<>();
        atualizacao.put("planoAtivo", planoEscolhido);
        atualizacao.put("tokenPagamento", tokenSeguro);

        db.collection("usuarios").document(uid).collection("pets").document(petId)
                .update(atualizacao)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Pagamento aprovado! Plano " + planoEscolhido + " ativo.", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(this, HomeActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao ativar plano: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}