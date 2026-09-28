package com.example.demo.service;

import com.example.demo.model.Agendamento;
import com.example.demo.repository.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

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

    public Agendamento atualizarStatus(Long id, String novoStatus) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado com o ID: " + id));

        agendamento.setStatus(novoStatus);
        return agendamentoRepository.save(agendamento);
    }

    public long contarAgendamentosPorColaborador(Long colaboradorId) {
        return agendamentoRepository.countByColaboradorId(colaboradorId);
    }

    public long contarAgendamentosPorColaboradorEStatus(Long colaboradorId, String status) {
        return agendamentoRepository.countByColaboradorIdAndStatus(colaboradorId, status);
    }
}