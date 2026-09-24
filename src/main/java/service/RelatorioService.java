package com.example.demo.service;

import com.example.demo.dto.RelatorioColaboradorDTO;
import com.example.demo.repository.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RelatorioService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    public RelatorioColaboradorDTO gerarRelatorioColaborador(Long colaboradorId) {
        long total = agendamentoRepository.countByColaboradorId(colaboradorId);
        long concluidos = agendamentoRepository.countByColaboradorIdAndStatus(colaboradorId, "CONCLUIDO");
        long pendentes = agendamentoRepository.countByColaboradorIdAndStatus(colaboradorId, "PENDENTE");

        return new RelatorioColaboradorDTO(colaboradorId, total, concluidos, pendentes);
    }
}