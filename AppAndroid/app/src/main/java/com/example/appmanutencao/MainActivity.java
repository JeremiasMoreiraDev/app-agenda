package com.example.appmanutencao;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appmanutencao.api.ApiService;
import com.example.appmanutencao.api.RetrofitClient;
import com.example.appmanutencao.model.AgendamentoRequest;
import com.example.appmanutencao.model.Cliente;
import com.example.appmanutencao.model.OrdemServico;
import com.example.appmanutencao.model.Servico;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerCliente, spinnerServico;
    private EditText editData, editTipo, editObs;
    private List<Cliente> clientes = new ArrayList<>();
    private List<Servico> servicos = new ArrayList<>();
    private ApiService api;
    private String usuarioAtual = "CLIENTE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ("ADMIN".equalsIgnoreCase(getIntent().getStringExtra("tipoPerfil"))) {
            Intent intent = new Intent(this, OrdemServicoActivity.class);
            intent.putExtra("tipoPerfil", "ADMIN");
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        if (getIntent().hasExtra("usuarioAtual")) {
            usuarioAtual = getIntent().getStringExtra("usuarioAtual");
        }

        spinnerCliente = findViewById(R.id.spinnerCliente);
        spinnerServico = findViewById(R.id.spinnerServico);
        editData = findViewById(R.id.editDataAgendada);
        editTipo = findViewById(R.id.editTipoAtendimento);
        editObs = findViewById(R.id.editObservacoes);

        api = RetrofitClient.getApiService();

        findViewById(R.id.btnVoltar).setOnClickListener(v -> irParaLogin());
        findViewById(R.id.btnSair).setOnClickListener(v -> irParaLogin());
        findViewById(R.id.btnPerfil).setOnClickListener(v -> abrirPerfil());
        findViewById(R.id.btnSalvarAgendamento).setOnClickListener(v -> criarAgendamento());
        findViewById(R.id.btnProdutos).setOnClickListener(v -> abrirProdutos());
        findViewById(R.id.btnAtendimentos).setOnClickListener(v -> abrirAtendimentos());

        carregarClientes();
        carregarServicos();
    }

    private void irParaLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void abrirPerfil() {
        Intent intent = new Intent(this, PerfilActivity.class);
        intent.putExtra("usuarioAtual", usuarioAtual);
        intent.putExtra("tipoPerfil", "CLIENTE");
        startActivity(intent);
    }

    // Abre a tela de produtos com o cliente que esta escolhido no spinner
    private void abrirProdutos() {
        int posCliente = spinnerCliente.getSelectedItemPosition();
        if (clientes.isEmpty() || posCliente < 0) {
            aviso("Selecione um cliente antes de ver os produtos");
            return;
        }

        long clienteId = clientes.get(posCliente).getId();

        Intent intent = new Intent(this, ProdutoClienteActivity.class);
        intent.putExtra("clienteId", clienteId);
        startActivity(intent);
    }

    // Abre a lista de atendimentos (e os chats) do cliente escolhido no spinner
    private void abrirAtendimentos() {
        int posCliente = spinnerCliente.getSelectedItemPosition();
        if (clientes.isEmpty() || posCliente < 0) {
            aviso("Selecione um cliente antes de ver os atendimentos");
            return;
        }

        long clienteId = clientes.get(posCliente).getId();

        Intent intent = new Intent(this, AtendimentoClienteActivity.class);
        intent.putExtra("clienteId", clienteId);
        intent.putExtra("usuarioAtual", usuarioAtual);
        startActivity(intent);
    }

    private void aviso(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
    }

    private void preencher(Spinner spinner, List<String> itens) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, itens);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private String converterData(String texto) {
        texto = texto.trim();
        if (texto.length() == 10) {
            texto = texto + " 08:00";
        }
        try {
            SimpleDateFormat entrada = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            entrada.setLenient(false);
            Date data = entrada.parse(texto);
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(data);
        } catch (Exception e) {
            return null;
        }
    }

    private void carregarClientes() {
        api.listarClientes().enqueue(new Callback<List<Cliente>>() {
            @Override
            public void onResponse(Call<List<Cliente>> call, Response<List<Cliente>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    clientes = response.body();
                    List<String> nomes = new ArrayList<>();
                    for (Cliente c : clientes) {
                        nomes.add(c.getNome() != null ? c.getNome() : "Cliente " + c.getId());
                    }
                    preencher(spinnerCliente, nomes);
                } else {
                    aviso("Erro ao carregar clientes (codigo " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<Cliente>> call, Throwable t) {
                Log.e("API_ERRO", "Falha clientes: " + t.getMessage());
                aviso("Sem conexao com o backend para clientes");
            }
        });
    }

    private void carregarServicos() {
        api.listarServicos().enqueue(new Callback<List<Servico>>() {
            @Override
            public void onResponse(Call<List<Servico>> call, Response<List<Servico>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    servicos = response.body();
                    List<String> titulos = new ArrayList<>();
                    for (Servico s : servicos) {
                        titulos.add(s.getTitulo() != null ? s.getTitulo() : "Servico " + s.getId());
                    }
                    preencher(spinnerServico, titulos);
                } else {
                    aviso("Erro ao carregar servicos (codigo " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<Servico>> call, Throwable t) {
                Log.e("API_ERRO", "Falha servicos: " + t.getMessage());
                aviso("Sem conexao com o backend para servicos");
            }
        });
    }

    private void criarAgendamento() {
        if (clientes.isEmpty() || servicos.isEmpty()) {
            aviso("Aguarde o carregamento ou verifique o backend...");
            return;
        }

        int posCliente = spinnerCliente.getSelectedItemPosition();
        int posServico = spinnerServico.getSelectedItemPosition();
        if (posCliente < 0 || posServico < 0) {
            aviso("Selecione um cliente e um servico validos");
            return;
        }

        String dataDigitada = editData.getText().toString().trim();
        String tipo = editTipo.getText().toString().trim();
        String obs = editObs.getText().toString().trim();

        if (dataDigitada.isEmpty() || tipo.isEmpty()) {
            aviso("Preencha a data e o tipo de atendimento");
            return;
        }

        String dataApi = converterData(dataDigitada);
        if (dataApi == null) {
            aviso("Data invalida. Use o formato 25/10/2026 14:30");
            return;
        }

        AgendamentoRequest request = new AgendamentoRequest(
                clientes.get(posCliente).getId(),
                servicos.get(posServico).getId(),
                null,
                dataApi,
                tipo,
                "PENDENTE",
                obs
        );

        api.criarAgendamento(request).enqueue(new Callback<OrdemServico>() {
            @Override
            public void onResponse(Call<OrdemServico> call, Response<OrdemServico> response) {
                if (response.isSuccessful() && response.body() != null) {
                    aviso("Agendamento criado com sucesso!");
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("ordemServicoId", response.body().getId());
                    intent.putExtra("usuarioAtual", usuarioAtual);
                    intent.putExtra("tipoPerfil", "CLIENTE");
                    startActivity(intent);
                } else {
                    Log.e("API_ERRO", "Erro criar agendamento. Codigo: " + response.code());
                    aviso("Erro ao criar agendamento (codigo " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<OrdemServico> call, Throwable t) {
                aviso("Falha na requisicao: " + t.getMessage());
            }
        });
    }
}