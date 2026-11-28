package com.petpass.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.petpass.tutor.models.Pet;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TextView txtSaudacao, txtBadgePlano, txtStatusAgendamento, txtNomePetshop, txtDetalhesServico, txtQtdCheckins, txtNivelRanking;
    private Spinner spnSeletorPet;
    private Button btnAcaoPrincipal;
    private ImageButton btnSair;
    private LinearLayout btnBuscaPetshops, bannerUpgrade, cardCatBanho, cardCatTosa, cardCatBrindes;
    private BottomNavigationView bottomNav;

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
        carregarDadosTutor();
        carregarPetsDoTutor();
        configurarEventos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (petSelecionado != null) {
            atualizarDashboardDoPet(petSelecionado);
        }
    }

    private void inicializarComponentes() {
        txtSaudacao = findViewById(R.id.txtSaudacaoTutor);
        txtBadgePlano = findViewById(R.id.txtBadgePlano);
        spnSeletorPet = findViewById(R.id.spnSeletorPetHome);
        txtStatusAgendamento = findViewById(R.id.txtStatusAgendamento);
        txtNomePetshop = findViewById(R.id.txtNomePetshopAgendado);
        txtDetalhesServico = findViewById(R.id.txtDetalhesServicoAgendado);
        btnAcaoPrincipal = findViewById(R.id.btnAcaoPrincipalCard);
        txtQtdCheckins = findViewById(R.id.txtQtdCheckins);
        txtNivelRanking = findViewById(R.id.txtNivelRankingPet);
        btnSair = findViewById(R.id.btnSairApp);
        btnBuscaPetshops = findViewById(R.id.btnBarraBuscaPetshops);
        bannerUpgrade = findViewById(R.id.bannerUpgradePlano);
        cardCatBanho = findViewById(R.id.cardCatBanho);
        cardCatTosa = findViewById(R.id.cardCatTosa);
        cardCatBrindes = findViewById(R.id.cardCatBrindes);
        bottomNav = findViewById(R.id.bottomNavigation);
    }

    private void carregarDadosTutor() {
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String nome = doc.getString("nomeCompleto");
                        if (nome != null && !nome.isEmpty()) {
                            String primeiroNome = nome.split(" ")[0];
                            txtSaudacao.setText("Olá, " + primeiroNome + " 👋");
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
                        Pet p = doc.toObject(Pet.class);
                        if (p != null) {
                            listaPets.add(p);
                            nomesPets.add("🐾 " + p.getNome());
                        }
                    }

                    if (!listaPets.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                this, android.R.layout.simple_spinner_dropdown_item, nomesPets);
                        spnSeletorPet.setAdapter(adapter);
                    }
                });
    }

    private void atualizarDashboardDoPet(Pet pet) {
        String plano = pet.getPlanoAtivo() != null ? pet.getPlanoAtivo() : "NENHUM";
        txtBadgePlano.setText("NENHUM".equals(plano) ? "SEM PLANO" : "PLANO " + plano);

        int checkins = pet.getCheckinsRealizados();
        txtQtdCheckins.setText(String.valueOf(checkins));
        if (checkins >= 10) {
            txtNivelRanking.setText("Nível: Pet VIP Ouro 🏆");
        } else if (checkins >= 4) {
            txtNivelRanking.setText("Nível: Pet Frequente ⭐");
        } else {
            txtNivelRanking.setText("Nível: Pet Iniciante 🐾");
        }

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
                        String shopNome = ag.getString("petshopNome");
                        String servico = ag.getString("servico");
                        String dataTexto = ag.getString("dataHoraTexto");
                        Long dataMillis = ag.getLong("dataHoraMillis");

                        txtNomePetshop.setText(shopNome);
                        txtDetalhesServico.setText(servico + " • " + pet.getNome());
                        txtStatusAgendamento.setText(dataTexto);
                        btnAcaoPrincipal.setText("FAZER CHECK-IN (LER QR CODE)");

                        if (dataMillis != null) {
                            long diff = dataMillis - System.currentTimeMillis();
                            if (diff > 0 && diff <= 7200000) {
                                Toast.makeText(this, "Lembrete: Horário próximo em " + shopNome + "!", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        agendamentoAtivoId = null;
                        txtStatusAgendamento.setText("Sem reserva");
                        txtNomePetshop.setText("Nenhum horário agendado");
                        txtDetalhesServico.setText("Toque em buscar para agendar o cuidado do seu pet");
                        btnAcaoPrincipal.setText("AGENDAR SERVIÇO AGORA");
                    }
                });
    }

    private void configurarEventos() {
        spnSeletorPet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                petSelecionado = listaPets.get(position);
                atualizarDashboardDoPet(petSelecionado);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnAcaoPrincipal.setOnClickListener(v -> {
            if (petSelecionado == null) return;
            if (agendamentoAtivoId != null) {
                Intent i = new Intent(this, LeitorQrCheckinActivity.class);
                i.putExtra("AGENDAMENTO_ID", agendamentoAtivoId);
                i.putExtra("PET_ID", petSelecionado.getPetId());
                startActivity(i);
            } else {
                abrirBuscaPetshops();
            }
        });

        btnBuscaPetshops.setOnClickListener(v -> abrirBuscaPetshops());
        cardCatBanho.setOnClickListener(v -> abrirBuscaPetshops());
        cardCatTosa.setOnClickListener(v -> abrirBuscaPetshops());
        cardCatBrindes.setOnClickListener(v -> abrirBuscaPetshops());

        txtBadgePlano.setOnClickListener(v -> abrirTelaPlanos());
        bannerUpgrade.setOnClickListener(v -> abrirTelaPlanos());

        btnSair.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_explorar) {
                abrirBuscaPetshops();
                return true;
            } else if (id == R.id.nav_checkin) {
                if (agendamentoAtivoId != null) {
                    Intent i = new Intent(this, LeitorQrCheckinActivity.class);
                    i.putExtra("AGENDAMENTO_ID", agendamentoAtivoId);
                    i.putExtra("PET_ID", petSelecionado.getPetId());
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Você precisa ter um agendamento ativo para fazer check-in.", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, PerfilTutorActivity.class));
                return true;
            }
            return true;
        });
    }

    private void abrirBuscaPetshops() {
        if (petSelecionado == null) return;
        Intent i = new Intent(this, BuscaPetshopsActivity.class);
        i.putExtra("PET_ID", petSelecionado.getPetId());
        i.putExtra("PET_NOME", petSelecionado.getNome());
        i.putExtra("PLANO_ATIVO", petSelecionado.getPlanoAtivo());
        startActivity(i);
    }

    private void abrirTelaPlanos() {
        if (petSelecionado == null) return;
        Intent i = new Intent(this, PlanosActivity.class);
        i.putExtra("PET_ID", petSelecionado.getPetId());
        startActivity(i);
    }
}