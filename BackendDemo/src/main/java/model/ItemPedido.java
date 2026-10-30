package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

// Um item dentro de um pedido
@Entity
@Table(name = "itens_pedido")
@Data
public class ItemPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @JsonIgnore evita loop infinito no JSON (pedido -> itens -> pedido ...)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Pedido pedido;

    private Long produtoId;

    // copia do nome e do preco na hora da compra,
    // assim o pedido nao muda se o ADM editar o produto depois
    private String nomeProduto;
    private BigDecimal precoUnitario;

    private Integer quantidade;
    private BigDecimal subtotal;
}