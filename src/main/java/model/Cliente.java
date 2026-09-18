package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String telefone;
    private String email;
    private String tipoImovel; // Residencial, Comercial, Rural, Usina
    private String possuiCarroEletrico; // Sim / Nao
    private String possuiBateria; // Sim / Nao
}