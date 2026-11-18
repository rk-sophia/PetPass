package com.petpass.tutor;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.petpass.tutor.data.api.*;
import com.petpass.tutor.util.SessionManager;

import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaParceirosActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Pet petAtual;
    private PetShop petShopSelecionado;
    private TextView tvNomePetShop;
    private Spinner spinnerServico, spinnerHorario, spinnerLembrete;
    private CheckBox cbAdicionarAgenda;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_parceiros);

        session = new SessionManager(this);
        petAtual = (Pet) getIntent().getSerializableExtra("PET_OBJ");

        tvNomePetShop = findViewById(R.id.tvNomePetShopSelecionado);
        spinnerServico = findViewById(R.id.spinnerServico);
        spinnerHorario = findViewById(R.id.spinnerHorario);
        spinnerLembrete = findViewById(R.id.spinnerLembrete);
        cbAdicionarAgenda = findViewById(R.id.cbAdicionarAgenda);

        spinnerServico.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Banho Completo", "Banho + Tosa Higiênica", "Tosa na Máquina / Tesoura"}));
        spinnerHorario.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Amanhã - 10:00", "Amanhã - 14:30", "Sábado - 09:00"}));
        spinnerLembrete.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Lembrete: 1 hora antes", "Lembrete: No dia (08:00)", "Lembrete: 10 minutos antes"}));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        findViewById(R.id.btnConfirmarAgendamento).setOnClickListener(v -> validarEAgendar());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.55052, -46.633308), 13f));
        carregarPetShopsDaApi();
    }

    private void carregarPetShopsDaApi() {
        ApiClient.getService().listarPetShops().enqueue(new Callback<List<PetShop>>() {
            @Override
            public void onResponse(Call<List<PetShop>> call, Response<List<PetShop>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                Map<Marker, PetShop> mapaMarcadores = new HashMap<>();
                for (PetShop ps : response.body()) {
                    Marker m = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(ps.getLatitude(), ps.getLongitude()))
                            .title(ps.getNome())
                            .snippet("Plano mínimo: " + ps.getCategoriaMinima()));
                    mapaMarcadores.put(m, ps);
                }

                mMap.setOnMarkerClickListener(marker -> {
                    petShopSelecionado = mapaMarcadores.get(marker);
                    if (petShopSelecionado != null) {
                        tvNomePetShop.setText(petShopSelecionado.getNome() + " (" + petShopSelecionado.getCategoriaMinima() + ")");
                    }
                    return false;
                });
            }

            @Override
            public void onFailure(Call<List<PetShop>> call, Throwable t) {
                Toast.makeText(MapaParceirosActivity.this, "Erro ao carregar mapa.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validarEAgendar() {
        if (petShopSelecionado == null) {
            Toast.makeText(this, "Toque em um marcador no mapa primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }

        int nivelPet = (petAtual != null) ? petAtual.getNivelPlano() : 1;
        if (nivelPet < petShopSelecionado.getNivelExigido()) {
            new AlertDialog.Builder(this)
                    .setTitle("Upgrade Necessário")
                    .setMessage("Este parceiro exige o plano " + petShopSelecionado.getCategoriaMinima()
                            + ". Deseja fazer upgrade do plano do seu pet agora?")
                    .setPositiveButton("Fazer Upgrade", (dialog, which) -> {
                        Intent intent = new Intent(this, PlanosActivity.class);
                        intent.putExtra("PET_OBJ", petAtual);
                        startActivity(intent);
                    })
                    .setNegativeButton("Escolher Outro", null)
                    .show();
            return;
        }

        String servico = spinnerServico.getSelectedItem().toString();
        String horario = spinnerHorario.getSelectedItem().toString();

        Agendamento novo = new Agendamento(
                session.getTutorId(),
                petAtual != null ? petAtual.getId() : "1",
                petShopSelecionado.getId(),
                servico,
                horario
        );

        ApiClient.getService().criarAgendamento(novo).enqueue(new Callback<Agendamento>() {
            @Override
            public void onResponse(Call<Agendamento> call, Response<Agendamento> response) {
                if (response.isSuccessful()) {
                    if (cbAdicionarAgenda.isChecked()) {
                        Intent calIntent = new Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.Events.TITLE, "PetPass: " + servico)
                                .putExtra(CalendarContract.Events.EVENT_LOCATION, petShopSelecionado.getNome());
                        startActivity(calIntent);
                    }
                    Toast.makeText(MapaParceirosActivity.this, "Agendamento confirmado!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MapaParceirosActivity.this, HomeDashboardActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Agendamento> call, Throwable t) {
                Toast.makeText(MapaParceirosActivity.this, "Erro ao agendar.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}