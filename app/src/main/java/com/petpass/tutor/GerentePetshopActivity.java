package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class GerentePetshopActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerente_petshop);

        TextView txtLista = findViewById(R.id.txtAgendamentosGerente);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("agendamentos").get().addOnSuccessListener(snap -> {
            StringBuilder sb = new StringBuilder();
            for (DocumentSnapshot doc : snap) {
                sb.append("Pet: ").append(doc.getString("petNome"))
                        .append(" | Serviço: ").append(doc.getString("servico"))
                        .append("\nData: ").append(doc.getString("dataHoraTexto"))
                        .append(" | Status: ").append(doc.getString("status"))
                        .append("\n\n");
            }
            txtLista.setText(sb.length() > 0 ? sb.toString() : "Nenhum atendimento registrado.");
        });

        findViewById(R.id.btnSairGerente).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}