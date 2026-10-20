package com.example.catalogoprodutos;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CarrinhoActivity extends AppCompatActivity {

    private final NumberFormat moeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private Carrinho carrinho;
    private LinearLayout listaItens;
    private TextView txtStatus;
    private TextView txtTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carrinho);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (view, insets) -> {
                    Insets barras = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            barras.left,
                            barras.top,
                            barras.right,
                            barras.bottom
                    );

                    return insets;
                }
        );

        carrinho = new Carrinho(this);
        listaItens = findViewById(R.id.listaItensCarrinho);
        txtStatus = findViewById(R.id.txtStatusCarrinho);
        txtTotal = findViewById(R.id.txtTotalCarrinho);

        findViewById(R.id.btnVoltarCarrinho).setOnClickListener(
                view -> finish()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        mostrarCarrinho();
    }

    private void mostrarCarrinho() {
        listaItens.removeAllViews();

        try {
            List<Carrinho.Item> itens = carrinho.listar();
            BigDecimal total = BigDecimal.ZERO;
            long unidades = 0;

            for (Carrinho.Item item : itens) {
                adicionarCard(item);
                total = total.add(item.getSubtotal());
                unidades += item.getQuantidade();
            }

            txtTotal.setText(moeda.format(total));

            if (itens.isEmpty()) {
                txtStatus.setText(
                        "Seu carrinho está vazio. "
                                + "Volte ao catálogo para adicionar produtos."
                );
            } else {
                txtStatus.setText(
                        "Produtos diferentes: " + itens.size()
                                + " • Unidades: " + unidades
                );
            }

        } catch (IllegalStateException erro) {
            txtStatus.setText(erro.getMessage());
            txtTotal.setText("—");
        }
    }

    private void adicionarCard(Carrinho.Item item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackgroundResource(R.drawable.fundo_card_produto);

        LinearLayout.LayoutParams parametrosCard =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametrosCard.bottomMargin = dp(12);
        card.setLayoutParams(parametrosCard);

        LinearLayout cabecalho = new LinearLayout(this);
        cabecalho.setOrientation(LinearLayout.HORIZONTAL);
        cabecalho.setGravity(Gravity.CENTER_VERTICAL);

        ImageView imagem = new ImageView(this);
        imagem.setLayoutParams(
                new LinearLayout.LayoutParams(dp(64), dp(64))
        );
        imagem.setContentDescription("Foto de " + item.getNome());

        Glide.with(this)
                .load(item.getUrlImagem())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .fallback(android.R.drawable.ic_menu_gallery)
                .centerInside()
                .into(imagem);

        cabecalho.addView(imagem);

        LinearLayout informacoes = new LinearLayout(this);
        informacoes.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams parametrosInformacoes =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        parametrosInformacoes.leftMargin = dp(12);
        informacoes.setLayoutParams(parametrosInformacoes);

        TextView nome = criarTexto(
                item.getNome(), 18, "#173D36", true
        );

        TextView preco = criarTexto(
                "Preço unitário: " + moeda.format(item.getPreco()),
                14,
                "#555555",
                false
        );

        informacoes.addView(nome);
        informacoes.addView(preco);
        cabecalho.addView(informacoes);
        card.addView(cabecalho);

        TextView quantidade = criarTexto(
                "Quantidade: " + item.getQuantidade(),
                16,
                "#173D36",
                true
        );
        quantidade.setPadding(0, dp(12), 0, dp(4));
        card.addView(quantidade);

        LinearLayout controles = new LinearLayout(this);
        controles.setOrientation(LinearLayout.HORIZONTAL);
        controles.setGravity(Gravity.CENTER_VERTICAL);

        MaterialButton diminuir = criarBotao("−", "#00796B");
        diminuir.setContentDescription(
                "Diminuir quantidade de " + item.getNome()
        );
        diminuir.setEnabled(item.getQuantidade() > 1);
        diminuir.setOnClickListener(view -> alterarQuantidade(
                item,
                item.getQuantidade() - 1
        ));
        controles.addView(diminuir);

        MaterialButton aumentar = criarBotao("+", "#00796B");
        aumentar.setContentDescription(
                "Aumentar quantidade de " + item.getNome()
        );
        aumentar.setEnabled(
                item.getQuantidade() < item.getEstoque()
        );
        aumentar.setOnClickListener(view -> alterarQuantidade(
                item,
                item.getQuantidade() + 1
        ));
        controles.addView(aumentar);

        card.addView(controles);

        TextView subtotal = criarTexto(
                "Subtotal: " + moeda.format(item.getSubtotal()),
                18,
                "#00796B",
                true
        );
        subtotal.setPadding(0, dp(8), 0, dp(8));
        card.addView(subtotal);

        MaterialButton remover = criarBotao(
                "Remover produto", "#B3261E"
        );

        remover.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        remover.setContentDescription(
                "Remover " + item.getNome() + " do carrinho"
        );

        remover.setOnClickListener(view -> {
            try {
                carrinho.remover(item.getProdutoId());
                mostrarCarrinho();
            } catch (IllegalStateException erro) {
                mostrarErro(erro);
            }
        });

        card.addView(remover);
        listaItens.addView(card);
    }

    private void alterarQuantidade(
            Carrinho.Item item,
            int quantidade
    ) {
        try {
            carrinho.alterarQuantidade(
                    item.getProdutoId(),
                    quantidade
            );

            mostrarCarrinho();

        } catch (IllegalArgumentException | IllegalStateException erro) {
            mostrarErro(erro);
        }
    }

    private TextView criarTexto(
            String texto,
            int tamanho,
            String cor,
            boolean negrito
    ) {
        TextView campo = new TextView(this);
        campo.setText(texto);
        campo.setTextSize(tamanho);
        campo.setTextColor(Color.parseColor(cor));

        if (negrito) {
            campo.setTypeface(
                    campo.getTypeface(),
                    android.graphics.Typeface.BOLD
            );
        }

        return campo;
    }

    private MaterialButton criarBotao(String texto, String cor) {
        MaterialButton botao = new MaterialButton(this);
        botao.setText(texto);
        botao.setAllCaps(false);
        botao.setTextColor(Color.WHITE);
        botao.setTextSize(16);
        botao.setMinHeight(dp(48));
        botao.setMinimumHeight(dp(48));
        botao.setCornerRadius(dp(12));
        botao.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor(cor))
        );

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        dp(64),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.rightMargin = dp(8);
        botao.setLayoutParams(parametros);

        return botao;
    }

    private void mostrarErro(RuntimeException erro) {
        Toast.makeText(
                this,
                erro.getMessage(),
                Toast.LENGTH_LONG
        ).show();
    }

    private int dp(int valor) {
        return Math.round(
                valor * getResources().getDisplayMetrics().density
        );
    }
}