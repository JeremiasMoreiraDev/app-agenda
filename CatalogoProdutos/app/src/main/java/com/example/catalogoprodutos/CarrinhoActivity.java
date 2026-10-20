package com.example.catalogoprodutos;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private static final String URL_PEDIDOS =
            "http://127.0.0.1:8080/api/pedidos";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private MaterialButton btnConfirmar;
    private MaterialButton btnUltimoPedido;
    private boolean enviando;

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

        // Os botões ficam no painel do total do XML existente.
        LinearLayout painel = (LinearLayout) txtTotal.getParent();
        btnConfirmar = criarBotao("Confirmar pedido", "#00796B");
        btnUltimoPedido = criarBotao("Ver último pedido", "#00796B");
        for (MaterialButton botao : new MaterialButton[]{btnConfirmar, btnUltimoPedido}) {
            LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            parametros.topMargin = dp(8);
            botao.setLayoutParams(parametros);
            painel.addView(botao);
        }
        btnConfirmar.setOnClickListener(view -> solicitarConfirmacao());
        btnUltimoPedido.setOnClickListener(view -> {
            String pedido = carrinho.obterUltimoPedido();
            if (pedido != null) mostrarComprovante(pedido);
        });

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
        boolean pendente = carrinho.temPedidoPendente();
        btnUltimoPedido.setVisibility(
                carrinho.obterUltimoPedido() == null ? View.GONE : View.VISIBLE
        );
        btnConfirmar.setText(enviando ? "Verificando pedido..."
                : pendente ? "Verificar confirmação" : "Confirmar pedido");
        btnConfirmar.setEnabled(!enviando && pendente);

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
            btnConfirmar.setEnabled(!enviando && (pendente || !itens.isEmpty()));

            if (enviando) {
                txtStatus.setText("Aguarde a resposta da API. Seu pedido está sendo verificado.");
            } else if (pendente) {
                txtStatus.setText("Existe uma confirmação pendente. Toque em Verificar "
                        + "confirmação para consultar ou reenviar o mesmo pedido. "
                        + "Os itens ficam bloqueados até recebermos a resposta.");
            } else if (itens.isEmpty()) {
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
        diminuir.setEnabled(!carrinho.temPedidoPendente() && item.getQuantidade() > 1);
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
                !carrinho.temPedidoPendente() && item.getQuantidade() < item.getEstoque()
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

        remover.setEnabled(!carrinho.temPedidoPendente());
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


    private void solicitarConfirmacao() {
        if (enviando) return;
        try {
            if (carrinho.temPedidoPendente()) {
                enviarPedido(carrinho.obterPedidoPendente());
                return;
            }
            List<Carrinho.Item> itens = carrinho.listar();
            if (itens.isEmpty()) return;
            BigDecimal total = BigDecimal.ZERO;
            StringBuilder resumo = new StringBuilder();
            for (Carrinho.Item item : itens) {
                total = total.add(item.getSubtotal());
                resumo.append(item.getQuantidade()).append(" × ")
                        .append(item.getNome()).append("\n");
            }
            resumo.append("\nTotal: ").append(moeda.format(total))
                    .append("\n\nAo confirmar, o pedido será registrado e o estoque será reduzido. "
                            + "Esta etapa não realiza pagamento.");
            new AlertDialog.Builder(this)
                    .setTitle("Confirmar pedido?")
                    .setMessage(resumo.toString())
                    .setNegativeButton("Voltar", null)
                    .setPositiveButton("Confirmar", (dialog, which) -> {
                        try {
                            // Salva a chave e os itens antes de iniciar a conexão.
                            enviarPedido(carrinho.prepararPedido());
                        } catch (RuntimeException erro) {
                            mostrarErro(erro);
                            mostrarCarrinho();
                        }
                    })
                    .show();
        } catch (RuntimeException erro) {
            mostrarErro(erro);
        }
    }

    private void enviarPedido(String corpo) {
        if (enviando || corpo == null) return;
        final String chave;
        try {
            chave = new JSONObject(corpo).getString("chaveConfirmacao");
        } catch (Exception erro) {
            mostrarAviso("Confirmação pendente inválida",
                    "Não foi possível ler o pedido salvo. Os dados foram preservados.");
            return;
        }
        enviando = true;
        mostrarCarrinho();
        executor.execute(() -> {
            HttpURLConnection conexao = null;
            try {
                conexao = (HttpURLConnection) new URL(URL_PEDIDOS).openConnection();
                conexao.setRequestMethod("POST");
                conexao.setConnectTimeout(10000);
                conexao.setReadTimeout(15000);
                conexao.setRequestProperty("Accept", "application/json");
                conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conexao.setDoOutput(true);
                byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
                conexao.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream saida = conexao.getOutputStream()) {
                    saida.write(bytes);
                }
                int status = conexao.getResponseCode();
                if (status == HttpURLConnection.HTTP_CREATED
                        || status == HttpURLConnection.HTTP_OK) {
                    StringBuilder resposta = new StringBuilder();
                    try (BufferedReader leitor = new BufferedReader(new InputStreamReader(
                            conexao.getInputStream(), StandardCharsets.UTF_8))) {
                        String linha;
                        while ((linha = leitor.readLine()) != null) resposta.append(linha);
                    }
                    String confirmado = resposta.toString();
                    JSONObject dados = new JSONObject(confirmado);
                    if (!chave.equals(dados.getString("chaveConfirmacao"))) {
                        throw new IOException("A resposta contém outra chave de confirmação.");
                    }
                    // A gravação acontece mesmo se a tela tiver sido fechada.
                    // Uma segunda resposta do mesmo pedido pode chegar após uma rotação.
                    if (carrinho.temPedidoPendente()) {
                        carrinho.concluirPedido(confirmado);
                    } else {
                        String ultimo = carrinho.obterUltimoPedido();
                        if (ultimo == null || !chave.equals(
                                new JSONObject(ultimo).getString("chaveConfirmacao"))) {
                            throw new IOException("Não foi possível verificar o pedido salvo.");
                        }
                    }
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        enviando = false;
                        setResult(RESULT_OK);
                        mostrarCarrinho();
                        mostrarComprovante(confirmado);
                    });
                } else if (status == 400 || status == 404 || status == 409) {
                    // Estas respostas da API indicam uma recusa definitiva.
                    carrinho.liberarPedidoRecusado(chave);
                    String mensagem = status == 409
                            ? "A API recusou o pedido por conflito nos preços ou no estoque. "
                            + "Os itens foram mantidos. Volte ao catálogo, atualize a lista "
                            + "e remova e adicione novamente os produtos alterados antes de confirmar."
                            : status == 404
                            ? "Um produto não foi encontrado. Os itens foram mantidos. "
                            + "Atualize o catálogo e remova o produto indisponível do carrinho."
                            : "A API recusou os dados do pedido. Os itens foram mantidos. "
                            + "Revise as quantidades e os produtos antes de tentar novamente.";
                    atualizarComAviso("Pedido não confirmado", mensagem);
                } else {
                    throw new IOException("HTTP " + status);
                }
            } catch (Exception erro) {
                Log.e("Catalogo", "Não foi possível concluir a verificação do pedido", erro);
                atualizarComAviso("Não foi possível confirmar o resultado",
                        "O pedido pode ter sido registrado. Os dados de confirmação foram "
                                + "preservados. Verifique a conexão e toque em Verificar confirmação. "
                                + "Será utilizada a mesma chave para evitar um pedido duplicado.");
            } finally {
                if (conexao != null) conexao.disconnect();
            }
        });
    }

    private void atualizarComAviso(String titulo, String mensagem) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            enviando = false;
            mostrarCarrinho();
            mostrarAviso(titulo, mensagem);
        });
    }

    private void mostrarComprovante(String resposta) {
        try {
            JSONObject pedido = new JSONObject(resposta);
            JSONArray itens = pedido.getJSONArray("itens");
            StringBuilder texto = new StringBuilder();
            for (int i = 0; i < itens.length(); i++) {
                JSONObject item = itens.getJSONObject(i);
                texto.append(item.getInt("quantidade")).append(" × ")
                        .append(item.getString("nomeProduto")).append(" — ")
                        .append(moeda.format(new BigDecimal(item.getString("subtotal"))))
                        .append("\n");
            }
            texto.append("\nTotal: ").append(moeda.format(
                            new BigDecimal(pedido.getString("total"))))
                    .append("\n\nPedido registrado. Nenhum pagamento foi realizado pelo aplicativo.");
            mostrarAviso("Pedido nº " + pedido.getLong("id") + " confirmado", texto.toString());
        } catch (Exception erro) {
            mostrarAviso("Comprovante indisponível",
                    "Não foi possível exibir os detalhes do comprovante salvo. "
                            + "Não envie outro pedido para substituir esta consulta.");
        }
    }

    private void mostrarAviso(String titulo, String mensagem) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        // Não interrompe um POST já iniciado ao sair ou girar a tela.
        executor.shutdown();
        super.onDestroy();
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
