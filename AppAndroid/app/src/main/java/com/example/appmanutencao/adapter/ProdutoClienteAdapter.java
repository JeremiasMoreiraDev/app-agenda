package com.example.appmanutencao.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmanutencao.R;
import com.example.appmanutencao.model.ProdutoLoja;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProdutoClienteAdapter extends RecyclerView.Adapter<ProdutoClienteAdapter.VH> {

    public interface OnComprarClick {
        void onComprar(ProdutoLoja produto);
    }

    private final List<ProdutoLoja> lista;
    private final OnComprarClick listener;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public ProdutoClienteAdapter(List<ProdutoLoja> lista, OnComprarClick listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_produto_cliente, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ProdutoLoja p = lista.get(position);

        h.txtNome.setText(p.getNome() != null ? p.getNome() : "");
        h.txtCategoria.setText(p.getCategoria() != null ? p.getCategoria() : "");
        h.txtDescricao.setText(p.getDescricao() != null ? p.getDescricao() : "");
        h.txtPreco.setText(moeda.format(p.getPreco()));

        int estoque = p.getQuantidadeEstoque();
        if (estoque > 0) {
            h.txtEstoque.setText("Em estoque: " + estoque);
            h.btnComprar.setEnabled(true);
            h.btnComprar.setText("Comprar");
        } else {
            h.txtEstoque.setText("Sem estoque");
            h.btnComprar.setEnabled(false);
            h.btnComprar.setText("Esgotado");
        }

        h.btnComprar.setOnClickListener(v -> listener.onComprar(p));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtNome, txtCategoria, txtDescricao, txtPreco, txtEstoque;
        Button btnComprar;

        VH(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtCategoria = itemView.findViewById(R.id.txtCategoria);
            txtDescricao = itemView.findViewById(R.id.txtDescricao);
            txtPreco = itemView.findViewById(R.id.txtPreco);
            txtEstoque = itemView.findViewById(R.id.txtEstoque);
            btnComprar = itemView.findViewById(R.id.btnComprar);
        }
    }
}