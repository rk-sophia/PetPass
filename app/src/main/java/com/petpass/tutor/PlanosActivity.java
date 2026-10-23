package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;

public class PlanosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planos);

        Button btnPremium = findViewById(R.id.btnAssinarPremium);
        Button btnGold = findViewById(R.id.btnAssinarGold);
        Button btnPlatina = findViewById(R.id.btnAssinarPlatina);

        btnPremium.setOnClickListener(v -> abrirPerfilPet("Plano Premium", new ArrayList<>(Arrays.asList(
                "4 Banhos por mês",
                "1 Tosa higiênica incluída"
        ))));

        btnGold.setOnClickListener(v -> abrirPerfilPet("Plano Gold", new ArrayList<>(Arrays.asList(
                "6 Banhos por mês",
                "2 Tosas completas",
                "Hidratação inclusa"
        ))));

        btnPlatina.setOnClickListener(v -> abrirPerfilPet("Plano Platina", new ArrayList<>(Arrays.asList(
                "Banhos ilimitados",
                "Tosas ilimitadas",
                "Leva e Traz incluso"
        ))));
    }

    private void abrirPerfilPet(String nomePlano, ArrayList<String> beneficios) {
        Intent intent = new Intent(PlanosActivity.this, PerfilPetActivity.class);
        intent.putExtra("NOME_PLANO", nomePlano);
        intent.putStringArrayListExtra("BENEFICIOS", beneficios);
        startActivity(intent);
    }
}
