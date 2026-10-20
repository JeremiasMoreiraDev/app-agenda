package com.example.demo.pedido;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PedidoResponse(
        Long id,
        String chaveConfirmacao,
        Instant criadoEm,
        BigDecimal total,
        List<Item> itens
) {

    public record Item(
            Long produtoId,
            String nomeProduto,
            BigDecimal precoUnitario,
            Integer quantidade,
            BigDecimal subtotal
    ) {
    }

    public static PedidoResponse de(Pedido pedido) {
        List<Item> itens = pedido.getItens().stream()
                .map(item -> new Item(
                        item.getProdutoId(),
                        item.getNomeProduto(),
                        item.getPrecoUnitario(),
                        item.getQuantidade(),
                        item.getSubtotal()
                ))
                .toList();

        return new PedidoResponse(
                pedido.getId(),
                pedido.getChaveConfirmacao(),
                pedido.getCriadoEm(),
                pedido.getTotal(),
                itens
        );
    }
}