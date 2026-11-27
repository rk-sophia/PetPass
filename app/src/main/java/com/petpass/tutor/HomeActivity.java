package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.petpass.tutor.models.Pet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private Spinner spnSeletorPet;
    private TextView txtQtdCheckins, txtLocalizacaoAtual, txtVerTudo;
    private TextView txtDataAgendamento, txtNomePetshopAgendado, txtServicoAgendado;
    private EditText edtBuscaHome;
    private Button btnFiltroPlano, btnFiltroBanho, btnFiltroTosa, btnFiltroBrindes, btnFazerCheckin;
    private ImageButton btnSairApp;
    private CardView cardAgendamentoAtivo, cardBannerPrincipal;
    private LinearLayout layoutLocalizacao;
    private RecyclerView rvExtraServices;
    private BottomNavigationView bottomNavigation;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String uid;

    private List<Pet> listaPets = new ArrayList<>();
    private Pet petSelecionado;
    private String agendamentoAtivoId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        inicializarComponentes();
        configurarServicosExtras();
        carregarLocalizacaoTutor();
        carregarPetsDoTutor();
        configurarEventos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (petSelecionado != null) {
            atualizarDadosDoPet(petSelecionado);
        }
    }

    private void inicializarComponentes() {
        spnSeletorPet = findViewById(R.id.spnSeletorPetHome);
        txtQtdCheckins = findViewById(R.id.txtQtdCheckins);
        btnSairApp = findViewById(R.id.btnSairApp);

        edtBuscaHome = findViewById(R.id.edtBuscaHome);
        btnFiltroPlano = findViewById(R.id.btnFiltroPlano);
        btnFiltroBanho = findViewById(R.id.btnFiltroBanho);
        btnFiltroTosa = findViewById(R.id.btnFiltroTosa);
        btnFiltroBrindes = findViewById(R.id.btnFiltroBrindes);

        layoutLocalizacao = findViewById(R.id.layoutLocalizacao);
        txtLocalizacaoAtual = findViewById(R.id.txtLocalizacaoAtual);

        cardAgendamentoAtivo = findViewById(R.id.cardAgendamentoAtivo);
        txtDataAgendamento = findViewById(R.id.txtDataAgendamento);
        txtNomePetshopAgendado = findViewById(R.id.txtNomePetshopAgendado);
        txtServicoAgendado = findViewById(R.id.txtServicoAgendado);
        btnFazerCheckin = findViewById(R.id.btnFazerCheckin);

        cardBannerPrincipal = findViewById(R.id.cardBannerPrincipal);
        txtVerTudo = findViewById(R.id.txtVerTudo);
        rvExtraServices = findViewById(R.id.rvExtraServices);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void configurarServicosExtras() {
        List<String> servicos = Arrays.asList(
                "Banho Relaxante com Ozônio",
                "Tosa Higiênica & Hidratação",
                "Spa de Patinhas & Corte de Unhas",
                "Escovação de Pelos & Perfume Pet"
        );
        rvExtraServices.setLayoutManager(new LinearLayoutManager(this));
        rvExtraServices.setAdapter(new ServicoExtraAdapter(servicos, servico -> abrirBuscaPetshops(servico)));
    }

    private void carregarLocalizacaoTutor() {
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String endereco = doc.getString("endereco");
                        if (endereco != null && !endereco.isEmpty()) {
                            txtLocalizacaoAtual.setText(" " + endereco);
                        }
                    }
                });
    }

    private void carregarPetsDoTutor() {
        db.collection("usuarios").document(uid).collection("pets").get()
                .addOnSuccessListener(querySnapshot -> {
                    listaPets.clear();
                    List<String> nomesPets = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Pet pet = doc.toObject(Pet.class);
                        if (pet != null) {
                            listaPets.add(pet);
                            nomesPets.add(pet.getNome());
                        }
                    }

                    if (!listaPets.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                this, android.R.layout.simple_spinner_dropdown_item, nomesPets);
                        spnSeletorPet.setAdapter(adapter);
                    }
                });
    }

    private void atualizarDadosDoPet(Pet pet) {
        // Atualiza plano no botão de filtro e check-ins no topo
        String plano = pet.getPlanoAtivo() != null ? pet.getPlanoAtivo() : "NENHUM";
        btnFiltroPlano.setText("NENHUM".equals(plano) ? "Assinar Plano ▾" : plano + " ▾");
        txtQtdCheckins.setText("🐾 " + pet.getCheckinsRealizados() + " check-ins");

        // Busca próximo agendamento ativo no Firestore
        db.collection("agendamentos")
                .whereEqualTo("tutorUid", uid)
                .whereEqualTo("petId", pet.getPetId())
                .whereEqualTo("status", "AGENDADO")
                .orderBy("dataHoraMillis", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        DocumentSnapshot ag = snap.getDocuments().get(0);
                        agendamentoAtivoId = ag.getId();

                        cardAgendamentoAtivo.setVisibility(View.VISIBLE);
                        txtDataAgendamento.setText("PRÓXIMO • " + ag.getString("dataHoraTexto"));
                        txtNomePetshopAgendado.setText(ag.getString("petshopNome"));
                        txtServicoAgendado.setText(ag.getString("servico") + " • " + pet.getNome());

                        Long dataMillis = ag.getLong("dataHoraMillis");
                        if (dataMillis != null) {
                            long diff = dataMillis - System.currentTimeMillis();
                            if (diff > 0 && diff <= 7200000) {
                                Toast.makeText(this, "Lembrete: Seu agendamento está próximo!", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        agendamentoAtivoId = null;
                        cardAgendamentoAtivo.setVisibility(View.GONE);
                    }
                });
    }

    private void configurarEventos() {
        spnSeletorPet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                petSelecionado = listaPets.get(position);
                atualizarDadosDoPet(petSelecionado);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Busca ao apertar Enter/Lupa no teclado
        edtBuscaHome.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                abrirBuscaPetshops(edtBuscaHome.getText().toString().trim());
                return true;
            }
            return false;
        });

        // Botões de filtro
        btnFiltroPlano.setOnClickListener(v -> abrirTelaPlanos());
        btnFiltroBanho.setOnClickListener(v -> abrirBuscaPetshops("Banho"));
        btnFiltroTosa.setOnClickListener(v -> abrirBuscaPetshops("Tosa"));
        btnFiltroBrindes.setOnClickListener(v -> abrirBuscaPetshops("Brindes"));

        // Localização, Banner e Ver Tudo
        layoutLocalizacao.setOnClickListener(v -> abrirBuscaPetshops(""));
        cardBannerPrincipal.setOnClickListener(v -> abrirTelaPlanos());
        txtVerTudo.setOnClickListener(v -> abrirBuscaPetshops(""));

        // Check-in via QR Code
        btnFazerCheckin.setOnClickListener(v -> abrirLeitorQrCode());

        // Sair (Logout)
        btnSairApp.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Menu Inferior
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_explorar) {
                abrirBuscaPetshops("");
                return true;
            } else if (id == R.id.nav_checkin) {
                abrirLeitorQrCode();
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, PerfilTutorActivity.class));
                return true;
            }
            return true;
        });
    }

    private void abrirBuscaPetshops(String filtroTexto) {
        if (petSelecionado == null) return;
        Intent i = new Intent(this, BuscaPetshopsActivity.class);
        i.putExtra("PET_ID", petSelecionado.getPetId());
        i.putExtra("PET_NOME", petSelecionado.getNome());
        i.putExtra("PLANO_ATIVO", petSelecionado.getPlanoAtivo());
        i.putExtra("FILTRO", filtroTexto);
        startActivity(i);
    }

    private void abrirTelaPlanos() {
        if (petSelecionado == null) return;
        Intent i = new Intent(this, PlanosActivity.class);
        i.putExtra("PET_ID", petSelecionado.getPetId());
        startActivity(i);
    }

    private void abrirLeitorQrCode() {
        if (agendamentoAtivoId != null && petSelecionado != null) {
            Intent i = new Intent(this, LeitorQrCheckinActivity.class);
            i.putExtra("AGENDAMENTO_ID", agendamentoAtivoId);
            i.putExtra("PET_ID", petSelecionado.getPetId());
            startActivity(i);
        } else {
            Toast.makeText(this, "Você não possui agendamento ativo para check-in.", Toast.LENGTH_SHORT).show();
        }
    }
}