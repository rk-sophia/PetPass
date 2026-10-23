package com.petpass.tutor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BeneficiosAdapter extends RecyclerView.Adapter<BeneficiosAdapter.ViewHolder> {

    private List<String> beneficios;

    public BeneficiosAdapter(List<String> beneficios) {
        this.beneficios = beneficios;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_beneficio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvBeneficio.setText(beneficios.get(position));
    }

    @Override
    public int getItemCount() {
        return beneficios.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBeneficio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBeneficio = itemView.findViewById(R.id.tvBeneficio);
        }
    }
}
