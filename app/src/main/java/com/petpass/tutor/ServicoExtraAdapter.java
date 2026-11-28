package com.petpass.tutor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ServicoExtraAdapter extends RecyclerView.Adapter<ServicoExtraAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String servico);
    }

    private final List<String> servicos;
    private final OnItemClickListener listener;

    public ServicoExtraAdapter(List<String> servicos, OnItemClickListener listener) {
        this.servicos = servicos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String servico = servicos.get(position);
        holder.txtTitulo.setText("✨  " + servico);
        holder.txtTitulo.setTextColor(0xFF333333);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(servico));
    }

    @Override
    public int getItemCount() {
        return servicos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo;
        ViewHolder(View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(android.R.id.text1);
        }
    }
}