package com.example.appmanutencao.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmanutencao.R;
import com.example.appmanutencao.model.OrdemServico;

import java.util.List;

public class AtendimentoClienteAdapter extends RecyclerView.Adapter<AtendimentoClienteAdapter.VH> {

    public interface OnChatClick {
        void onChat(OrdemServico ordem);
    }

    private final List<OrdemServico> lista;
    private final OnChatClick listener;

    public AtendimentoClienteAdapter(List<OrdemServico> lista, OnChatClick listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_atendimento_cliente, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        OrdemServico o = lista.get(position);

        h.txtTitulo.setText("Atendimento #" + (o.getId() != null ? o.getId() : "-"));

        String servico = "Servico nao informado";
        if (o.getServico() != null && o.getServico().getTitulo() != null) {
            servico = o.getServico().getTitulo();
        }
        h.txtServico.setText(servico);

        String tipo = o.getTipoAtendimento();
        if (tipo != null && !tipo.trim().isEmpty()) {
            h.txtTipo.setText("Tipo: " + tipo);
            h.txtTipo.setVisibility(View.VISIBLE);
        } else {
            h.txtTipo.setVisibility(View.GONE);
        }

        String status = o.getStatus() != null ? o.getStatus() : "SEM STATUS";
        h.txtStatus.setText("Status: " + status.replace("_", " "));
        h.txtStatus.setTextColor(corDoStatus(status));

        h.btnChat.setOnClickListener(v -> listener.onChat(o));
    }

    private int corDoStatus(String status) {
        String s = status.toUpperCase();
        if (s.contains("PEND")) return Color.parseColor("#EF6C00");
        if (s.contains("ANDAMENTO")) return Color.parseColor("#1565C0");
        if (s.contains("CONCL") || s.contains("FINAL")) return Color.parseColor("#2E7D32");
        if (s.contains("CANCEL")) return Color.parseColor("#C62828");
        return Color.parseColor("#555555");
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtServico, txtTipo, txtStatus;
        Button btnChat;

        VH(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtServico = itemView.findViewById(R.id.txtServico);
            txtTipo = itemView.findViewById(R.id.txtTipo);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnChat = itemView.findViewById(R.id.btnChat);
        }
    }
}