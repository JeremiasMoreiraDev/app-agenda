package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos")
@Data
public class Agendamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @ManyToOne
    @JoinColumn(name = "colaborador_id")
    private Colaborador colaborador;

    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataAgendada;
    private String status; // PENDENTE, CONFIRMADO, CONCLUIDO, CANCELADO
    private String tipoAtendimento; // MANUTENCAO_PREVENTIVA ou NOVO_SERVICO
    private String observacoes;
}