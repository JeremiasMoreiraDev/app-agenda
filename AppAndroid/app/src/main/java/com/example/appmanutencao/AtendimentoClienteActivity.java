package com.example.appmanutencao;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmanutencao.adapter.AtendimentoClienteAdapter;
import com.example.appmanutencao.api.ApiService;
import com.example.appmanutencao.api.RetrofitClient;
import com.example.appmanutencao.model.OrdemServico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Tela do CLIENTE: lista os atendimentos dele e abre o chat de cada um
public class AtendimentoClienteActivity extends AppCompatActivity {

    private ApiService api;
    private AtendimentoClienteAdapter adapter;
    private final List<OrdemServico> atendimentos = new ArrayList<>();
    private TextView txtVazio;
    private long clienteId;
    private String usuarioAtual = "CLIENTE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atendimento_cliente);

        clienteId = getIntent().getLongExtra("clienteId", -1L);
        if (clienteId == -1L) {
            aviso("Cliente nao identificado");
            finish();
            return;
        }

        String usuario = getIntent().getStringExtra("usuarioAtual");
        if (usuario != null) {
            usuarioAtual = usuario;
        }

        api = RetrofitClient.getApiService();
        txtVazio = findViewById(R.id.txtVazio);

        RecyclerView recycler = findViewById(R.id.recyclerAtendimentos);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AtendimentoClienteAdapter(atendimentos, this::abrirChat);
        recycler.setAdapter(adapter);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
    }

    // recarrega sempre que a tela aparece (ex.: voltando do chat)
    @Override
    protected void onResume() {
        super.onResume();
        if (api != null) {
            carregarAtendimentos();
        }
    }

    private void carregarAtendimentos() {
        api.listarAgendamentos().enqueue(new Callback<List<OrdemServico>>() {
            @Override
            public void onResponse(Call<List<OrdemServico>> call, Response<List<OrdemServico>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    atendimentos.clear();
                    for (OrdemServico o : response.body()) {
                        if (ehDoCliente(o)) {
                            atendimentos.add(o);
                        }
                    }
                    // mais novos primeiro
                    Collections.sort(atendimentos, (a, b) -> Long.compare(idDe(b), idDe(a)));
                    adapter.notifyDataSetChanged();
                    txtVazio.setVisibility(atendimentos.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    aviso("Erro ao carregar atendimentos (codigo " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<OrdemServico>> call, Throwable t) {
                Log.e("API_ERRO", "Falha atendimentos: " + t.getMessage());
                aviso("Sem conexao com o backend para atendimentos");
            }
        });
    }

    // compara o id do cliente do atendimento com o cliente que abriu a tela
    private boolean ehDoCliente(OrdemServico o) {
        if (o.getCliente() == null) {
            return false;
        }
        return String.valueOf(o.getCliente().getId()).equals(String.valueOf(clienteId));
    }

    private long idDe(OrdemServico o) {
        return o.getId() == null ? 0L : o.getId();
    }

    private void abrirChat(OrdemServico ordem) {
        if (ordem.getId() == null) {
            aviso("Atendimento sem identificacao");
            return;
        }
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ordemServicoId", ordem.getId().longValue());
        intent.putExtra("usuarioAtual", usuarioAtual);
        intent.putExtra("tipoPerfil", "CLIENTE");
        startActivity(intent);
    }

    private void aviso(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
    }
}