package com.example.catalogoprodutos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminProdutosActivity extends AppCompatActivity {

    private static final String URL_PRODUTOS =
            "http://127.0.0.1:8080/api/produtos";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private ProdutoAdapter adapter;
    private TextView txtStatus;
    private Button btnAtualizar;
    private boolean carregando = false;

    private final ActivityResultLauncher<Intent> abrirGerenciamento =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    resultado -> {
                        if (resultado.getResultCode() == RESULT_OK) {
                            setResult(RESULT_OK);
                            carregarProdutos();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_produtos);

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

        txtStatus = findViewById(R.id.txtStatusAdmin);
        btnAtualizar = findViewById(R.id.btnAtualizarAdmin);

        Button btnCadastrar = findViewById(R.id.btnNovoProduto);
        ListView lista = findViewById(R.id.listaProdutosAdmin);

        adapter = new ProdutoAdapter();
        lista.setAdapter(adapter);

        findViewById(R.id.btnVoltarAdmin).setOnClickListener(
                view -> finish()
        );

        btnCadastrar.setOnClickListener(view -> {
            Intent tela = new Intent(
                    this,
                    CadastroProdutoActivity.class
            );

            abrirGerenciamento.launch(tela);
        });

        lista.setOnItemClickListener((parent, view, position, id) -> {
            Produto produto = adapter.getItem(position);

            Intent tela = new Intent(
                    this,
                    DetalheProdutoActivity.class
            );

            tela.putExtra("modo_admin", true);
            tela.putExtra("id", produto.getId().longValue());
            tela.putExtra("nome", produto.getNome());
            tela.putExtra("categoria", produto.getCategoria());
            tela.putExtra("descricao", produto.getDescricao());
            tela.putExtra("urlImagem", produto.getUrlImagem());

            if (produto.getPreco() != null) {
                tela.putExtra(
                        "preco",
                        produto.getPreco().toPlainString()
                );
            }

            Integer estoque = produto.getQuantidadeEstoque();

            tela.putExtra(
                    "quantidadeEstoque",
                    estoque != null ? estoque : 0
            );

            abrirGerenciamento.launch(tela);
        });

        btnAtualizar.setOnClickListener(view -> carregarProdutos());

        carregarProdutos();
    }

    private void carregarProdutos() {
        if (carregando || isFinishing() || isDestroyed()) {
            return;
        }

        carregando = true;
        btnAtualizar.setEnabled(false);
        txtStatus.setText("Carregando produtos...");

        executor.execute(() -> {
            try {
                List<Produto> produtos = buscarProdutos();

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    carregando = false;
                    btnAtualizar.setEnabled(true);
                    adapter.atualizarProdutos(produtos);

                    txtStatus.setText(
                            produtos.isEmpty()
                                    ? "Nenhum produto ativo cadastrado."
                                    : "Produtos ativos: " + produtos.size()
                    );
                });

            } catch (Exception erro) {
                Log.e(
                        "Catalogo",
                        "Erro ao carregar produtos na administração",
                        erro
                );

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    carregando = false;
                    btnAtualizar.setEnabled(true);

                    txtStatus.setText(
                            "Não foi possível atualizar a lista. "
                                    + "Verifique a conexão com a API."
                    );
                });
            }
        });
    }

    private List<Produto> buscarProdutos() throws Exception {
        HttpURLConnection conexao = (HttpURLConnection)
                new URL(URL_PRODUTOS).openConnection();

        try {
            conexao.setRequestMethod("GET");
            conexao.setConnectTimeout(10000);
            conexao.setReadTimeout(10000);
            conexao.setRequestProperty("Accept", "application/json");

            int status = conexao.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("Erro HTTP: " + status);
            }

            StringBuilder resposta = new StringBuilder();

            try (BufferedReader leitor = new BufferedReader(
                    new InputStreamReader(
                            conexao.getInputStream(),
                            StandardCharsets.UTF_8
                    )
            )) {
                String linha;

                while ((linha = leitor.readLine()) != null) {
                    resposta.append(linha);
                }
            }

            JSONArray dados = new JSONArray(resposta.toString());
            List<Produto> produtos = new ArrayList<>();

            for (int i = 0; i < dados.length(); i++) {
                JSONObject item = dados.getJSONObject(i);

                String descricao = item.isNull("descricao")
                        ? "" : item.optString("descricao", "");

                String urlImagem = item.isNull("urlImagem")
                        ? "" : item.optString("urlImagem", "");

                produtos.add(new Produto(
                        item.getLong("id"),
                        item.getString("nome"),
                        descricao,
                        item.getString("categoria"),
                        new BigDecimal(item.getString("preco")),
                        item.getInt("quantidadeEstoque"),
                        urlImagem
                ));
            }

            return produtos;

        } finally {
            conexao.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}