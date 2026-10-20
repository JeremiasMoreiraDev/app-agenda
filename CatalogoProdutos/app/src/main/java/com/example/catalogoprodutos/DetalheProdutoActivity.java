package com.example.catalogoprodutos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetalheProdutoActivity extends AppCompatActivity {

    private static final String URL_PRODUTOS =
            "http://127.0.0.1:8080/api/produtos";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private boolean alterado = false;
    private boolean desativando = false;

    private Button btnEditarProduto;
    private Button btnDesativarProduto;

    private final ActivityResultLauncher<Intent> abrirEdicao =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    resultado -> {
                        if (resultado.getResultCode() == RESULT_OK
                                && resultado.getData() != null) {

                            getIntent().putExtras(resultado.getData());

                            mostrarProduto();
                            alterado = true;
                            setResult(RESULT_OK);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhe_produto);

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

        if (savedInstanceState != null) {
            Bundle produto = savedInstanceState.getBundle("produto");

            if (produto != null) {
                getIntent().putExtras(produto);
            }

            alterado = savedInstanceState.getBoolean(
                    "alterado", false
            );

            if (alterado) {
                setResult(RESULT_OK);
            }
        }

        btnEditarProduto = findViewById(R.id.btnEditarProduto);
        btnDesativarProduto = findViewById(R.id.btnDesativarProduto);

        boolean modoAdmin = getIntent().getBooleanExtra(
                "modo_admin", false
        );

        boolean idValido = getIntent().getLongExtra("id", -1L) > 0;

        btnEditarProduto.setVisibility(
                modoAdmin ? View.VISIBLE : View.GONE
        );

        btnDesativarProduto.setVisibility(
                modoAdmin ? View.VISIBLE : View.GONE
        );

        btnEditarProduto.setEnabled(modoAdmin && idValido);
        btnDesativarProduto.setEnabled(modoAdmin && idValido);

        findViewById(R.id.btnVoltar).setOnClickListener(view -> {
            if (!desativando) {
                finish();
            }
        });

        btnEditarProduto.setOnClickListener(view -> {
            Intent tela = new Intent(
                    this,
                    CadastroProdutoActivity.class
            );

            tela.putExtras(getIntent());
            abrirEdicao.launch(tela);
        });

        btnDesativarProduto.setOnClickListener(
                view -> confirmarDesativacao()
        );

        mostrarProduto();
    }

    private void confirmarDesativacao() {
        if (desativando) {
            return;
        }

        String nome = getIntent().getStringExtra("nome");

        new AlertDialog.Builder(this)
                .setTitle("Desativar produto?")
                .setMessage(
                        "O produto \"" + nome + "\" deixará de aparecer "
                                + "no catálogo. O registro será mantido "
                                + "no banco de dados."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(
                        "Desativar",
                        (dialog, which) -> desativarProduto()
                )
                .show();
    }

    private void desativarProduto() {
        long id = getIntent().getLongExtra("id", -1L);

        if (desativando || id <= 0) {
            return;
        }

        desativando = true;
        btnEditarProduto.setEnabled(false);
        btnDesativarProduto.setEnabled(false);
        btnDesativarProduto.setText("Desativando...");

        // Ao voltar, atualiza a lista mesmo se a resposta se perder.
        alterado = true;
        setResult(RESULT_OK);

        executor.execute(() -> {
            try {
                enviarDesativacao(id);

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    Toast.makeText(
                            this,
                            "Produto desativado com sucesso!",
                            Toast.LENGTH_LONG
                    ).show();

                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception erro) {
                Log.e("Catalogo", "Erro ao desativar produto", erro);

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    desativando = false;
                    btnEditarProduto.setEnabled(true);
                    btnDesativarProduto.setEnabled(true);
                    btnDesativarProduto.setText("Desativar produto");

                    new AlertDialog.Builder(this)
                            .setTitle("Não foi possível confirmar")
                            .setMessage(
                                    "Confira a conexão e volte ao catálogo "
                                            + "para verificar se o produto "
                                            + "ainda aparece.\n\n"
                                            + erro.getMessage()
                            )
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        });
    }

    private void enviarDesativacao(long id) throws IOException {
        HttpURLConnection conexao = (HttpURLConnection)
                new URL(URL_PRODUTOS + "/" + id).openConnection();

        try {
            conexao.setRequestMethod("DELETE");
            conexao.setConnectTimeout(10000);
            conexao.setReadTimeout(10000);
            conexao.setRequestProperty("Accept", "application/json");

            int status = conexao.getResponseCode();

            if (status != HttpURLConnection.HTTP_NO_CONTENT) {
                throw new IOException("Resposta da API: HTTP " + status);
            }
        } finally {
            conexao.disconnect();
        }
    }

    private void mostrarProduto() {
        Intent dados = getIntent();

        TextView txtNome = findViewById(R.id.txtDetalheNome);
        TextView txtCategoria = findViewById(R.id.txtDetalheCategoria);
        TextView txtPreco = findViewById(R.id.txtDetalhePreco);
        TextView txtEstoque = findViewById(R.id.txtDetalheEstoque);
        TextView txtDescricao = findViewById(R.id.txtDetalheDescricao);
        ImageView imagem = findViewById(R.id.imgDetalheProduto);

        txtNome.setText(dados.getStringExtra("nome"));
        txtCategoria.setText(dados.getStringExtra("categoria"));

        String descricao = dados.getStringExtra("descricao");

        txtDescricao.setText(
                descricao == null || descricao.trim().isEmpty()
                        ? "Sem descrição disponível."
                        : descricao
        );

        String preco = dados.getStringExtra("preco");

        if (preco == null) {
            txtPreco.setText("Preço não informado");
        } else {
            NumberFormat moeda = NumberFormat.getCurrencyInstance(
                    new Locale("pt", "BR")
            );

            txtPreco.setText(moeda.format(new BigDecimal(preco)));
        }

        int estoque = dados.getIntExtra("quantidadeEstoque", 0);

        txtEstoque.setText(
                estoque > 0
                        ? "Em estoque: " + estoque + " unidade(s)"
                        : "Indisponível em estoque"
        );

        String urlImagem = dados.getStringExtra("urlImagem");

        if (urlImagem != null) {
            urlImagem = urlImagem.trim();

            if (urlImagem.isEmpty()
                    || urlImagem.equalsIgnoreCase("null")) {
                urlImagem = null;
            }
        }

        Glide.with(this)
                .load(urlImagem)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .fallback(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .fitCenter()
                .into(imagem);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle("produto", getIntent().getExtras());
        outState.putBoolean("alterado", alterado);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}