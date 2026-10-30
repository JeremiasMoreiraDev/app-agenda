package com.example.appmanutencao.model;

public class Cliente {
    private Long id;
    private String nome;
    private String telefone;
    private String email;
    private String tipoImovel;
    private String possuiCarroEletrico;
    private String possuiBateria;

    public Cliente() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTipoImovel() { return tipoImovel; }
    public void setTipoImovel(String tipoImovel) { this.tipoImovel = tipoImovel; }

    public String getPossuiCarroEletrico() { return possuiCarroEletrico; }
    public void setPossuiCarroEletrico(String possuiCarroEletrico) { this.possuiCarroEletrico = possuiCarroEletrico; }

    public String getPossuiBateria() { return possuiBateria; }
    public void setPossuiBateria(String possuiBateria) { this.possuiBateria = possuiBateria; }
}