package com.example.appmanutencao.model;

// Produto como a API devolve em GET /api/produtos
public class ProdutoLoja {

    private Long id;
    private String nome;
    private String descricao;
    private String categoria;
    private double preco;
    private Integer quantidadeEstoque;
    private String imagemUrl;

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

    public double getPreco() {
        return preco;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque == null ? 0 : quantidadeEstoque;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }
}