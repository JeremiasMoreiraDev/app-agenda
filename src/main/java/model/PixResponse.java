package com.example.demo.model;

import java.math.BigDecimal;

// Resposta do endpoint de Pix: o app gera o QR code a partir do codigoCopiaECola
public record PixResponse(
        Long pedidoId,
        BigDecimal valorTotal,
        String codigoCopiaECola
) {
}