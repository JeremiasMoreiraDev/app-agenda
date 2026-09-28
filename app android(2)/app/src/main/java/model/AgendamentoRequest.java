package com.example.appmanutencao.model;

import com.google.gson.annotations.SerializedName;

public class AgendamentoRequest {

    @SerializedName("cliente")
    private ClienteRef cliente;

    @SerializedName("servico")
    private ServicoRef servico;

    @SerializedName("dataAgendada")
    private String dataAgendada;

    @SerializedName("tipoAtendimento")
    private String tipoAtendimento;

    @SerializedName("status")
    private String status;

    @SerializedName("observacoes")
    private String observacoes;

    public AgendamentoRequest(Long clienteId, Long servicoId, String dataAgendada, String tipoAtendimento, String status, String observacoes) {
        this.cliente = new ClienteRef(clienteId);
        this.servico = new ServicoRef(servicoId);
        this.dataAgendada = dataAgendada;
        this.tipoAtendimento = tipoAtendimento;
        this.status = status;
        this.observacoes = observacoes;
    }

    // Getters e Setters
    public ClienteRef getCliente() { return cliente; }
    public void setCliente(ClienteRef cliente) { this.cliente = cliente; }

    public ServicoRef getServico() { return servico; }
    public void setServico(ServicoRef servico) { this.servico = servico; }

    public String getDataAgendada() { return dataAgendada; }
    public void setDataAgendada(String dataAgendada) { this.dataAgendada = dataAgendada; }

    public String getTipoAtendimento() { return tipoAtendimento; }
    public void setTipoAtendimento(String tipoAtendimento) { this.tipoAtendimento = tipoAtendimento; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}