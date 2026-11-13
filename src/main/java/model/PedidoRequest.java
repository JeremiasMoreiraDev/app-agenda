package com.example.demo.model;

import lombok.Data;

import java.util.List;

// O que o app envia ao finalizar a compra
@Data
public class PedidoRequest {
    private Long clienteId;

    // carrinho: varios produtos num pedido so
    private List<ItemPedidoRequest> itens;

    // compatibilidade com a versao antiga do app (um produto por compra).
    // Pode ser removido depois que o app novo estiver pronto.
    private Long produtoId;
    private Integer quantidade;
}