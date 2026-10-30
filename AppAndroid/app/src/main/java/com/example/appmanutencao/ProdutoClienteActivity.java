package com.example.appmanutencao;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmanutencao.adapter.ProdutoClienteAdapter;
import com.example.appmanutencao.api.LojaApi;
import com.example.appmanutencao.api.RetrofitClient;
import com.example.appmanutencao.model.CarrinhoManager;
import com.example.appmanutencao.model.ProdutoLoja;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProdutoClienteActivity extends AppCompatActivity {

    private LojaApi api;
    private ProdutoClienteAdapter adapter;
    private final List<ProdutoLoja> produtos = new ArrayList<>();
    private long clienteId;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto_cliente);

        clienteId = getIntent().getLongExtra("clienteId", 1L);
        api = RetrofitClient.getClient().create(LojaApi.class);

        RecyclerView recycler = findViewById(R.id.recyclerProdutos);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProdutoClienteAdapter(produtos, this::adicionarAoCarrinho);
        recycler.setAdapter(adapter);

        Button btnVoltar = findViewById(R.id.btnVoltar);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }

        Button btnIrParaCarrinho = findViewById(R.id.btnIrParaCarrinho);
        if (btnIrParaCarrinho != null) {
            btnIrParaCarrinho.setOnClickListener(v -> {
                Intent intent = new Intent(ProdutoClienteActivity.this, CarrinhoActivity.class);
                startActivity(intent);
            });
        }

        carregarProdutos();
    }

    private void carregarProdutos() {
        api.listarProdutos().enqueue(new Callback<List<ProdutoLoja>>() {
            @Override
            public void onResponse(Call<List<ProdutoLoja>> call, Response<List<ProdutoLoja>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    produtos.clear();
                    produtos.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (produtos.isEmpty()) {
                        aviso("Nenhum produto cadastrado ainda");
                    }
                } else {
                    aviso("Erro ao carregar produtos (código " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<ProdutoLoja>> call, Throwable t) {
                Log.e("API_ERRO", "Falha produtos: " + t.getMessage());
                aviso("Sem conexão com o backend para produtos");
            }
        });
    }

    private void adicionarAoCarrinho(ProdutoLoja produto) {
        int estoque = produto.getQuantidadeEstoque();
        if (estoque <= 0) {
            aviso("Produto sem estoque");
            return;
        }

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText("1");
        input.setSelectAllOnFocus(true);

        LinearLayout container = new LinearLayout(this);
        int margem = (int) (24 * getResources().getDisplayMetrics().density);
        container.setPadding(margem, margem / 2, margem, 0);
        container.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        new AlertDialog.Builder(this)
                .setTitle("Adicionar " + produto.getNome())
                .setMessage("Preço: " + moeda.format(produto.getPreco())
                        + "\nDisponível: " + estoque
                        + "\n\nQuantidade:")
                .setView(container)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    int quantidade;
                    try {
                        quantidade = Integer.parseInt(input.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        aviso("Digite uma quantidade válida");
                        return;
                    }
                    if (quantidade <= 0) {
                        aviso("A quantidade deve ser maior que zero");
                        return;
                    }
                    if (quantidade > estoque) {
                        aviso("Quantidade maior que o estoque (" + estoque + ")");
                        return;
                    }

                    // Envia ID, Nome, Quantidade e Preço Unitário para o CarrinhoManager
                    CarrinhoManager.getInstance().setClienteId(clienteId);
                    CarrinhoManager.getInstance().adicionarItem(
                            produto.getId(),
                            produto.getNome(),
                            quantidade,
                            produto.getPreco()
                    );
                    aviso(produto.getNome() + " adicionado ao carrinho!");
                })
                .show();
    }

    private void aviso(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
    }
}