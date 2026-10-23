package com.example.catalogoprodutos;

import android.widget.ImageView;
import com.bumptech.glide.Glide;

public final class ImagensProduto {
    private ImagensProduto() { }

    public static void carregar(ImageView imagem, String nome, String urlImagem) {
        String url = urlImagem == null ? null : urlImagem.trim();
        if (url != null && (url.isEmpty() || url.equalsIgnoreCase("null"))) {
            url = null;
        }
        Object modelo = url;
        // Ilustrações locais apenas para os dois produtos de demonstração.
        // Os demais produtos continuam usando a URL cadastrada na API.
        if ("Painel Solar - TESTE".equalsIgnoreCase(nome)) {
            modelo = R.drawable.painel_solar_demo;
        } else if ("Inversor Solar - TESTE".equalsIgnoreCase(nome)) {
            modelo = R.drawable.inversor_solar_demo;
        }
        Glide.with(imagem)
                .load(modelo)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .fallback(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .fitCenter()
                .into(imagem);
        imagem.setContentDescription("Foto de " + (nome == null ? "produto" : nome));
    }
}
