package com.example.catalogoprodutos;

import java.math.BigDecimal;

public class Produto {

    private Long id;
    private String nome;
    private String descricao;
    private String categoria;
    private BigDecimal preco;
    private Integer quantidadeEstoque;
    private String urlImagem;

    public Produto(Long id, String nome, String descricao,
                   String categoria, BigDecimal preco,
                   Integer quantidadeEstoque, String urlImagem) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
        this.urlImagem = urlImagem;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public String getUrlImagem() {
        return urlImagem;
    }
}