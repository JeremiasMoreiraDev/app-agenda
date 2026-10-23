package com.example.demo.pedido;

import java.math.BigDecimal;
import java.util.List;

public record CriarPedidoRequest(
        String chaveConfirmacao,
        List<Item> itens,
        BigDecimal totalEsperado
) {

    public record Item(
            Long produtoId,
            Integer quantidade
    ) {
    }
}