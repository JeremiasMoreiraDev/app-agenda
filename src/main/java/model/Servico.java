package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "servicos")
@Data
public class Servico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo; // Ex: Manutenção Preventiva, Instalação de Carregador
    private String descricao;
    private Double precoBase;
    private String categoria; // "Manutenção", "Mobilidade", "Armazenamento", "Consultoria"
}