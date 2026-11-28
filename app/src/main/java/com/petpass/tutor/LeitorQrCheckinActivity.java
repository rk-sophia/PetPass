package com.petpass.tutor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.HashMap;
import java.util.Map;

public class LeitorQrCheckinActivity extends AppCompatActivity {

    private String agendamentoId, petId, uid;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    validarCheckinNoBanco(result.getContents());
                } else {
                    Toast.makeText(this, "Leitura cancelada.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leitor_qr);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        agendamentoId = getIntent().getStringExtra("AGENDAMENTO_ID");
        petId = getIntent().getStringExtra("PET_ID");

        Button btnCamera = findViewById(R.id.btnAbrirCameraQr);
        EditText edtManual = findViewById(R.id.edtCodigoManualQr);
        Button btnManual = findViewById(R.id.btnValidarCodigoManual);

        btnCamera.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Escaneie o QR Code do Pet Shop");
            options.setBeepEnabled(true);
            options.setOrientationLocked(false);
            barcodeLauncher.launch(options);
        });

        btnManual.setOnClickListener(v -> {
            String codigo = edtManual.getText().toString().trim();
            if (!codigo.isEmpty()) {
                validarCheckinNoBanco(codigo);
            }
        });
    }

    private void validarCheckinNoBanco(String conteudoQrLido) {
        Map<String, Object> dadosCheckin = new HashMap<>();
        dadosCheckin.put("status", "CONCLUIDO");
        dadosCheckin.put("qrValidado", conteudoQrLido);
        dadosCheckin.put("checkinTimestamp", System.currentTimeMillis());

        // 1. Atualiza o agendamento para o Painel Web do Vini liberar o serviço
        db.collection("agendamentos").document(agendamentoId)
                .update(dadosCheckin)
                .addOnSuccessListener(aVoid -> {
                    // 2. Incrementa +1 no ranking de check-ins do Pet
                    db.collection("usuarios").document(uid).collection("pets").document(petId)
                            .update("checkinsRealizados", FieldValue.increment(1))
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Check-in concluído! Serviço liberado.", Toast.LENGTH_LONG).show();
                                finish();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao validar QR Code: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}