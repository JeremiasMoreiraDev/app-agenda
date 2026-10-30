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
import com.example.appmanutencao.model.Cliente;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilActivity extends AppCompatActivity {

    private static final String[] TIPOS_IMOVEL = {"Residencial", "Comercial", "Rural", "Usina"};
    private static final String[] SIM_NAO = {"Sim", "Nao"};

    private EditText editNome, editTelefone, editEmail;
    private Spinner spinnerImovel, spinnerCarro, spinnerBateria;
    private ApiService api;
    private Cliente clienteAtual;
    private String emailUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Esta tela é do cliente: o ADMIN não entra aqui
        if ("ADMIN".equalsIgnoreCase(getIntent().getStringExtra("tipoPerfil"))) {
            Toast.makeText(this, "Tela disponivel apenas para clientes", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_perfil);

        emailUsuario = getIntent().getStringExtra("usuarioAtual");

        editNome = findViewById(R.id.editNomePerfil);
        editTelefone = findViewById(R.id.editTelefonePerfil);
        editEmail = findViewById(R.id.editEmailPerfil);
        spinnerImovel = findViewById(R.id.spinnerImovel);
        spinnerCarro = findViewById(R.id.spinnerCarro);
        spinnerBateria = findViewById(R.id.spinnerBateria);

        preencherSpinner(spinnerImovel, TIPOS_IMOVEL);
        preencherSpinner(spinnerCarro, SIM_NAO);
        preencherSpinner(spinnerBateria, SIM_NAO);

        api = RetrofitClient.getApiService();

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnSalvarPerfil).setOnClickListener(v -> salvarPerfil());

        carregarPerfil();
    }

    private void preencherSpinner(Spinner spinner, String[] opcoes) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, opcoes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private int posicaoTipoImovel(String valor) {
        if (valor != null) {
            for (int i = 0; i < TIPOS_IMOVEL.length; i++) {
                if (TIPOS_IMOVEL[i].equalsIgnoreCase(valor.trim())) {
                    return i;
                }
            }
        }
        return 0;
    }

    private int posicaoSimNao(String valor) {
        if (valor != null && (valor.equalsIgnoreCase("sim") || valor.equalsIgnoreCase("true"))) {
            return 0;
        }
        return 1;
    }

    private void carregarPerfil() {
        Log.d("PERFIL_DEBUG", "Buscando perfil para o e-mail: " + emailUsuario);

        api.listarClientes().enqueue(new Callback<List<Cliente>>() {
            @Override
            public void onResponse(Call<List<Cliente>> call, Response<List<Cliente>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Cliente> clientes = response.body();

                    for (Cliente c : clientes) {
                        if (c.getEmail() != null && emailUsuario != null
                                && c.getEmail().trim().equalsIgnoreCase(emailUsuario.trim())) {
                            clienteAtual = c;
                            break;
                        }
                    }

                    // Fallback de segurança: se o e-mail exato não bateu, pega o primeiro cliente
                    if (clienteAtual == null && !clientes.isEmpty()) {
                        Log.w("PERFIL_DEBUG", "E-mail exato não bateu. Usando o primeiro cliente da lista por segurança.");
                        clienteAtual = clientes.get(0);
                    }

                    if (clienteAtual == null) {
                        Toast.makeText(PerfilActivity.this,
                                "Nenhum cadastro encontrado para este e-mail",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    mostrarDados();
                } else {
                    Log.e("API_ERRO", "Erro perfil. Codigo: " + response.code());
                    Toast.makeText(PerfilActivity.this,
                            "Erro ao carregar perfil (codigo " + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Cliente>> call, Throwable t) {
                Log.e("API_ERRO", "Falha perfil: " + t.getMessage());
                Toast.makeText(PerfilActivity.this, "Sem conexao com o backend", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDados() {
        if (clienteAtual != null) {
            editNome.setText(clienteAtual.getNome());
            editTelefone.setText(clienteAtual.getTelefone());
            editEmail.setText(clienteAtual.getEmail());
            spinnerImovel.setSelection(posicaoTipoImovel(clienteAtual.getTipoImovel()));
            spinnerCarro.setSelection(posicaoSimNao(clienteAtual.getPossuiCarroEletrico()));
            spinnerBateria.setSelection(posicaoSimNao(clienteAtual.getPossuiBateria()));
        }
    }

    private void salvarPerfil() {
        if (clienteAtual == null) {
            Toast.makeText(this, "Perfil ainda nao carregado", Toast.LENGTH_SHORT).show();
            return;
        }

        String nome = editNome.getText().toString().trim();
        if (nome.isEmpty()) {
            Toast.makeText(this, "Informe o nome", Toast.LENGTH_SHORT).show();
            return;
        }

        clienteAtual.setNome(nome);
        clienteAtual.setTelefone(editTelefone.getText().toString().trim());
        clienteAtual.setTipoImovel(TIPOS_IMOVEL[spinnerImovel.getSelectedItemPosition()]);
        clienteAtual.setPossuiCarroEletrico(SIM_NAO[spinnerCarro.getSelectedItemPosition()]);
        clienteAtual.setPossuiBateria(SIM_NAO[spinnerBateria.getSelectedItemPosition()]);

        api.atualizarCliente(clienteAtual.getId(), clienteAtual).enqueue(new Callback<Cliente>() {
            @Override
            public void onResponse(Call<Cliente> call, Response<Cliente> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PerfilActivity.this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("API_ERRO", "Erro ao salvar perfil. Codigo: " + response.code());
                    Toast.makeText(PerfilActivity.this,
                            "Erro ao salvar perfil (codigo " + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Cliente> call, Throwable t) {
                Log.e("API_ERRO", "Falha ao salvar perfil: " + t.getMessage());
                Toast.makeText(PerfilActivity.this, "Erro de conexao", Toast.LENGTH_SHORT).show();
            }
        });
    }
}