package com.example.appmanutencao;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmanutencao.adapter.ItemCarrinhoAdapter;
import com.example.appmanutencao.api.LojaApi;
import com.example.appmanutencao.api.RetrofitClient;
import com.example.appmanutencao.model.CarrinhoManager;
import com.example.appmanutencao.model.Pedido;
import com.example.appmanutencao.model.PedidoRequest;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarrinhoActivity extends AppCompatActivity {

    private TextView txtTotalGeral;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private ItemCarrinhoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrinho);

        LojaApi api = RetrofitClient.getClient().create(LojaApi.class);

        txtTotalGeral = findViewById(R.id.txtTotalGeral);
        RecyclerView recycler = findViewById(R.id.recyclerCarrinho);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        CarrinhoManager carrinho = CarrinhoManager.getInstance();
        adapter = new ItemCarrinhoAdapter(carrinho.getItens());
        recycler.setAdapter(adapter);

        atualizarTotalGeral(carrinho);

        Button btnFinalizar = findViewById(R.id.btnFinalizarCarrinho);
        btnFinalizar.setOnClickListener(v -> finalizarCompraEmLote(api, carrinho));
    }

    private void atualizarTotalGeral(CarrinhoManager carrinho) {
        if (txtTotalGeral != null) {
            double total = carrinho.calcularTotalGeral();
            txtTotalGeral.setText("Total Geral: " + moeda.format(total));
        }
    }

    private void finalizarCompraEmLote(LojaApi api, CarrinhoManager carrinho) {
        if (carrinho.getItens().isEmpty()) {
            Toast.makeText(this, "O carrinho está vazio", Toast.LENGTH_SHORT).show();
            return;
        }

        Long clienteId = carrinho.getClienteId() != null ? carrinho.getClienteId() : 1L;

        PedidoRequest request = new PedidoRequest();
        request.setClienteId(clienteId);
        request.setItens(carrinho.getItens());

        api.comprar(request).enqueue(new Callback<Pedido>() {
            @Override
            public void onResponse(@NonNull Call<Pedido> call, @NonNull Response<Pedido> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Pedido pedido = response.body();
                    Toast.makeText(CarrinhoActivity.this, "Pedido criado com sucesso!", Toast.LENGTH_LONG).show();

                    carrinho.limpar();

                    Intent intent = new Intent(CarrinhoActivity.this, PagamentoActivity.class);
                    intent.putExtra(PagamentoActivity.EXTRA_PEDIDO_ID, pedido.getId());
                    startActivity(intent);
                    finish();
                } else {
                    String erro = "Erro ao fechar pedido";
                    if (response.errorBody() != null) {
                        try (okhttp3.ResponseBody errorBody = response.errorBody()) {
                            String msg = errorBody.string();
                            if (!msg.isEmpty()) {
                                erro = msg;
                            }
                        } catch (IOException e) {
                            Log.e("API_ERRO", e.getMessage() != null ? e.getMessage() : "Erro desconhecido");
                        }
                    }
                    Toast.makeText(CarrinhoActivity.this, erro, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pedido> call, @NonNull Throwable t) {
                Log.e("API_ERRO", "Falha: " + (t.getMessage() != null ? t.getMessage() : "Erro de conexão"));
                Toast.makeText(CarrinhoActivity.this, "Sem conexão com o servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}