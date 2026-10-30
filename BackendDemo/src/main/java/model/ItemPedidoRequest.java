package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Um item do carrinho enviado pelo app: qual produto e quantas unidades
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoRequest {
    private Long produtoId;
    private Integer quantidade;
}