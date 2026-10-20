package com.example.demo.pedido;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos_catalogo")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "chave_confirmacao",
            nullable = false,
            unique = true,
            length = 36,
            updatable = false
    )
    private String chaveConfirmacao;

    @Column(
            name = "resumo_requisicao",
            nullable = false,
            length = 64,
            updatable = false
    )
    private String resumoRequisicao;

    @Column(
            name = "criado_em",
            nullable = false,
            updatable = false
    )
    private Instant criadoEm;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal total;

    @ElementCollection
    @CollectionTable(
            name = "itens_pedido_catalogo",
            joinColumns = @JoinColumn(name = "pedido_id")
    )
    @OrderColumn(name = "posicao")
    private List<ItemPedido> itens = new ArrayList<>();
}