package com.example.catalogoprodutos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CadastroProdutoActivity extends AppCompatActivity {

    private static final String URL_PRODUTOS =
            "http://127.0.0.1:8080/api/produtos";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private EditText edtNome;
    private EditText edtCategoria;
    private EditText edtDescricao;
    private EditText edtPreco;
    private EditText edtEstoque;
    private EditText edtUrlImagem;

    private Button btnSalvarProduto;
    private TextView txtStatusCadastro;
    private boolean salvando = false;

    private long produtoId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_produto);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (view, insets) -> {
                    Insets barras = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    Insets teclado = insets.getInsets(
                            WindowInsetsCompat.Type.ime()
                    );

                    view.setPadding(
                            barras.left,
                            barras.top,
                            barras.right,
                            Math.max(barras.bottom, teclado.bottom)
                    );

                    return insets;
                }
        );

        edtNome = findViewById(R.id.edtNome);
        edtCategoria = findViewById(R.id.edtCategoria);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtPreco = findViewById(R.id.edtPreco);
        edtEstoque = findViewById(R.id.edtEstoque);
        edtUrlImagem = findViewById(R.id.edtUrlImagem);

        btnSalvarProduto = findViewById(R.id.btnSalvarProduto);
        txtStatusCadastro = findViewById(R.id.txtStatusCadastro);

        findViewById(R.id.btnVoltarCadastro).setOnClickListener(view -> {
            if (!salvando) {
                finish();
            }
        });

        btnSalvarProduto.setOnClickListener(view -> salvarProduto());
        produtoId = getIntent().getLongExtra("id", -1L);

        if (produtoId > 0) {
            Intent dados = getIntent();

            edtNome.setText(dados.getStringExtra("nome"));
            edtCategoria.setText(dados.getStringExtra("categoria"));
            edtDescricao.setText(dados.getStringExtra("descricao"));
            edtPreco.setText(dados.getStringExtra("preco"));
            edtEstoque.setText(String.valueOf(
                    dados.getIntExtra("quantidadeEstoque", 0)
            ));
            edtUrlImagem.setText(dados.getStringExtra("urlImagem"));

            btnSalvarProduto.setText("Salvar alterações");
        }
    }




    private String texto(EditText campo) {
        return campo.getText().toString().trim();
    }

    private void mostrarErro(EditText campo, String mensagem) {
        campo.setError(mensagem);
        campo.requestFocus();
    }

    private void salvarProduto() {
        if (salvando) {
            return;
        }

        txtStatusCadastro.setText("");

        String nome = texto(edtNome);
        String categoria = texto(edtCategoria);
        String descricao = texto(edtDescricao);
        String urlImagem = texto(edtUrlImagem);

        if (nome.isEmpty()) {
            mostrarErro(edtNome, "Informe o nome do produto.");
            return;
        }

        if (categoria.isEmpty()) {
            mostrarErro(edtCategoria, "Informe a categoria.");
            return;
        }

        // Aceita 500,00 ou 500.00, sem separador de milhares.
        String valorDigitado = texto(edtPreco);

        if (!valorDigitado.matches("\\d+([.,]\\d{1,2})?")) {
            mostrarErro(
                    edtPreco,
                    "Use um valor como 500,00, sem pontos de milhar."
            );
            return;
        }

        BigDecimal preco =
                new BigDecimal(valorDigitado.replace(',', '.'));

        if (preco.compareTo(new BigDecimal("9999999999.99")) > 0) {
            mostrarErro(edtPreco, "O preço informado é muito alto.");
            return;
        }

        int quantidade;

        try {
            quantidade = Integer.parseInt(texto(edtEstoque));

            if (quantidade < 0) {
                mostrarErro(edtEstoque, "O estoque não pode ser negativo.");
                return;
            }
        } catch (NumberFormatException erro) {
            mostrarErro(
                    edtEstoque,
                    "Informe uma quantidade inteira válida, como 10."
            );
            return;
        }

        if (!urlImagem.isEmpty()) {
            try {
                URL endereco = new URL(urlImagem);

                if (!"https".equalsIgnoreCase(endereco.getProtocol())
                        || endereco.getHost().isEmpty()) {
                    mostrarErro(
                            edtUrlImagem,
                            "Informe um link HTTPS válido ou deixe vazio."
                    );
                    return;
                }
            } catch (Exception erro) {
                mostrarErro(edtUrlImagem, "O link da imagem é inválido.");
                return;
            }
        }

        JSONObject dados = new JSONObject();

        try {
            dados.put("nome", nome);
            dados.put("categoria", categoria);
            dados.put("descricao", descricao);
            dados.put("preco", preco);
            dados.put("quantidadeEstoque", quantidade);
            dados.put("urlImagem", urlImagem);
        } catch (Exception erro) {
            Log.e("Catalogo", "Erro ao preparar cadastro", erro);
            txtStatusCadastro.setText(
                    "Não foi possível preparar os dados do produto."
            );
            return;
        }

        salvando = true;
        btnSalvarProduto.setEnabled(false);
        txtStatusCadastro.setText("Salvando produto...");

        executor.execute(() -> {
            try {
                enviarProduto(dados);

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    Toast.makeText(
                            this,
                            produtoId > 0
                                    ? "Produto atualizado com sucesso!"
                                    : "Produto cadastrado com sucesso!",
                            Toast.LENGTH_LONG
                    ).show();

                    Intent resultado = new Intent();
                    resultado.putExtra("nome", nome);
                    resultado.putExtra("categoria", categoria);
                    resultado.putExtra("descricao", descricao);
                    resultado.putExtra("preco", preco.toPlainString());
                    resultado.putExtra("quantidadeEstoque", quantidade);
                    resultado.putExtra("urlImagem", urlImagem);

                    setResult(RESULT_OK, resultado);
                    finish();
                });

            } catch (Exception erro) {
                Log.e("Catalogo", "Erro ao cadastrar produto", erro);

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    salvando = false;
                    btnSalvarProduto.setEnabled(true);

                    txtStatusCadastro.setText(
                            "Não foi possível confirmar o cadastro. "
                                    + "Confira a conexão e a lista de produtos "
                                    + "antes de tentar novamente.\n"
                                    + erro.getMessage()
                    );
                });
            }
        });
    }

    private void enviarProduto(JSONObject dados) throws IOException {
        boolean editando = produtoId > 0;

        String endereco = editando
                ? URL_PRODUTOS + "/" + produtoId
                : URL_PRODUTOS;

        HttpURLConnection conexao = (HttpURLConnection)
                new URL(endereco).openConnection();

        try {
            conexao.setRequestMethod(editando ? "PUT" : "POST");
            conexao.setConnectTimeout(10000);
            conexao.setReadTimeout(10000);
            conexao.setDoOutput(true);

            conexao.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );
            conexao.setRequestProperty("Accept", "application/json");

            byte[] corpo = dados.toString()
                    .getBytes(StandardCharsets.UTF_8);

            conexao.setFixedLengthStreamingMode(corpo.length);

            try (OutputStream saida = conexao.getOutputStream()) {
                saida.write(corpo);
            }

            int status = conexao.getResponseCode();

            int esperado = editando
                    ? HttpURLConnection.HTTP_OK
                    : HttpURLConnection.HTTP_CREATED;

            if (status != esperado) {
                throw new IOException("Resposta da API: HTTP " + status);
            }
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