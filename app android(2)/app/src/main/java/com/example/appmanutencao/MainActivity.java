package com.example.appmanutencao;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appmanutencao.api.RetrofitClient;
import com.example.appmanutencao.model.AgendamentoRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText editClienteId, editTipoAtendimento, editDataAgendada, editObservacoes;
    private Button btnSalvarAgendamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vinculando os componentes do layout XML
        editClienteId = findViewById(R.id.editClienteId);
        editTipoAtendimento = findViewById(R.id.editTipoAtendimento);
        editDataAgendada = findViewById(R.id.editDataAgendada);
        editObservacoes = findViewById(R.id.editObservacoes);
        btnSalvarAgendamento = findViewById(R.id.btnSalvarAgendamento);

        // Ação do botão para disparar o cadastro de manutenção
        btnSalvarAgendamento.setOnClickListener(v -> enviarAgendamento());
    }

    private void enviarAgendamento() {
        String clienteIdStr = editClienteId.getText().toString().trim();
        String tipo = editTipoAtendimento.getText().toString().trim();
        String data = editDataAgendada.getText().toString().trim();
        String obs = editObservacoes.getText().toString().trim();

        if (clienteIdStr.isEmpty() || tipo.isEmpty() || data.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        Long clienteId = Long.parseLong(clienteIdStr);

        // Cria o request com todos os parâmetros exigidos pelo Spring Boot (incluindo o serviço de manutenção ID 1L)
        AgendamentoRequest request = new AgendamentoRequest(clienteId, 1L, data, tipo, "PENDENTE", obs);

        RetrofitClient.getApiService().criarAgendamento(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Agendamento de manutenção realizado com sucesso!", Toast.LENGTH_LONG).show();
                    limparCampos();
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao salvar: Código " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void limparCampos() {
        editClienteId.setText("");
        editTipoAtendimento.setText("");
        editDataAgendada.setText("");
        editObservacoes.setText("");
    }
}