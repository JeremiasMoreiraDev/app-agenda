package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clienteId;

    private BigDecimal valorTotal;

    private LocalDateTime dataPedido;
    private String status; // Pendente, Confirmado, Entregue, Cancelado

    // situacao do pagamento (separada do status do pedido)
    @Enumerated(EnumType.STRING)
    private StatusPagamento statusPagamento = StatusPagamento.AGUARDANDO_PAGAMENTO;

    private LocalDateTime dataPagamento;

    // produtos comprados neste pedido
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ItemPedido> itens = new ArrayList<>();

    // liga o item ao pedido nos dois lados
    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        itens.add(item);
    }
}