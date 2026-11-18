package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.petpass.tutor.data.api.*;
import com.petpass.tutor.util.QrCodeGenerator;

import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanosActivity extends AppCompatActivity {

    private Pet petSelecionado;
    private RadioGroup rgPlanos, rgPagamento;
    private LinearLayout layoutPix, layoutCartao;
    private ImageView imgQrPix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planos);

        petSelecionado = (Pet) getIntent().getSerializableExtra("PET_OBJ");

        TextView tvTitulo = findViewById(R.id.tvTituloPlanoPet);
        if (petSelecionado != null) {
            tvTitulo.setText("Escolha o Plano para " + petSelecionado.getNome());
        }

        rgPlanos = findViewById(R.id.rgPlanos);
        rgPagamento = findViewById(R.id.rgPagamento);
        layoutPix = findViewById(R.id.layoutPix);
        layoutCartao = findViewById(R.id.layoutCartao);
        imgQrPix = findViewById(R.id.imgQrPix);

        try {
            imgQrPix.setImageBitmap(QrCodeGenerator.generate("00020126360014BR.GOV.BCB.PIX0114PETPASS-ASSINA"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        rgPagamento.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isPix = checkedId == R.id.rbPix;
            layoutPix.setVisibility(isPix ? View.VISIBLE : View.GONE);
            layoutCartao.setVisibility(isPix ? View.GONE : View.VISIBLE);
        });

        findViewById(R.id.btnConfirmarAssinatura).setOnClickListener(v -> confirmarAssinatura());
    }

    private String getPlanoEscolhido() {
        int id = rgPlanos.getCheckedRadioButtonId();
        if (id == R.id.rbBronze) return "BRONZE";
        if (id == R.id.rbPrata) return "PRATA";
        if (id == R.id.rbDiamante) return "DIAMANTE";
        return "OURO";
    }

    private void confirmarAssinatura() {
        Map<String, String> payload = new HashMap<>();
        payload.put("pet_id", petSelecionado != null ? petSelecionado.getId() : "1");
        payload.put("plano", getPlanoEscolhido());
        payload.put("metodo_pagamento", rgPagamento.getCheckedRadioButtonId() == R.id.rbPix ? "PIX" : "CARTAO");

        ApiClient.getService().contratarPlano(payload).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PlanosActivity.this, "Plano ativado com sucesso!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(PlanosActivity.this, HomeDashboardActivity.class));
                    finish();
                } else {
                    Toast.makeText(PlanosActivity.this, "Erro no pagamento: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(PlanosActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}