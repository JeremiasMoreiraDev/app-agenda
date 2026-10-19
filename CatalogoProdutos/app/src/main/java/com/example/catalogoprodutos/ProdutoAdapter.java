package com.example.catalogoprodutos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProdutoAdapter extends BaseAdapter {

    private final List<Produto> produtos = new ArrayList<>();

    private final NumberFormat moeda =
            NumberFormat.getCurrencyInstance(
                    new Locale("pt", "BR")
            );

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
        return id != null ? id : position;
    }

    @Override
    public View getView(int position, View convertView,
                        ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_produto, parent, false);
        }

        Produto produto = getItem(position);

        ImageView imagem = convertView.findViewById(R.id.imgProduto);

        String urlImagem = produto.getUrlImagem();

        if (urlImagem != null) {
            urlImagem = urlImagem.trim();

            if (urlImagem.isEmpty() || urlImagem.equalsIgnoreCase("null")) {
                urlImagem = null;
            }
        }

        Glide.with(imagem)
                .load(urlImagem)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .fallback(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerInside()
                .into(imagem);

        TextView nome = convertView.findViewById(R.id.txtNome);
        TextView categoria =
                convertView.findViewById(R.id.txtCategoria);
        TextView preco = convertView.findViewById(R.id.txtPreco);
        TextView estoque =
                convertView.findViewById(R.id.txtEstoque);

        nome.setText(produto.getNome());
        categoria.setText(produto.getCategoria());

        if (produto.getPreco() != null) {
            preco.setText(moeda.format(produto.getPreco()));
        } else {
            preco.setText("Preço não informado");
        }

        Integer quantidade = produto.getQuantidadeEstoque();

        if (quantidade != null && quantidade > 0) {
            estoque.setText("Disponível em estoque");
        } else {
            estoque.setText("Indisponível");
        }

        return convertView;
    }
}