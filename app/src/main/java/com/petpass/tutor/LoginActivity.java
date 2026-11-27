package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtSenha;
    private Button btnEntrar;
    private TextView txtCadastro;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtEmail = findViewById(R.id.edtEmailLogin);
        edtSenha = findViewById(R.id.edtSenhaLogin);
        btnEntrar = findViewById(R.id.btnEntrar);
        txtCadastro = findViewById(R.id.txtIrParaCadastro);

        // Persistência de Sessão: Se já estiver logado e verificado, redireciona direto
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            verificarPerfilERedirecionar(currentUser.getUid());
        }

        btnEntrar.setOnClickListener(v -> realizarLogin());
        txtCadastro.setOnClickListener(v -> startActivity(new Intent(this, CadastroTutorActivity.class)));
    }

    private void realizarLogin() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha e-mail e senha.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        // Fator 2 de Autenticação: Validação de posse do e-mail
                        user.reload().addOnSuccessListener(aVoid -> {
                            if (user.isEmailVerified()) {
                                verificarPerfilERedirecionar(user.getUid());
                            } else {
                                exibirDialogo2FA(user);
                            }
                        });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro no login: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void exibirDialogo2FA(FirebaseUser user) {
        new AlertDialog.Builder(this)
                .setTitle("Verificação em 2 Etapas (2FA)")
                .setMessage("Enviamos um link de confirmação para o e-mail " + user.getEmail() +
                        ". Clique no link recebido e depois toque em 'Já confirmei'.")
                .setCancelable(false)
                .setPositiveButton("Já confirmei", (dialog, which) -> {
                    user.reload().addOnSuccessListener(aVoid -> {
                        if (user.isEmailVerified()) {
                            verificarPerfilERedirecionar(user.getUid());
                        } else {
                            Toast.makeText(this, "E-mail ainda não confirmado!", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    });
                })
                .setNeutralButton("Reenviar E-mail", (dialog, which) -> {
                    user.sendEmailVerification();
                    Toast.makeText(this, "Novo e-mail enviado!", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> mAuth.signOut())
                .show();
    }

    private void verificarPerfilERedirecionar(String uid) {
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Boolean banido = doc.getBoolean("banido");
                    if (banido != null && banido) {
                        Toast.makeText(this, "Conta suspensa por violação dos termos.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        return;
                    }

                    String role = doc.getString("role");
                    if ("ADMIN".equals(role)) {
                        startActivity(new Intent(this, AdminActivity.class));
                        finish();
                    } else if ("GERENTE".equals(role)) {
                        startActivity(new Intent(this, GerentePetshopActivity.class));
                        finish();
                    } else {
                        // É TUTOR: verifica se já possui pelo menos 1 pet cadastrado
                        db.collection("usuarios").document(uid).collection("pets").limit(1).get()
                                .addOnSuccessListener(petsSnap -> {
                                    if (petsSnap.isEmpty()) {
                                        // Obriga cadastro do primeiro pet antes de navegar
                                        Intent i = new Intent(this, CadastroPetActivity.class);
                                        i.putExtra("PRIMEIRO_CADASTRO", true);
                                        startActivity(i);
                                    } else {
                                        startActivity(new Intent(this, HomeActivity.class));
                                    }
                                    finish();
                                });
                    }
                });
    }
}