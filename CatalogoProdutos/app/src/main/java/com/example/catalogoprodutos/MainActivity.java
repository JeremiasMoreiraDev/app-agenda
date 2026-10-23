package com.example.catalogoprodutos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class MainActivity extends AppCompatActivity {
    private static final String URL_PRODUTOS = "http://127.0.0.1:8080/api/produtos";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ProdutoAdapter adapter;
    private TextView txtStatus;
    private TextView txtContadorCarrinho;
    private View btnCarrinho;
    private Button btnAtualizar;
    private boolean carregando;

    private final ActivityResultLauncher<Intent> abrirTela = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == RESULT_OK) carregarProdutos();
                atualizarContadorCarrinho();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
        txtStatus = findViewById(R.id.txtStatus);
        txtContadorCarrinho = findViewById(R.id.txtContadorCarrinho);
        btnCarrinho = findViewById(R.id.btnCarrinho);
        btnAtualizar = findViewById(R.id.btnAtualizar);
        ListView lista = findViewById(R.id.listaProdutos);
        adapter = new ProdutoAdapter(true);
        lista.setAdapter(adapter);
        lista.setOnItemClickListener((parent, view, position, id) -> {
            Produto produto = adapter.getItem(position);
            Intent tela = new Intent(this, DetalheProdutoActivity.class);
            if (produto.getId() != null) tela.putExtra("id", produto.getId().longValue());
            tela.putExtra("modo_admin", false);
            tela.putExtra("nome", produto.getNome());
            tela.putExtra("categoria", produto.getCategoria());
            tela.putExtra("descricao", produto.getDescricao());
            tela.putExtra("urlImagem", produto.getUrlImagem());
            if (produto.getPreco() != null) {
                tela.putExtra("preco", produto.getPreco().toPlainString());
            }
            Integer estoque = produto.getQuantidadeEstoque();
            tela.putExtra("quantidadeEstoque", estoque == null ? 0 : estoque.intValue());
            abrirTela.launch(tela);
        });
        btnCarrinho.setOnClickListener(view ->
                abrirTela.launch(new Intent(this, CarrinhoActivity.class)));
        btnAtualizar.setOnClickListener(view -> carregarProdutos());
        carregarProdutos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarContadorCarrinho();
    }

    private void atualizarContadorCarrinho() {
        if (txtContadorCarrinho == null) return;
        try {
            long unidades = 0;
            for (Carrinho.Item item : new Carrinho(this).listar()) {
                unidades += item.getQuantidade();
            }
            txtContadorCarrinho.setText(unidades > 99 ? "99+" : String.valueOf(unidades));
            txtContadorCarrinho.setVisibility(unidades > 0 ? View.VISIBLE : View.GONE);
            btnCarrinho.setContentDescription("Abrir carrinho, " + unidades + " unidades");
        } catch (IllegalStateException erro) {
            txtContadorCarrinho.setText("!");
            txtContadorCarrinho.setVisibility(View.VISIBLE);
            btnCarrinho.setContentDescription("Abrir carrinho para verificar os dados salvos");
            Log.e("Catalogo", "Erro ao consultar contador do carrinho", erro);
        }
    }

    private void carregarProdutos() {
        if (txtStatus == null || carregando || isFinishing() || isDestroyed()) return;
        carregando = true;
        txtStatus.setText("Carregando produtos...");
        btnAtualizar.setEnabled(false);
        executor.execute(() -> {
            try {
                List<Produto> produtos = buscarProdutos();
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    carregando = false;
                    adapter.atualizarProdutos(produtos);
                    txtStatus.setText(produtos.isEmpty() ? "Nenhum produto disponível."
                            : "Produtos disponíveis: " + produtos.size());
                    btnAtualizar.setEnabled(true);
                });
            } catch (Exception erro) {
                Log.e("Catalogo", "Erro ao buscar produtos", erro);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    carregando = false;
                    txtStatus.setText("Não foi possível atualizar os produtos. "
                            + "Verifique a conexão com a API.");
                    btnAtualizar.setEnabled(true);
                });
            }
        });
    }

    private List<Produto> buscarProdutos() throws Exception {
        HttpURLConnection conexao = (HttpURLConnection) new URL(URL_PRODUTOS).openConnection();
        try {
            conexao.setRequestMethod("GET");
            conexao.setConnectTimeout(10000);
            conexao.setReadTimeout(10000);
            conexao.setRequestProperty("Accept", "application/json");
            int status = conexao.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) throw new IOException("Erro HTTP: " + status);
            StringBuilder resposta = new StringBuilder();
            try (BufferedReader leitor = new BufferedReader(new InputStreamReader(
                    conexao.getInputStream(), StandardCharsets.UTF_8))) {
                String linha;
                while ((linha = leitor.readLine()) != null) resposta.append(linha);
            }
            JSONArray dados = new JSONArray(resposta.toString());
            List<Produto> produtos = new ArrayList<>();
            for (int i = 0; i < dados.length(); i++) {
                JSONObject item = dados.getJSONObject(i);
                produtos.add(new Produto(item.getLong("id"), item.getString("nome"),
                        item.isNull("descricao") ? "" : item.optString("descricao", ""),
                        item.getString("categoria"), new BigDecimal(item.getString("preco")),
                        item.getInt("quantidadeEstoque"),
                        item.isNull("urlImagem") ? "" : item.optString("urlImagem", "")));
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
