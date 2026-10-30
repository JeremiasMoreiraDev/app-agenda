
package com.example.appmanutencao;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmanutencao.adapter.ChatAdapter;
import com.example.appmanutencao.api.ApiService;
import com.example.appmanutencao.api.RetrofitClient;
import com.example.appmanutencao.model.Mensagem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerChat;
    private EditText editMensagem;
    private Button btnEnviar;
    private Button btnVoltar;

    private ChatAdapter chatAdapter;
    private List<Mensagem> listaMensagens = new ArrayList<>();

    private Long ordemServicoId;
    private String usuarioAtual = "CLIENTE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Recebe o ID da ordem de serviço
        long idRecebido = getIntent().getLongExtra(
                "ordemServicoId", -1L
        );

        if (idRecebido == -1L) {
            Toast.makeText(
                    this,
                    "Ordem de serviço não encontrada",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        ordemServicoId = idRecebido;

        // Identifica o perfil do usuário
        String perfil = getIntent().getStringExtra("tipoPerfil");

        if ("ADMIN".equalsIgnoreCase(perfil)) {
            usuarioAtual = "ADMIN";
        } else {
            usuarioAtual = "CLIENTE";
        }

        // Inicializa os componentes
        recyclerChat = findViewById(R.id.recyclerChat);
        editMensagem = findViewById(R.id.editMensagem);
        btnEnviar = findViewById(R.id.btnEnviar);
        btnVoltar = findViewById(R.id.btnVoltar);

        // Botão Voltar
        btnVoltar.setOnClickListener(v -> finish());

        // Configura a lista de mensagens
        recyclerChat.setLayoutManager(
                new LinearLayoutManager(this)
        );

        chatAdapter = new ChatAdapter(
                listaMensagens,
                usuarioAtual
        );

        recyclerChat.setAdapter(chatAdapter);

        // Carrega as mensagens
        carregarMensagens();

        // Botão Enviar
        btnEnviar.setOnClickListener(v -> enviarMensagem());
    }

    private void carregarMensagens() {

        ApiService apiService = RetrofitClient.getApiService();

        apiService.listarMensagens(ordemServicoId)
                .enqueue(new Callback<List<Mensagem>>() {

                    @Override
                    public void onResponse(
                            Call<List<Mensagem>> call,
                            Response<List<Mensagem>> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null) {

                            listaMensagens = response.body();

                            chatAdapter.atualizarMensagens(
                                    listaMensagens
                            );

                            if (!listaMensagens.isEmpty()) {
                                recyclerChat.scrollToPosition(
                                        listaMensagens.size() - 1
                                );
                            }

                        } else {
                            Log.e(
                                    "API_ERRO",
                                    "Erro ao carregar mensagens. Código: "
                                            + response.code()
                            );

                            Toast.makeText(
                                    ChatActivity.this,
                                    "Erro ao carregar chat (código "
                                            + response.code() + ")",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<Mensagem>> call,
                            Throwable t
                    ) {
                        Log.e(
                                "API_ERRO",
                                "Falha chat: " + t.getMessage()
                        );

                        Toast.makeText(
                                ChatActivity.this,
                                "Erro ao carregar chat",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void enviarMensagem() {

        String texto = editMensagem.getText()
                .toString()
                .trim();

        if (texto.isEmpty()) {
            return;
        }

        Mensagem novaMensagem = new Mensagem(
                ordemServicoId,
                usuarioAtual,
                texto
        );

        ApiService apiService = RetrofitClient.getApiService();

        apiService.enviarMensagem(novaMensagem)
                .enqueue(new Callback<Mensagem>() {

                    @Override
                    public void onResponse(
                            Call<Mensagem> call,
                            Response<Mensagem> response
                    ) {
                        if (response.isSuccessful()) {

                            editMensagem.setText("");

                            carregarMensagens();

                        } else {
                            Log.e(
                                    "API_ERRO",
                                    "Erro ao enviar mensagem. Código: "
                                            + response.code()
                            );

                            Toast.makeText(
                                    ChatActivity.this,
                                    "Falha ao enviar mensagem (código "
                                            + response.code() + ")",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Mensagem> call,
                            Throwable t
                    ) {
                        Log.e(
                                "API_ERRO",
                                "Falha envio: " + t.getMessage()
                        );

                        Toast.makeText(
                                ChatActivity.this,
                                "Erro de conexão",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
