package com.example.appmanutencao.model;

import com.google.gson.annotations.SerializedName;

public class AgendamentoRequest {
    @SerializedName("cliente")
    private ClienteRef cliente;

    @SerializedName("servico")
    private ServicoRef servico;

    @SerializedName("colaborador")
    private ColaboradorRef colaborador;

    @SerializedName("dataAgendada")
    private String dataAgendada;

    @SerializedName("tipoAtendimento")
    private String tipoAtendimento;

    @SerializedName("status")
    private String status;

    @SerializedName("observacoes")
    private String observacoes;

    public AgendamentoRequest(Long clienteId, Long servicoId, Long colaboradorId, String dataAgendada, String tipoAtendimento, String status, String observacoes) {
        this.cliente = new ClienteRef(clienteId);
        this.servico = new ServicoRef(servicoId);
        if (colaboradorId != null) {
            this.colaborador = new ColaboradorRef(colaboradorId);
        }
        this.dataAgendada = dataAgendada;
        this.tipoAtendimento = tipoAtendimento;
        this.status = status;
        this.observacoes = observacoes;
    }
}