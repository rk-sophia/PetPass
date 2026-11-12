package com.petpass.tutor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class PerfilDashboardFragment extends Fragment {

    private TextView tvPlanoBadge, tvCheckinCount;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil_dashboard, container, false);

        tvPlanoBadge = view.findViewById(R.id.tvPlanoBadge);
        tvCheckinCount = view.findViewById(R.id.tvCheckinCount);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        carregarDadosPerfil();

        return view;
    }

    private void carregarDadosPerfil() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId != null) {
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("statusAssinatura");
                        Long qtdPets = documentSnapshot.getLong("quantidadePets");
                        
                        if ("ATIVO".equals(status) && qtdPets != null) {
                            tvPlanoBadge.setText(qtdPets + " Pet(s) Assinado(s)");
                        } else {
                            tvPlanoBadge.setText("Plano Ativo");
                        }
                        
                        tvCheckinCount.setText("0"); 
                    }
                });
        }
    }
}
