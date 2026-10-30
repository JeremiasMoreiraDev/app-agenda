package com.example.appmanutencao.model;

import java.util.ArrayList;
import java.util.List;

public class CarrinhoManager {
    private static CarrinhoManager instancia;
    private final List<ItemPedidoRequest> itens = new ArrayList<>();
    private Long clienteId;

    private CarrinhoManager() {}

    public static synchronized CarrinhoManager getInstance() {
        if (instancia == null) {
            instancia = new CarrinhoManager();
        }
        return instancia;
    }

    public void adicionarItem(Long produtoId, String nomeProduto, int quantidade, double precoUnitario) {
        for (ItemPedidoRequest item : itens) {
            if (item.getProdutoId().equals(produtoId)) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                return;
            }
        }
        itens.add(new ItemPedidoRequest(produtoId, nomeProduto, quantidade, precoUnitario));
    }

    public List<ItemPedidoRequest> getItens() {
        return itens;
    }

    public double calcularTotalGeral() {
        double total = 0.0;
        for (ItemPedidoRequest item : itens) {
            if (item.getPrecoUnitario() != null) {
                total += item.getPrecoUnitario() * item.getQuantidade();
            }
        }
        return total;
    }

    public void limpar() {
        itens.clear();
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getClienteId() {
        return clienteId;
    }
}