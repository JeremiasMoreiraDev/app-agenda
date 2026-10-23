package com.example.demo.pedido;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ItemPedido {

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "nome_produto", nullable = false, length = 150)
    private String nomeProduto;

    @Column(
            name = "preco_unitario",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal precoUnitario;

    @Column(nullable = false)
    private Integer quantidade;

    @Transient
    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(
                BigDecimal.valueOf(quantidade)
        );
    }
}