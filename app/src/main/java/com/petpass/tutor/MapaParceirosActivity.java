package com.petpass.tutor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.petpass.tutor.data.api.ApiClient;
import com.petpass.tutor.data.api.PetShop;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaParceirosActivity extends AppCompatActivity {

    private enum Estado { CARREGANDO, CONTEUDO, VAZIO, ERRO }

    private ProgressBar progressBar;
    private View layoutConteudo, layoutErro;
    private TextView txtVazio, txtMensagemErro;
    private Button btnTentarNovamente;
    
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_parceiros);

        progressBar = findViewById(R.id.progressBar);
        layoutConteudo = findViewById(R.id.layoutConteudo);
        txtVazio = findViewById(R.id.txtVazio);
        layoutErro = findViewById(R.id.layoutErro);
        txtMensagemErro = findViewById(R.id.txtMensagemErro);
        btnTentarNovamente = findViewById(R.id.btnTentarNovamente);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnTentarNovamente.setOnClickListener(v -> verificarPermissaoELocalizar());

        verificarPermissaoELocalizar();
    }

    private void verificarPermissaoELocalizar() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mostrarEstado(Estado.CARREGANDO);
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    buscarPetShops(location.getLatitude(), location.getLongitude());
                } else {
                    // Se não conseguir a localização, usa uma padrão (ex: SP) ou avisa o usuário
                    buscarPetShops(-23.55052, -46.633308);
                    Toast.makeText(this, "Não foi possível obter localização exata. Usando padrão.", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                txtMensagemErro.setText("Erro ao obter localização: " + e.getMessage());
                mostrarEstado(Estado.ERRO);
            });
    }

    private void buscarPetShops(double lat, double lng) {
        Call<List<PetShop>> chamada = ApiClient.getService().listarPetShopsProximos(lat, lng);

        chamada.enqueue(new Callback<List<PetShop>>() {
            @Override
            public void onResponse(Call<List<PetShop>> call, Response<List<PetShop>> response) {
                if (!response.isSuccessful()) {
                    txtMensagemErro.setText("Erro do servidor: " + response.code());
                    mostrarEstado(Estado.ERRO);
                    return;
                }

                List<PetShop> lista = response.body();
                if (lista == null || lista.isEmpty()) {
                    mostrarEstado(Estado.VAZIO);
                    return;
                }

                mostrarEstado(Estado.CONTEUDO);
                // Aqui você integraria com o Google Maps para mostrar os markers
            }

            @Override
            public void onFailure(Call<List<PetShop>> call, Throwable t) {
                txtMensagemErro.setText("Falha de conexão: " + t.getMessage());
                mostrarEstado(Estado.ERRO);
            }
        });
    }

    private void mostrarEstado(Estado estado) {
        progressBar.setVisibility(estado == Estado.CARREGANDO ? View.VISIBLE : View.GONE);
        layoutConteudo.setVisibility(estado == Estado.CONTEUDO ? View.VISIBLE : View.GONE);
        txtVazio.setVisibility(estado == Estado.VAZIO ? View.VISIBLE : View.GONE);
        layoutErro.setVisibility(estado == Estado.ERRO ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                verificarPermissaoELocalizar();
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_LONG).show();
                mostrarEstado(Estado.ERRO);
                txtMensagemErro.setText("O app precisa da localização para buscar petshops próximos.");
            }
        }
    }
}
