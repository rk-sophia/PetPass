package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminActivity extends AppCompatActivity {

    private TextView txtRelatorio;
    private EditText edtEmailBanir;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();
        txtRelatorio = findViewById(R.id.txtRelatorioAdmin);
        edtEmailBanir = findViewById(R.id.edtEmailBanir);

        carregarRelatorioGeral();

        findViewById(R.id.btnBanirUsuario).setOnClickListener(v -> banirUsuario());
        findViewById(R.id.btnSairAdmin).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void carregarRelatorioGeral() {
        db.collection("usuarios").get().addOnSuccessListener(users -> {
            db.collection("petshops").get().addOnSuccessListener(shops -> {
                db.collection("agendamentos").get().addOnSuccessListener(ags -> {
                    txtRelatorio.setText(
                            "• Usuários cadastrados: " + users.size() + "\n" +
                                    "• Pet Shops parceiros: " + shops.size() + "\n" +
                                    "• Agendamentos registrados: " + ags.size()
                    );
                });
            });
        });
    }

    private void banirUsuario() {
        String email = edtEmailBanir.getText().toString().trim();
        if (email.isEmpty()) return;

        db.collection("usuarios").whereEqualTo("email", email).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Toast.makeText(this, "Usuário não encontrado.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (DocumentSnapshot doc : snap) {
                        doc.getReference().update("banido", true);
                    }
                    Toast.makeText(this, "Usuário banido com sucesso!", Toast.LENGTH_SHORT).show();
                    edtEmailBanir.setText("");
                });
    }
}