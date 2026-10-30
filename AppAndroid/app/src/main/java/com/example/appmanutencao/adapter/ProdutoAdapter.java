
package com.example.appmanutencao.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appmanutencao.R;
import com.example.appmanutencao.model.Produto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder> {

    private static final String BASE_URL = "http://10.0.2.2:8080";

    public interface OnProdutoListener {
        void onEditar(Produto produto);
        void onExcluir(Produto produto);
    }

    private List<Produto> lista = new ArrayList<>();
    private final OnProdutoListener listener;

    public ProdutoAdapter(OnProdutoListener listener) {
        this.listener = listener;
    }

    public void setLista(List<Produto> novaLista) {
        this.lista = novaLista != null ? novaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_produto, parent, false);
        return new ProdutoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder holder, int position) {
        Produto produto = lista.get(position);

        holder.textNome.setText(
                produto.getNome() != null ? produto.getNome() : "Sem nome"
        );

        holder.textCategoria.setText(
                produto.getCategoria() != null ? produto.getCategoria() : "Sem categoria"
        );

        double preco = produto.getPreco() != null ? produto.getPreco() : 0.0;
        int estoque = produto.getQuantidadeEstoque() != null
                ? produto.getQuantidadeEstoque() : 0;

        holder.textPrecoEstoque.setText(
                String.format(Locale.getDefault(),
                        "R$ %.2f  |  Estoque: %d", preco, estoque)
        );

        String imagemUrl = produto.getImagemUrl();

        if (imagemUrl != null && !imagemUrl.trim().isEmpty()) {
            String urlCompleta;

            if (imagemUrl.startsWith("http://")
                    || imagemUrl.startsWith("https://")) {
                urlCompleta = imagemUrl;
            } else {
                urlCompleta = BASE_URL
                        + (imagemUrl.startsWith("/") ? "" : "/")
                        + imagemUrl;
            }

            holder.imageProduto.setVisibility(View.VISIBLE);

            Glide.with(holder.itemView.getContext())
                    .load(urlCompleta)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(holder.imageProduto);
        } else {
            Glide.with(holder.itemView.getContext())
                    .clear(holder.imageProduto);
            holder.imageProduto.setVisibility(View.GONE);
        }

        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditar(produto);
            }
        });

        holder.btnExcluir.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExcluir(produto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ProdutoViewHolder extends RecyclerView.ViewHolder {

        ImageView imageProduto;
        TextView textNome;
        TextView textCategoria;
        TextView textPrecoEstoque;
        Button btnEditar;
        Button btnExcluir;

        ProdutoViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProduto = itemView.findViewById(R.id.imageProduto);
            textNome = itemView.findViewById(R.id.textNome);
            textCategoria = itemView.findViewById(R.id.textCategoria);
            textPrecoEstoque = itemView.findViewById(R.id.textPrecoEstoque);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}
