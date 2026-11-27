package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PlanosActivity extends AppCompatActivity {

    private String petId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planos);

        petId = getIntent().getStringExtra("PET_ID");

        Button btnSilver = findViewById(R.id.btnAssinarSilver);
        Button btnGold = findViewById(R.id.btnAssinarGold);
        Button btnPlatinum = findViewById(R.id.btnAssinarPlatinum);

        btnSilver.setOnClickListener(v -> irParaPagamento("SILVER", "R$ 59,90"));
        btnGold.setOnClickListener(v -> irParaPagamento("GOLD", "R$ 99,90"));
        btnPlatinum.setOnClickListener(v -> irParaPagamento("PLATINUM", "R$ 159,90"));
    }

    private void irParaPagamento(String plano, String valor) {
        Intent i = new Intent(this, PagamentoActivity.class);
        i.putExtra("PET_ID", petId);
        i.putExtra("PLANO_ESCOLHIDO", plano);
        i.putExtra("VALOR_PLANO", valor);
        startActivity(i);
        finish();
    }
}