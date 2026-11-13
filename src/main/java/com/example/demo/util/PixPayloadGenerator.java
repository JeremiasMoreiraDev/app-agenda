package com.example.demo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Gera o codigo "Pix copia e cola" (BR Code, padrao EMV do Banco Central).
 * Uso apenas para demonstracao: nao confirma pagamento, so monta o codigo.
 */
@Component
public class PixPayloadGenerator {

    private static final int MAX_NOME = 25;
    private static final int MAX_CIDADE = 15;
    private static final int MAX_TXID = 25;

    private final String chave;
    private final String nomeRecebedor;
    private final String cidade;

    public PixPayloadGenerator(
            @Value("${pix.chave:00000000-0000-0000-0000-000000000000}") String chave,
            @Value("${pix.nome-recebedor:Solar Manutencao}") String nomeRecebedor,
            @Value("${pix.cidade:Salvador}") String cidade) {
        this.chave = chave;
        this.nomeRecebedor = limpar(nomeRecebedor, MAX_NOME);
        this.cidade = limpar(cidade, MAX_CIDADE);
    }

    public String gerar(BigDecimal valor, String txid) {
        String contaPix = campo("00", "br.gov.bcb.pix") + campo("01", chave);
        String dadosAdicionais = campo("05", txidSeguro(txid));

        String payload = campo("00", "01")
                + campo("01", "12") // 12 = QR de uso unico
                + campo("26", contaPix)
                + campo("52", "0000")
                + campo("53", "986") // moeda: Real
                + campo("54", valor.setScale(2, RoundingMode.HALF_UP).toPlainString())
                + campo("58", "BR")
                + campo("59", nomeRecebedor)
                + campo("60", cidade)
                + campo("62", dadosAdicionais)
                + "6304";

        return payload + crc16(payload);
    }

    // formato de cada campo: ID + tamanho (2 digitos) + valor
    private String campo(String id, String valor) {
        return id + String.format("%02d", valor.length()) + valor;
    }

    // remove acentos, deixa maiusculo e corta no tamanho maximo
    private String limpar(String texto, int max) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9 ]", "")
                .toUpperCase(Locale.ROOT)
                .trim();
        return semAcento.length() > max ? semAcento.substring(0, max) : semAcento;
    }

    // o txid so aceita letras e numeros
    private String txidSeguro(String txid) {
        String limpo = txid.replaceAll("[^A-Za-z0-9]", "");
        return limpo.length() > MAX_TXID ? limpo.substring(0, MAX_TXID) : limpo;
    }

    // CRC16-CCITT (polinomio 0x1021, inicio 0xFFFF) exigido pelo padrao Pix
    private String crc16(String payload) {
        int crc = 0xFFFF;
        for (byte b : payload.getBytes(StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1;
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }
}