package com.petpass.tutor;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AgendamentoActivity extends AppCompatActivity {

    private Spinner spnServico, spnHorario;
    private Button btnData, btnConfirmar;
    private CheckBox chkAgenda;
    private Calendar dataSelecionada = Calendar.getInstance();
    private boolean dataEscolhida = false;

    private String petId, petNome, petshopId, petshopNome;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendamento);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        petId = getIntent().getStringExtra("PET_ID");
        petNome = getIntent().getStringExtra("PET_NOME");
        petshopId = getIntent().getStringExtra("PETSHOP_ID");
        petshopNome = getIntent().getStringExtra("PETSHOP_NOME");

        ArrayList<String> servicos = getIntent().getStringArrayListExtra("SERVICOS");
        ArrayList<String> horarios = getIntent().getStringArrayListExtra("HORARIOS");
        if (servicos == null || servicos.isEmpty()) {
            servicos = new ArrayList<>();
            servicos.add("Banho");
            servicos.add("Tosa");
        }
        if (horarios == null || horarios.isEmpty()) {
            horarios = new ArrayList<>();
            horarios.add("09:00");
            horarios.add("14:00");
        }

        TextView txtTitulo = findViewById(R.id.txtTituloLojaAgendamento);
        txtTitulo.setText("Agendar em " + petshopNome);

        spnServico = findViewById(R.id.spnServicoAgendamento);
        spnHorario = findViewById(R.id.spnHorarioAgendamento);
        btnData = findViewById(R.id.btnEscolherData);
        chkAgenda = findViewById(R.id.chkAdicionarAgenda);
        btnConfirmar = findViewById(R.id.btnConfirmarAgendamento);

        spnServico.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, servicos));
        spnHorario.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, horarios));

        btnData.setOnClickListener(v -> abrirSeletorData());
        btnConfirmar.setOnClickListener(v -> salvarAgendamento());
    }

    private void abrirSeletorData() {
        Calendar hoje = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            dataSelecionada.set(year, month, dayOfMonth);
            dataEscolhida = true;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            btnData.setText("Data: " + sdf.format(dataSelecionada.getTime()));
        }, hoje.get(Calendar.YEAR), hoje.get(Calendar.MONTH), hoje.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void salvarAgendamento() {
        if (!dataEscolhida) {
            Toast.makeText(this, "Selecione uma data primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }

        String servico = spnServico.getSelectedItem().toString();
        String horario = spnHorario.getSelectedItem().toString();

        String[] partesHora = horario.split(":");
        dataSelecionada.set(Calendar.HOUR_OF_DAY, Integer.parseInt(partesHora[0]));
        dataSelecionada.set(Calendar.MINUTE, Integer.parseInt(partesHora[1]));
        dataSelecionada.set(Calendar.SECOND, 0);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM 'às' HH:mm", Locale.getDefault());
        String dataTexto = sdf.format(dataSelecionada.getTime());
        long millis = dataSelecionada.getTimeInMillis();

        String agendamentoId = db.collection("agendamentos").document().getId();
        String codigoValidacao = "PET-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Map<String, Object> agendamento = new HashMap<>();
        agendamento.put("id", agendamentoId);
        agendamento.put("tutorUid", uid);
        agendamento.put("petId", petId);
        agendamento.put("petNome", petNome);
        agendamento.put("petshopId", petshopId);
        agendamento.put("petshopNome", petshopNome);
        agendamento.put("servico", servico);
        agendamento.put("dataHoraMillis", millis);
        agendamento.put("dataHoraTexto", dataTexto);
        agendamento.put("status", "AGENDADO");
        agendamento.put("codigoValidacao", codigoValidacao);

        db.collection("agendamentos").document(agendamentoId)
                .set(agendamento)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Agendamento confirmado!", Toast.LENGTH_SHORT).show();

                    if (chkAgenda.isChecked()) {
                        Intent calIntent = new Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, millis)
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, millis + 3600000)
                                .putExtra(CalendarContract.Events.TITLE, "PetPass: " + servico + " (" + petNome + ")")
                                .putExtra(CalendarContract.Events.EVENT_LOCATION, petshopNome);
                        startActivity(calIntent);
                    }

                    Intent home = new Intent(this, HomeActivity.class);
                    home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(home);
                    finish();
                });
    }
}