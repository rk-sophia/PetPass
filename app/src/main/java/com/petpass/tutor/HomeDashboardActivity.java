package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.petpass.tutor.data.api.*;
import com.petpass.tutor.util.SessionManager;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeDashboardActivity extends AppCompatActivity {

    private SessionManager session;
    private TextView tvBoasVindas, tvResumoPet, tvGamificacaoCheckins, tvDetalhesAgendamento;
    private Pet primeiroPet;
    private Agendamento agendamentoAtivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_dashboard);

        session = new SessionManager(this);
        tvBoasVindas = findViewById(R.id.tvBoasVindas);
        tvResumoPet = findViewById(R.id.tvResumoPet);
        tvGamificacaoCheckins = findViewById(R.id.tvGamificacaoCheckins);
        tvDetalhesAgendamento = findViewById(R.id.tvDetalhesAgendamento);

        tvBoasVindas.setText("Olá, " + session.getTutorNome() + "!");

        findViewById(R.id.btnNovoPet).setOnClickListener(v ->
                startActivity(new Intent(this, CadastroPetActivity.class)));

        findViewById(R.id.btnBuscarParceiros).setOnClickListener(v -> {
            Intent intent = new Intent(this, MapaParceirosActivity.class);
            intent.putExtra("PET_OBJ", primeiroPet);
            startActivity(intent);
        });

        findViewById(R.id.btnAbrirCheckinQr).setOnClickListener(v -> {
            Intent intent = new Intent(this, QrCodeCheckinActivity.class);
            intent.putExtra("AGENDAMENTO_ID", agendamentoAtivo != null ? agendamentoAtivo.getId() : "AGD-001");
            startActivity(intent);
        });

        carregarDadosDashboard();
    }

    private void carregarDadosDashboard() {
        String tutorId = session.getTutorId();

        ApiClient.getService().listarPets(tutorId).enqueue(new Callback<List<Pet>>() {
            @Override
            public void onResponse(Call<List<Pet>> call, Response<List<Pet>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    primeiroPet = response.body().get(0);
                    tvResumoPet.setText(primeiroPet.getNome() + " • Plano " + primeiroPet.getPlanoAtivo());
                    tvGamificacaoCheckins.setText("🏆 " + primeiroPet.getNome() + " já fez " + primeiroPet.getTotalCheckins() + " check-ins!");
                } else {
                    tvResumoPet.setText("Nenhum pet cadastrado ainda.");
                }
            }
            @Override
            public void onFailure(Call<List<Pet>> call, Throwable t) {}
        });

        ApiClient.getService().listarAgendamentosAtivos(tutorId).enqueue(new Callback<List<Agendamento>>() {
            @Override
            public void onResponse(Call<List<Agendamento>> call, Response<List<Agendamento>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    agendamentoAtivo = response.body().get(0);
                    tvDetalhesAgendamento.setText(agendamentoAtivo.getPetshopNome() + "\n"
                            + agendamentoAtivo.getServico() + " • " + agendamentoAtivo.getDataHora());
                }
            }
            @Override
            public void onFailure(Call<List<Agendamento>> call, Throwable t) {}
        });
    }
}