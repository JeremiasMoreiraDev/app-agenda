package com.example.appmanutencao;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appmanutencao.api.ApiService;
import com.example.appmanutencao.api.RetrofitClient;
import com.example.appmanutencao.model.Pedido;
import com.example.appmanutencao.model.PixResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Tela de pagamento via Pix (DEMONSTRACAO: o pagamento e confirmado pelo botao "Ja paguei")
public class PagamentoActivity extends AppCompatActivity {

    // chave usada para enviar o id do pedido para esta tela
    public static final String EXTRA_PEDIDO_ID = "pedidoId";

    private static final int TAMANHO_QR_CODE = 600;

    private TextView textValor, textCodigoPix;
    private ImageView imgQrCode;
    private ProgressBar progressPagamento;
    private Button btnCopiar, btnJaPaguei, btnVoltar;

    private ApiService apiService;
    private long pedidoId;
    private String codigoPix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pedidoId = getIntent().getLongExtra(EXTRA_PEDIDO_ID, -1);
        if (pedidoId == -1) {
            Toast.makeText(this, "Pedido nao informado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_pagamento);

        textValor = findViewById(R.id.textValor);
        textCodigoPix = findViewById(R.id.textCodigoPix);
        imgQrCode = findViewById(R.id.imgQrCode);
        progressPagamento = findViewById(R.id.progressPagamento);
        btnCopiar = findViewById(R.id.btnCopiar);
        btnJaPaguei = findViewById(R.id.btnJaPaguei);
        btnVoltar = findViewById(R.id.btnVoltar);

        apiService = RetrofitClient.getApiService();

        btnVoltar.setOnClickListener(v -> finish());
        btnCopiar.setOnClickListener(v -> copiarCodigo());
        btnJaPaguei.setOnClickListener(v -> confirmarPagamento());

        carregarPix();
    }

    private void carregarPix() {
        progressPagamento.setVisibility(View.VISIBLE);

        apiService.gerarPix(pedidoId).enqueue(new Callback<PixResponse>() {
            @Override
            public void onResponse(Call<PixResponse> call, Response<PixResponse> response) {
                progressPagamento.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    mostrarPix(response.body());
                } else {
                    Log.e("API_ERRO", "Erro ao gerar Pix. Codigo: " + response.code());
                    Toast.makeText(PagamentoActivity.this,
                            mensagemDeErro(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PixResponse> call, Throwable t) {
                progressPagamento.setVisibility(View.GONE);
                Log.e("API_ERRO", "Falha ao gerar Pix: " + t.getMessage());
                Toast.makeText(PagamentoActivity.this,
                        "Sem conexao com o backend", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarPix(PixResponse pix) {
        codigoPix = pix.getCodigoCopiaECola();

        if (pix.getValorTotal() != null) {
            textValor.setText(formatarMoeda(pix.getValorTotal()));
        }
        textCodigoPix.setText(codigoPix);

        try {
            imgQrCode.setImageBitmap(gerarQrCode(codigoPix));
            imgQrCode.setVisibility(View.VISIBLE);
        } catch (WriterException e) {
            Log.e("PIX_ERRO", "Erro ao gerar QR code: " + e.getMessage());
            Toast.makeText(this, "Nao foi possivel gerar o QR code", Toast.LENGTH_SHORT).show();
        }

        btnCopiar.setEnabled(true);
        btnJaPaguei.setEnabled(true);
    }

    // Transforma o texto do Pix em uma imagem de QR code
    private Bitmap gerarQrCode(String texto) throws WriterException {
        BitMatrix matriz = new QRCodeWriter()
                .encode(texto, BarcodeFormat.QR_CODE, TAMANHO_QR_CODE, TAMANHO_QR_CODE);

        int[] pixels = new int[TAMANHO_QR_CODE * TAMANHO_QR_CODE];
        for (int y = 0; y < TAMANHO_QR_CODE; y++) {
            for (int x = 0; x < TAMANHO_QR_CODE; x++) {
                pixels[y * TAMANHO_QR_CODE + x] = matriz.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(TAMANHO_QR_CODE, TAMANHO_QR_CODE, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, TAMANHO_QR_CODE, 0, 0, TAMANHO_QR_CODE, TAMANHO_QR_CODE);
        return bitmap;
    }

    private void copiarCodigo() {
        if (codigoPix == null) {
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Pix copia e cola", codigoPix));
        Toast.makeText(this, "Codigo Pix copiado!", Toast.LENGTH_SHORT).show();
    }

    // DEMONSTRACAO: simula a confirmacao do pagamento
    private void confirmarPagamento() {
        btnJaPaguei.setEnabled(false);
        progressPagamento.setVisibility(View.VISIBLE);

        apiService.confirmarPagamento(pedidoId).enqueue(new Callback<Pedido>() {
            @Override
            public void onResponse(Call<Pedido> call, Response<Pedido> response) {
                progressPagamento.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(PagamentoActivity.this,
                            "Pagamento confirmado!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Log.e("API_ERRO", "Erro ao confirmar pagamento. Codigo: " + response.code());
                    btnJaPaguei.setEnabled(true);
                    Toast.makeText(PagamentoActivity.this,
                            mensagemDeErro(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Pedido> call, Throwable t) {
                progressPagamento.setVisibility(View.GONE);
                btnJaPaguei.setEnabled(true);
                Log.e("API_ERRO", "Falha ao confirmar pagamento: " + t.getMessage());
                Toast.makeText(PagamentoActivity.this,
                        "Erro de conexao", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Le a mensagem de erro que a API devolveu (ex.: "Pedido ja foi pago")
    private String mensagemDeErro(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String corpo = response.errorBody().string();
                if (!corpo.isEmpty()) {
                    return corpo;
                }
            }
        } catch (IOException e) {
            Log.e("API_ERRO", "Erro ao ler resposta de erro: " + e.getMessage());
        }
        return "Erro (codigo " + response.code() + ")";
    }

    private String formatarMoeda(double valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }
}