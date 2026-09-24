package com.example.demo;

public class RelatorioColaboradorDTO {

    private Long colaboradorId;
    private Long totalAtendimentos;
    private Long concluidos;
    private Long pendentes;

    // Construtor
    public RelatorioColaboradorDTO(Long colaboradorId, Long totalAtendimentos, Long concluidos, Long pendentes) {
        this.colaboradorId = colaboradorId;
        this.totalAtendimentos = totalAtendimentos;
        this.concluidos = concluidos;
        this.pendentes = pendentes;
    }

    // Getters e Setters
    public Long getColaboradorId() { return colaboradorId; }
    public void setColaboradorId(Long colaboradorId) { this.colaboradorId = colaboradorId; }

    public Long getTotalAtendimentos() { return totalAtendimentos; }
    public void setTotalAtendimentos(Long totalAtendimentos) { this.totalAtendimentos = totalAtendimentos; }

    public Long getConcluidos() { return concluidos; }
    public void setConcluidos(Long concluidos) { this.concluidos = concluidos; }

    public Long getPendentes() { return pendentes; }
    public void setPendentes(Long pendentes) { this.pendentes = pendentes; }
}