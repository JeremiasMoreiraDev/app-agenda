package com.example.appmanutencao.model;

import com.google.gson.annotations.SerializedName;

public class OrdemServico {
    @SerializedName("id")
    private Long id;

    @SerializedName("cliente")
    private Cliente cliente;

    @SerializedName("servico")
    private Servico servico;

    @SerializedName("status")
    private String status;

    @SerializedName("tipoAtendimento")
    private String tipoAtendimento;

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Servico getServico() { return servico; }
    public String getStatus() { return status; }
    public String getTipoAtendimento() { return tipoAtendimento; }
}