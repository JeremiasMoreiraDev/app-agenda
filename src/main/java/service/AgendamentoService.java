
package com.example.demo.service;

import com.example.demo.model.Agendamento;
import com.example.demo.repository.AgendamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAll();
    }

    public Optional<Agendamento> buscarPorId(Long id) {
        return agendamentoRepository.findById(id);
    }

    public Agendamento salvar(Agendamento agendamento) {
        return agendamentoRepository.save(agendamento);
    }

    public void deletar(Long id) {
        agendamentoRepository.deleteById(id);
    }

    public List<Agendamento> listarPorCliente(Long clienteId) {
        return agendamentoRepository.findByClienteId(clienteId);
    }

    @Transactional
    public Agendamento atualizarStatus(Long id, String novoStatus) {
        if (novoStatus == null || novoStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "O status não pode ser vazio."
            );
        }

        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Agendamento não encontrado com o ID: " + id
                        )
                );

        String statusNormalizado = novoStatus
                .trim()
                .toUpperCase(Locale.ROOT);

        agendamento.setStatus(statusNormalizado);

        return agendamentoRepository.save(agendamento);
    }

    public long contarAgendamentosPorColaborador(Long colaboradorId) {
        return agendamentoRepository.countByColaboradorId(colaboradorId);
    }

    public long contarAgendamentosPorColaboradorEStatus(
            Long colaboradorId,
            String status
    ) {
        return agendamentoRepository
                .countByColaboradorIdAndStatus(colaboradorId, status);
    }
}
