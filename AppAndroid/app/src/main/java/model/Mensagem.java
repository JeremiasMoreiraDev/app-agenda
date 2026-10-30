package com.example.appmanutencao.model;

public class Mensagem {
    private Long id;
    private Long ordemServicoId;
    private String remetente;
    private String texto;
    private String dataHora;

    // Construtores
    public Mensagem() {}

    public Mensagem(Long ordemServicoId, String remetente, String texto) {
        this.ordemServicoId = ordemServicoId;
        this.remetente = remetente;
        this.texto = texto;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrdemServicoId() { return ordemServicoId; }
    public void setOrdemServicoId(Long ordemServicoId) { this.ordemServicoId = ordemServicoId; }

    public String getRemetente() { return remetente; }
    public void setRemetente(String remetente) { this.remetente = remetente; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }
}