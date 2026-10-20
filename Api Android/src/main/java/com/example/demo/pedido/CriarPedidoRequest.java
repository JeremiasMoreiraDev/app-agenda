package com.example.demo.pedido;

import java.util.List;

public record CriarPedidoRequest(
        String chaveConfirmacao,
        List<Item> itens
) {

    public record Item(
            Long produtoId,
            Integer quantidade
    ) {
    }
}