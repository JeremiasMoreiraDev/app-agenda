
package com.example.appmanutencao.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmanutencao.R;
import com.example.appmanutencao.model.OrdemServico;

import java.util.ArrayList;
import java.util.List;

public class OrdemServicoAdapter
        extends RecyclerView.Adapter<OrdemServicoAdapter.ViewHolder> {

    public interface OnStatusClickListener {
        void onStatusClick(OrdemServico ordem);
    }

    private final List<OrdemServico> lista = new ArrayList<>();
    private final OnStatusClickListener statusClickListener;

    public OrdemServicoAdapter(OnStatusClickListener listener) {
        this.statusClickListener = listener;
    }

    public void setLista(List<OrdemServico> novaLista) {
        lista.clear();

        if (novaLista != null) {
            lista.addAll(novaLista);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ordem_servico, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        OrdemServico item = lista.get(position);

        String nomeCliente = "Não informado";

        if (item.getCliente() != null
                && item.getCliente().getNome() != null) {
            nomeCliente = item.getCliente().getNome();
        }

        String tituloServico = "Não informado";

        if (item.getServico() != null) {
            if (item.getServico().getTitulo() != null) {
                tituloServico = item.getServico().getTitulo();
            } else if (item.getServico().getDescricao() != null) {
                tituloServico = item.getServico().getDescricao();
            }
        }

        String tipoAtendimento = item.getTipoAtendimento() != null
                ? item.getTipoAtendimento()
                : "---";

        String status = item.getStatus() != null
                ? item.getStatus()
                : "Não informado";

        String descricaoServico =
                item.getServico() != null
                        && item.getServico().getDescricao() != null
                        ? item.getServico().getDescricao()
                        : "Sem descrição";

        holder.tvClienteNome.setText("Cliente: " + nomeCliente);
        holder.tvServicoNome.setText("Serviço: " + tituloServico);
        holder.tvData.setText("Tipo: " + tipoAtendimento);
        holder.tvStatus.setText("Status: " + status);
        holder.tvObservacoes.setText("Desc: " + descricaoServico);

        holder.btnAlterarStatus.setOnClickListener(v -> {
            if (statusClickListener != null) {
                statusClickListener.onStatusClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView tvClienteNome;
        final TextView tvServicoNome;
        final TextView tvData;
        final TextView tvStatus;
        final TextView tvObservacoes;
        final Button btnAlterarStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvClienteNome = itemView.findViewById(R.id.tvClienteNome);
            tvServicoNome = itemView.findViewById(R.id.tvServicoNome);
            tvData = itemView.findViewById(R.id.tvData);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvObservacoes = itemView.findViewById(R.id.tvObservacoes);
            btnAlterarStatus = itemView.findViewById(R.id.btnAlterarStatus);
        }
    }
}
