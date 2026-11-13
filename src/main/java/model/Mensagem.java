package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensagens")
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ordemServicoId;
    private String remetente; // Ex: "CLIENTE" ou "ADMIN"
    private String texto;
    private LocalDateTime dataHora;

    public Mensagem() {
    }

    public Mensagem(Long ordemServicoId, String remetente, String texto) {
        this.ordemServicoId = ordemServicoId;
        this.remetente = remetente;
        this.texto = texto;
    }

    // Garante que a data/hora será preenchida automaticamente antes de salvar no banco
    @PrePersist
    public void prePersist() {
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now();
        }
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrdemServicoId() {
        return ordemServicoId;
    }

    public void setOrdemServicoId(Long ordemServicoId) {
        this.ordemServicoId = ordemServicoId;
    }

    public String getRemetente() {
        return remetente;
    }

    public void setRemetente(String remetente) {
        this.remetente = remetente;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}