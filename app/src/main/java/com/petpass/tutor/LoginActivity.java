package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etSenha;
    private Button btnLogin;
    private TextView tvEsqueceuSenha, tvCriarCadastro;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        btnLogin = findViewById(R.id.btnLogin);
        tvEsqueceuSenha = findViewById(R.id.tvEsqueceuSenha);
        tvCriarCadastro = findViewById(R.id.tvCriarCadastro);

        btnLogin.setOnClickListener(v -> loginUsuario());

        tvCriarCadastro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        tvEsqueceuSenha.setOnClickListener(v -> resetarSenha());
    }

    private void loginUsuario() {
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Informe o e-mail");
            return;
        }

        if (TextUtils.isEmpty(senha)) {
            etSenha.setError("Informe a senha");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(LoginActivity.this, PlanosActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Erro ao entrar: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    private void resetarSenha() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Informe seu e-mail para recuperar a senha");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "E-mail de recuperação enviado!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Erro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
}
