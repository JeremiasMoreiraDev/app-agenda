package com.example.appmanutencao;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmanutencao.adapter.ProdutoAdapter;
import com.example.appmanutencao.api.ApiService;
import com.example.appmanutencao.api.RetrofitClient;
import com.example.appmanutencao.model.Produto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProdutoActivity extends AppCompatActivity {

    private EditText editNome, editDescricao, editCategoria, editPreco, editEstoque;
    private Button btnSalvarProduto, btnCancelarEdicao, btnVoltar;
    private RecyclerView recyclerProdutos;
    private ProdutoAdapter adapter;
    private ApiService apiService;

    // Se for null, estamos cadastrando. Se tiver um id, estamos editando.
    private Long produtoEmEdicaoId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // PROTEÇÃO: só o ADMIN pode ver esta tela
        String tipoPerfil = getIntent().getStringExtra("tipoPerfil");
        if (!"ADMIN".equalsIgnoreCase(tipoPerfil)) {
            Toast.makeText(this, "Acesso restrito ao administrador", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_produto);

        editNome = findViewById(R.id.editNomeProduto);
        editDescricao = findViewById(R.id.editDescricaoProduto);
        editCategoria = findViewById(R.id.editCategoriaProduto);
        editPreco = findViewById(R.id.editPrecoProduto);
        editEstoque = findViewById(R.id.editEstoqueProduto);
        btnSalvarProduto = findViewById(R.id.btnSalvarProduto);
        btnCancelarEdicao = findViewById(R.id.btnCancelarEdicao);
        btnVoltar = findViewById(R.id.btnVoltar);
        recyclerProdutos = findViewById(R.id.recyclerProdutos);

        apiService = RetrofitClient.getApiService();

        recyclerProdutos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProdutoAdapter(new ProdutoAdapter.OnProdutoListener() {
            @Override
            public void onEditar(Produto produto) {
                iniciarEdicao(produto);
            }

            @Override
            public void onExcluir(Produto produto) {
                confirmarExclusao(produto);
            }
        });
        recyclerProdutos.setAdapter(adapter);

        // Volta para a tela anterior (lista de ordens do ADM)
        btnVoltar.setOnClickListener(v -> finish());

        btnSalvarProduto.setOnClickListener(v -> salvarProduto());
        btnCancelarEdicao.setOnClickListener(v -> limparFormulario());

        carregarProdutos();
    }

    private void carregarProdutos() {
        apiService.listarProdutos().enqueue(new Callback<List<Produto>>() {
            @Override
            public void onResponse(Call<List<Produto>> call, Response<List<Produto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setLista(response.body());
                } else {
                    Log.e("API_ERRO", "Erro ao listar produtos. Codigo: " + response.code());
                    Toast.makeText(ProdutoActivity.this,
                            "Erro ao carregar produtos (codigo " + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Produto>> call, Throwable t) {
                Log.e("API_ERRO", "Falha produtos: " + t.getMessage());
                Toast.makeText(ProdutoActivity.this,
                        "Sem conexao com o backend para produtos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void salvarProduto() {
        String nome = editNome.getText().toString().trim();
        String descricao = editDescricao.getText().toString().trim();
        String categoria = editCategoria.getText().toString().trim();
        String precoTexto = editPreco.getText().toString().trim().replace(",", ".");
        String estoqueTexto = editEstoque.getText().toString().trim();

        if (nome.isEmpty() || precoTexto.isEmpty()) {
            Toast.makeText(this, "Preencha pelo menos o nome e o preco", Toast.LENGTH_SHORT).show();
            return;
        }

        double preco;
        try {
            preco = Double.parseDouble(precoTexto);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Preco invalido. Exemplo: 899,90", Toast.LENGTH_SHORT).show();
            return;
        }

        int estoque = 0;
        if (!estoqueTexto.isEmpty()) {
            try {
                estoque = Integer.parseInt(estoqueTexto);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Estoque invalido. Use apenas numeros", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setCategoria(categoria);
        produto.setPreco(preco);
        produto.setQuantidadeEstoque(estoque);

        if (produtoEmEdicaoId == null) {
            apiService.criarProduto(produto).enqueue(callbackSalvar("Produto cadastrado com sucesso!"));
        } else {
            apiService.atualizarProduto(produtoEmEdicaoId, produto)
                    .enqueue(callbackSalvar("Produto atualizado com sucesso!"));
        }
    }

    private Callback<Produto> callbackSalvar(String mensagemSucesso) {
        return new Callback<Produto>() {
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProdutoActivity.this, mensagemSucesso, Toast.LENGTH_SHORT).show();
                    limparFormulario();
                    carregarProdutos();
                } else {
                    Log.e("API_ERRO", "Erro ao salvar produto. Codigo: " + response.code());
                    Toast.makeText(ProdutoActivity.this,
                            "Erro ao salvar produto (codigo " + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Produto> call, Throwable t) {
                Log.e("API_ERRO", "Falha ao salvar produto: " + t.getMessage());
                Toast.makeText(ProdutoActivity.this, "Erro de conexao", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void iniciarEdicao(Produto produto) {
        produtoEmEdicaoId = produto.getId();

        editNome.setText(produto.getNome());
        editDescricao.setText(produto.getDescricao());
        editCategoria.setText(produto.getCategoria());
        editPreco.setText(produto.getPreco() != null ? String.valueOf(produto.getPreco()) : "");
        editEstoque.setText(produto.getQuantidadeEstoque() != null
                ? String.valueOf(produto.getQuantidadeEstoque()) : "");

        btnSalvarProduto.setText("Salvar Alteracoes");
        btnCancelarEdicao.setVisibility(View.VISIBLE);
    }

    private void confirmarExclusao(Produto produto) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir produto")
                .setMessage("Deseja realmente excluir \"" + produto.getNome() + "\"?")
                .setPositiveButton("Excluir", (dialog, which) -> excluirProduto(produto))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirProduto(Produto produto) {
        apiService.deletarProduto(produto.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProdutoActivity.this, "Produto excluido", Toast.LENGTH_SHORT).show();
                    // Se estava editando justamente este produto, limpa o formulario
                    if (produtoEmEdicaoId != null && produtoEmEdicaoId.equals(produto.getId())) {
                        limparFormulario();
                    }
                    carregarProdutos();
                } else {
                    Log.e("API_ERRO", "Erro ao excluir produto. Codigo: " + response.code());
                    Toast.makeText(ProdutoActivity.this,
                            "Erro ao excluir (codigo " + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("API_ERRO", "Falha ao excluir produto: " + t.getMessage());
                Toast.makeText(ProdutoActivity.this, "Erro de conexao", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void limparFormulario() {
        produtoEmEdicaoId = null;
        editNome.setText("");
        editDescricao.setText("");
        editCategoria.setText("");
        editPreco.setText("");
        editEstoque.setText("");
        btnSalvarProduto.setText("Cadastrar Produto");
        btnCancelarEdicao.setVisibility(View.GONE);
    }
}