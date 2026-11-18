package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.petpass.tutor.data.api.*;
import com.petpass.tutor.util.SessionManager;

import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private enum EtapaAuth { LOGIN, CADASTRO, VERIFICAR_TELEFONE, VERIFICAR_EMAIL }

    private EtapaAuth etapaAtual = EtapaAuth.LOGIN;
    private LinearLayout blocoFormularioPrincipal, layoutCamposCadastro, blocoVerificarTelefone, blocoVerificarEmail;
    private EditText etNome, etTelefone, etCep, etEndereco, etEmail, etSenha, etCodigoSms, etCodigoEmail;
    private Button btnAcaoAuth, btnValidarTelefone, btnValidarEmail;
    private TextView tvAlternarModo, tvSubtituloAuth;
    private ProgressBar progressAuth;
    private SessionManager session;

    private String telefonePendente = "";
    private String emailPendente = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);
        if (session.estaLogado()) {
            irParaHome();
            return;
        }

        setContentView(R.layout.activity_login);

        blocoFormularioPrincipal = findViewById(R.id.blocoFormularioPrincipal);
        layoutCamposCadastro = findViewById(R.id.layoutCamposCadastro);
        blocoVerificarTelefone = findViewById(R.id.blocoVerificarTelefone);
        blocoVerificarEmail = findViewById(R.id.blocoVerificarEmail);

        etNome = findViewById(R.id.etNome);
        etTelefone = findViewById(R.id.etTelefone);
        etCep = findViewById(R.id.etCep);
        etEndereco = findViewById(R.id.etEndereco);
        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        etCodigoSms = findViewById(R.id.etCodigoSms);
        etCodigoEmail = findViewById(R.id.etCodigoEmail);

        btnAcaoAuth = findViewById(R.id.btnAcaoAuth);
        btnValidarTelefone = findViewById(R.id.btnValidarTelefone);
        btnValidarEmail = findViewById(R.id.btnValidarEmail);

        tvAlternarModo = findViewById(R.id.tvAlternarModo);
        tvSubtituloAuth = findViewById(R.id.tvSubtituloAuth);
        progressAuth = findViewById(R.id.progressAuth);

        tvAlternarModo.setOnClickListener(v ->
                atualizarEtapa(etapaAtual == EtapaAuth.LOGIN ? EtapaAuth.CADASTRO : EtapaAuth.LOGIN));

        btnAcaoAuth.setOnClickListener(v -> {
            if (etapaAtual == EtapaAuth.LOGIN) executarLogin();
            else iniciarCadastroComSms();
        });

        btnValidarTelefone.setOnClickListener(v -> confirmarCodigoTelefone());
        btnValidarEmail.setOnClickListener(v -> confirmarCodigoEmail());
    }

    private void atualizarEtapa(EtapaAuth novaEtapa) {
        etapaAtual = novaEtapa;

        blocoFormularioPrincipal.setVisibility(
                (novaEtapa == EtapaAuth.LOGIN || novaEtapa == EtapaAuth.CADASTRO) ? View.VISIBLE : View.GONE);
        layoutCamposCadastro.setVisibility(novaEtapa == EtapaAuth.CADASTRO ? View.VISIBLE : View.GONE);
        blocoVerificarTelefone.setVisibility(novaEtapa == EtapaAuth.VERIFICAR_TELEFONE ? View.VISIBLE : View.GONE);
        blocoVerificarEmail.setVisibility(novaEtapa == EtapaAuth.VERIFICAR_EMAIL ? View.VISIBLE : View.GONE);

        if (novaEtapa == EtapaAuth.LOGIN) {
            btnAcaoAuth.setText("Entrar");
            tvSubtituloAuth.setText("Entre na sua conta de tutor");
            tvAlternarModo.setText("Não tem conta? Cadastre-se agora");
        } else if (novaEtapa == EtapaAuth.CADASTRO) {
            btnAcaoAuth.setText("Continuar para Verificação");
            tvSubtituloAuth.setText("Etapa 1 de 3: Dados do Tutor");
            tvAlternarModo.setText("Já possui conta? Faça Login");
        } else if (novaEtapa == EtapaAuth.VERIFICAR_TELEFONE) {
            tvSubtituloAuth.setText("Etapa 2 de 3: Autenticação de Telefone");
        } else if (novaEtapa == EtapaAuth.VERIFICAR_EMAIL) {
            tvSubtituloAuth.setText("Etapa 3 de 3: Autenticação de E-mail");
        }
    }

    private void executarLogin() {
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Informe e-mail e senha.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressAuth.setVisibility(View.VISIBLE);
        ApiClient.getService().login(new AuthRequest(email, senha)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                progressAuth.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    session.salvarSessao(auth.getToken(), auth.getTutorId(), auth.getNome());
                    irParaHome();
                } else {
                    Toast.makeText(LoginActivity.this, "Credenciais inválidas (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                progressAuth.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarCadastroComSms() {
        String nome = etNome.getText().toString().trim();
        telefonePendente = etTelefone.getText().toString().trim();
        emailPendente = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();
        String cep = etCep.getText().toString().trim();
        String endereco = etEndereco.getText().toString().trim();

        if (nome.isEmpty() || telefonePendente.isEmpty() || emailPendente.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressAuth.setVisibility(View.VISIBLE);
        AuthRequest req = new AuthRequest(nome, telefonePendente, emailPendente, senha, cep, endereco);

        ApiClient.getService().iniciarCadastro(req).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                progressAuth.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "SMS enviado para " + telefonePendente, Toast.LENGTH_SHORT).show();
                    atualizarEtapa(EtapaAuth.VERIFICAR_TELEFONE);
                } else {
                    Toast.makeText(LoginActivity.this, "Erro ao iniciar cadastro: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                progressAuth.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarCodigoTelefone() {
        String codigoSms = etCodigoSms.getText().toString().trim();
        if (codigoSms.length() < 4) {
            Toast.makeText(this, "Digite o código recebido por SMS.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressAuth.setVisibility(View.VISIBLE);
        Map<String, String> payload = new HashMap<>();
        payload.put("telefone", telefonePendente);
        payload.put("email", emailPendente);
        payload.put("codigo_sms", codigoSms);

        ApiClient.getService().verificarTelefone(payload).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                progressAuth.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Telefone autenticado! Código enviado para " + emailPendente, Toast.LENGTH_LONG).show();
                    atualizarEtapa(EtapaAuth.VERIFICAR_EMAIL);
                } else {
                    Toast.makeText(LoginActivity.this, "Código SMS inválido.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                progressAuth.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarCodigoEmail() {
        String codigoEmail = etCodigoEmail.getText().toString().trim();
        if (codigoEmail.length() < 4) {
            Toast.makeText(this, "Digite o código recebido por e-mail.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressAuth.setVisibility(View.VISIBLE);
        Map<String, String> payload = new HashMap<>();
        payload.put("email", emailPendente);
        payload.put("codigo_email", codigoEmail);

        ApiClient.getService().verificarEmailFinalizar(payload).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                progressAuth.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    session.salvarSessao(auth.getToken(), auth.getTutorId(), auth.getNome());
                    Toast.makeText(LoginActivity.this, "Cadastro concluído com sucesso!", Toast.LENGTH_SHORT).show();
                    irParaHome();
                } else {
                    Toast.makeText(LoginActivity.this, "Código de e-mail inválido.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                progressAuth.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irParaHome() {
        startActivity(new Intent(this, HomeDashboardActivity.class));
        finish();
    }
}