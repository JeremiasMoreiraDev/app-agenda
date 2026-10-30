package com.example.appmanutencao.model;

// Pedido como a API devolve depois da compra
public class Pedido {
    private Long id;
    private Long clienteId;
    private Long produtoId;
    private String nomeProduto;
    private double precoUnitario;
    private Integer quantidade;
    private double valorTotal;
    private String dataPedido;
    private String status;
    private String statusPagamento; // AGUARDANDO_PAGAMENTO ou PAGO
    private String dataPagamento;

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public Long getProdutoId() { return produtoId; }
    public String getNomeProduto() { return nomeProduto; }
    public double getPrecoUnitario() { return precoUnitario; }
    public Integer getQuantidade() { return quantidade; }
    public double getValorTotal() { return valorTotal; }
    public String getDataPedido() { return dataPedido; }
    public String getStatus() { return status; }
    public String getStatusPagamento() { return statusPagamento; }
    public String getDataPagamento() { return dataPagamento; }
}