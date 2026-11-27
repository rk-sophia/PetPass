package com.petpass.tutor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petpass.models.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CadastroTutorActivity extends AppCompatActivity {

    private EditText edtNome, edtEmail, edtSenha, edtEndereco;
    private Button btnCadastrar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_tutor);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtNome = findViewById(R.id.edtNomeCadastro);
        edtEmail = findViewById(R.id.edtEmailCadastro);
        edtSenha = findViewById(R.id.edtSenhaCadastro);
        edtEndereco = findViewById(R.id.edtEnderecoCadastro);
        btnCadastrar = findViewById(R.id.btnSalvarTutor);

        btnCadastrar.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nome = edtNome.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();
        String endereco = edtEndereco.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.length() < 6 || endereco.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos corretamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        user.sendEmailVerification();

                        Usuario novoUsuario = new Usuario(user.getUid(), nome, email, endereco, "TUTOR");
                        db.collection("usuarios").document(user.getUid())
                                .set(novoUsuario)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Cadastro salvo! Confirme o link no seu e-mail.", Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Erro ao salvar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Falha ao cadastrar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}