
package com.example.appmanutencao;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmanutencao.adapter.OrdemServicoAdapter;
import com.example.appmanutencao.api.ApiService;
import com.example.appmanutencao.api.RetrofitClient;
import com.example.appmanutencao.model.OrdemServico;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdemServicoActivity extends AppCompatActivity {

    private static final String TAG = "OrdemServicoActivity";

    private Button btnFiltroTodas;
    private Button btnFiltroPendente;
    private Button btnFiltroConfirmado;
    private Button btnFiltroConcluido;
    private Button btnGerenciarProdutos;
    private Button btnSair;

    private RecyclerView recyclerViewOrdens;
    private OrdemServicoAdapter adapter;

    private List<OrdemServico> listaTodasOrdens = new ArrayList<>();

    private ApiService apiService;
    private String statusAtual = "PENDENTE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String tipoPerfil = getIntent().getStringExtra("tipoPerfil");

        if (!"ADMIN".equalsIgnoreCase(tipoPerfil)) {
            Toast.makeText(
                    this,
                    "Acesso restrito ao administrador",
                    Toast.LENGTH_SHORT
            ).show();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_ordem_servico);

        btnFiltroTodas = findViewById(R.id.btnFiltroTodas);
        btnFiltroPendente = findViewById(R.id.btnFiltroPendente);
        btnFiltroConfirmado = findViewById(R.id.btnFiltroConfirmado);
        btnFiltroConcluido = findViewById(R.id.btnFiltroConcluido);
        btnGerenciarProdutos = findViewById(R.id.btnGerenciarProdutos);
        btnSair = findViewById(R.id.btnSair);
        recyclerViewOrdens = findViewById(R.id.recyclerViewOrdens);

        recyclerViewOrdens.setLayoutManager(
                new LinearLayoutManager(this)
        );

        // O botão de alterar status não abre mais o chat.
        adapter = new OrdemServicoAdapter(this::alterarStatus);
        recyclerViewOrdens.setAdapter(adapter);

        apiService = RetrofitClient.getApiService();

        btnFiltroTodas.setOnClickListener(
                v -> filtrarPorStatus("TODAS")
        );

        btnFiltroPendente.setOnClickListener(
                v -> filtrarPorStatus("PENDENTE")
        );

        btnFiltroConfirmado.setOnClickListener(
                v -> filtrarPorStatus("CONFIRMADO")
        );

        btnFiltroConcluido.setOnClickListener(
                v -> filtrarPorStatus("CONCLUIDO")
        );

        btnGerenciarProdutos.setOnClickListener(v -> {
            Intent intent = new Intent(
                    OrdemServicoActivity.this,
                    ProdutoActivity.class
            );

            intent.putExtra("tipoPerfil", "ADMIN");
            startActivity(intent);
        });

        btnSair.setOnClickListener(v -> {
            Intent intent = new Intent(
                    OrdemServicoActivity.this,
                    LoginActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (apiService != null) {
            carregarOrdensServico();
        }
    }

    private void carregarOrdensServico() {
        apiService.listarAgendamentos().enqueue(
                new Callback<List<OrdemServico>>() {

                    @Override
                    public void onResponse(
                            Call<List<OrdemServico>> call,
                            Response<List<OrdemServico>> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null) {

                            listaTodasOrdens = new ArrayList<>(
                                    response.body()
                            );

                            filtrarPorStatus(statusAtual);

                        } else {
                            Log.e(
                                    TAG,
                                    "Erro ao carregar ordens. HTTP "
                                            + response.code()
                            );

                            Toast.makeText(
                                    OrdemServicoActivity.this,
                                    "Erro ao carregar ordens. Código: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<OrdemServico>> call,
                            Throwable t
                    ) {
                        Log.e(TAG, "Falha ao carregar ordens", t);

                        Toast.makeText(
                                OrdemServicoActivity.this,
                                "Erro de conexão ao carregar ordens",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void filtrarPorStatus(String status) {
        statusAtual = status;

        if ("TODAS".equalsIgnoreCase(status)) {
            adapter.setLista(listaTodasOrdens);
            return;
        }

        List<OrdemServico> filtradas = new ArrayList<>();

        for (OrdemServico ordem : listaTodasOrdens) {
            if (ordem.getStatus() != null
                    && ordem.getStatus().equalsIgnoreCase(status)) {
                filtradas.add(ordem);
            }
        }

        adapter.setLista(filtradas);
    }

    private void alterarStatus(OrdemServico ordem) {
        if (ordem == null || ordem.getId() == null) {
            Toast.makeText(
                    this,
                    "Não foi possível identificar a ordem",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String[] statusDisponiveis = {
                "PENDENTE",
                "CONFIRMADO",
                "CONCLUIDO"
        };

        new AlertDialog.Builder(this)
                .setTitle("Alterar status da ordem")
                .setItems(statusDisponiveis, (dialog, which) -> {
                    String novoStatus = statusDisponiveis[which];

                    if (novoStatus.equalsIgnoreCase(ordem.getStatus())) {
                        Toast.makeText(
                                OrdemServicoActivity.this,
                                "A ordem já possui esse status",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    enviarAtualizacaoStatus(ordem, novoStatus);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void enviarAtualizacaoStatus(
            OrdemServico ordem,
            String novoStatus
    ) {
        apiService.atualizarStatusAgendamento(
                ordem.getId(),
                novoStatus
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response
            ) {
                if (response.isSuccessful()) {
                    Toast.makeText(
                            OrdemServicoActivity.this,
                            "Status atualizado para " + novoStatus,
                            Toast.LENGTH_SHORT
                    ).show();

                    carregarOrdensServico();

                } else {
                    Log.e(
                            TAG,
                            "Erro ao atualizar status. HTTP "
                                    + response.code()
                    );

                    Toast.makeText(
                            OrdemServicoActivity.this,
                            "Erro ao atualizar status. Código: "
                                    + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable t
            ) {
                Log.e(TAG, "Falha ao atualizar status", t);

                Toast.makeText(
                        OrdemServicoActivity.this,
                        "Falha de conexão ao atualizar status",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
