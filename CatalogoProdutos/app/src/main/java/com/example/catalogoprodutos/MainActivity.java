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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String URL_PRODUTOS =
            "http://127.0.0.1:8080/api/produtos";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private ProdutoAdapter adapter;
    private TextView txtStatus;
    private Button btnAtualizar;

    private final ActivityResultLauncher<Intent> abrirCadastro =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    resultado -> {
                        if (resultado.getResultCode() == RESULT_OK) {
                            carregarProdutos();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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

        txtStatus = findViewById(R.id.txtStatus);
        btnAtualizar = findViewById(R.id.btnAtualizar);

        Button btnCadastrar = findViewById(R.id.btnCadastrar);
        ListView listaProdutos = findViewById(R.id.listaProdutos);

        adapter = new ProdutoAdapter();
        listaProdutos.setAdapter(adapter);

        listaProdutos.setOnItemClickListener(
                (parent, view, position, id) -> {
                    Produto produto = adapter.getItem(position);

                    Intent tela = new Intent(
                            MainActivity.this,
                            DetalheProdutoActivity.class
                    );

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
                            estoque != null ? estoque.intValue() : 0
                    );

                    abrirCadastro.launch(tela);
                }
        );

        btnCadastrar.setText("Administrar produtos (teste)");

        btnCadastrar.setOnClickListener(view -> {
            Intent tela = new Intent(
                    MainActivity.this,
                    AdminProdutosActivity.class
            );

            abrirCadastro.launch(tela);
        });

        btnAtualizar.setOnClickListener(view -> carregarProdutos());

        carregarProdutos();
    }

    private void carregarProdutos() {
        txtStatus.setText("Carregando produtos...");
        btnAtualizar.setEnabled(false);

        executor.execute(() -> {
            try {
                List<Produto> produtos = buscarProdutos();

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    adapter.atualizarProdutos(produtos);

                    if (produtos.isEmpty()) {
                        txtStatus.setText("Nenhum produto disponível.");
                    } else {
                        txtStatus.setText(
                                "Produtos carregados: " + produtos.size()
                        );
                    }

                    btnAtualizar.setEnabled(true);
                });

            } catch (Exception erro) {
                Log.e("Catalogo", "Erro ao buscar produtos", erro);

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    txtStatus.setText(
                            "Não foi possível atualizar os produtos. "
                                    + "Verifique se a API está rodando."
                    );

                    btnAtualizar.setEnabled(true);
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
                            conexao.getInputStream(), "UTF-8"
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

                Produto produto = new Produto(
                        item.getLong("id"),
                        item.getString("nome"),
                        descricao,
                        item.getString("categoria"),
                        new BigDecimal(item.getString("preco")),
                        item.getInt("quantidadeEstoque"),
                        urlImagem
                );

                produtos.add(produto);
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