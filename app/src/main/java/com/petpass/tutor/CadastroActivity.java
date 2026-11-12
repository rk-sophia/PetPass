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

public class CadastroActivity extends AppCompatActivity {

    private EditText etEmail, etSenha, etConfirmarSenha;
    private Button btnRegistrar;
    private TextView tvVoltarLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmailCadastro);
        etSenha = findViewById(R.id.etSenhaCadastro);
        etConfirmarSenha = findViewById(R.id.etConfirmarSenha);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        tvVoltarLogin = findViewById(R.id.tvVoltarLogin);

        btnRegistrar.setOnClickListener(v -> cadastrarUsuario());

        tvVoltarLogin.setOnClickListener(v -> finish());
    }

    private void cadastrarUsuario() {
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();
        String confirmarSenha = etConfirmarSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Informe o e-mail");
            return;
        }

        if (TextUtils.isEmpty(senha)) {
            etSenha.setError("Informe a senha");
            return;
        }

        if (senha.length() < 6) {
            etSenha.setError("A senha deve ter pelo menos 6 caracteres");
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            etConfirmarSenha.setError("As senhas não coincidem");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CadastroActivity.this, PerfilPetActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CadastroActivity.this, "Erro ao cadastrar: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
}
