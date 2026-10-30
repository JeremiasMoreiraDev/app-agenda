package com.example.appmanutencao.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmanutencao.model.ItemPedidoRequest;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ItemCarrinhoAdapter extends RecyclerView.Adapter<ItemCarrinhoAdapter.VH> {

    private final List<ItemPedidoRequest> itens;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public ItemCarrinhoAdapter(List<ItemPedidoRequest> itens) {
        this.itens = itens;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ItemPedidoRequest item = itens.get(position);

        double precoUnitario = item.getPrecoUnitario() != null ? item.getPrecoUnitario() : 0.0;
        double subtotal = precoUnitario * item.getQuantidade();

        holder.text1.setText(item.getNomeProduto() + " (Qtd: " + item.getQuantidade() + ")");
        holder.text2.setText("Subtotal: " + moeda.format(subtotal) + " (" + moeda.format(precoUnitario) + " un)");
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView text1, text2;

        VH(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}