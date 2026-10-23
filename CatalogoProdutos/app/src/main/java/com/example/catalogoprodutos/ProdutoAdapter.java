package com.example.catalogoprodutos;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProdutoAdapter extends BaseAdapter {

    private final List<Produto> produtos = new ArrayList<>();

    private final NumberFormat moeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private final boolean modoCatalogo;

    public ProdutoAdapter() {
        this(false);
    }

    public ProdutoAdapter(boolean modoCatalogo) {
        this.modoCatalogo = modoCatalogo;
    }

    public void atualizarProdutos(List<Produto> novosProdutos) {
        produtos.clear();
        produtos.addAll(novosProdutos);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return produtos.size();
    }

    @Override
    public Produto getItem(int position) {
        return produtos.get(position);
    }

    @Override
    public long getItemId(int position) {
        Long id = getItem(position).getId();
        return id == null ? position : id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            int layout = modoCatalogo
                    ? R.layout.item_produto_catalogo
                    : R.layout.item_produto_admin;

            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(layout, parent, false);
        }

        Produto produto = getItem(position);

        ImageView imagem = convertView.findViewById(R.id.imgProduto);
        TextView nome = convertView.findViewById(R.id.txtNome);
        TextView categoria = convertView.findViewById(R.id.txtCategoria);
        TextView preco = convertView.findViewById(R.id.txtPreco);
        TextView estoque = convertView.findViewById(R.id.txtEstoque);

        ImagensProduto.carregar(
                imagem,
                produto.getNome(),
                produto.getUrlImagem()
        );

        nome.setText(produto.getNome());

        categoria.setText(produto.getCategoria());
        categoria.setVisibility(modoCatalogo ? View.VISIBLE : View.GONE);

        preco.setText(
                produto.getPreco() == null
                        ? "Preço não informado"
                        : moeda.format(produto.getPreco())
        );

        preco.setTextColor(Color.parseColor(
                modoCatalogo ? "#173D36" : "#00796B"
        ));

        Integer quantidade = produto.getQuantidadeEstoque();
        boolean disponivel = quantidade != null && quantidade > 0;

        if (modoCatalogo) {
            estoque.setText(
                    disponivel ? "Disponível" : "Indisponível"
            );

            estoque.setTextColor(Color.parseColor(
                    disponivel ? "#00796B" : "#B3261E"
            ));
        } else {
            estoque.setText(
                    "Em estoque: " + (quantidade == null ? 0 : quantidade)
            );

            estoque.setTextColor(Color.parseColor("#586E75"));
        }

        return convertView;
    }
}