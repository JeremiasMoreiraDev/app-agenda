package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_ordem_servico")
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Column(nullable = false)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOs status = StatusOs.PENDENTE;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    private Double valorTotal;

    public OrdemServico() {}

    public OrdemServico(Cliente cliente, Servico servico, String observacoes, Double valorTotal) {
        this.cliente = cliente;
        this.servico = servico;
        this.observacoes = observacoes;
        this.valorTotal = valorTotal;
        this.status = StatusOs.PENDENTE;
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public StatusOs getStatus() { return status; }
    public void setStatus(StatusOs status) { this.status = status; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public Double getValorTotal() { return valorTotal; }
    public void setValorTotal(Double valorTotal) { this.valorTotal = valorTotal; }
}