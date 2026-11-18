package com.petpass.tutor;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.petpass.tutor.data.api.ApiClient;
import com.petpass.tutor.util.QrCodeGenerator;

import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrCodeCheckinActivity extends AppCompatActivity {

    private ImageView imgQrCode;
    private TextView tvTimer;
    private String agendamentoId;
    private final Handler handler = new Handler();
    private int segundos = 30;

    private final Runnable contadorTotp = new Runnable() {
        @Override
        public void run() {
            if (segundos > 0) {
                segundos--;
            } else {
                segundos = 30;
                solicitarTokenTotpApi();
            }
            tvTimer.setText("Código expira em: " + segundos + "s");
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_checkin);

        imgQrCode = findViewById(R.id.imgQrCode);
        tvTimer = findViewById(R.id.tvTimer);
        agendamentoId = getIntent().getStringExtra("AGENDAMENTO_ID");

        solicitarTokenTotpApi();
        handler.post(contadorTotp);
    }

    private void solicitarTokenTotpApi() {
        ApiClient.getService().obterTokenTotp(agendamentoId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                String payloadQr;
                if (response.isSuccessful() && response.body() != null) {
                    payloadQr = response.body().get("totp_token");
                } else {
                    payloadQr = "PETPASS|" + agendamentoId + "|" + (System.currentTimeMillis() / 30000);
                }
                renderizarQrCode(payloadQr);
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                String fallback = "PETPASS|" + agendamentoId + "|" + (System.currentTimeMillis() / 30000);
                renderizarQrCode(fallback);
            }
        });
    }

    private void renderizarQrCode(String conteudo) {
        try {
            Bitmap bmp = QrCodeGenerator.generate(conteudo);
            imgQrCode.setImageBitmap(bmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(contadorTotp);
    }
}