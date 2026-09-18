package com.example.demo.service;

import com.example.demo.model.Agendamento;
import com.example.demo.repository.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AgendamentoService {
    @Autowired
    private AgendamentoRepository agendamentoRepository;

    public List<Agendamento> listarTodos() { return agendamentoRepository.findAll(); }
    public Optional<Agendamento> buscarPorId(Long id) { return agendamentoRepository.findById(id); }
    public List<Agendamento> listarPorCliente(Long clienteId) { return agendamentoRepository.findByClienteId(clienteId); }

    public Agendamento salvar(Agendamento agendamento) {
        if (agendamento.getDataSolicitacao() == null) {
            agendamento.setDataSolicitacao(LocalDateTime.now());
        }
        if (agendamento.getStatus() == null) {
            agendamento.setStatus("PENDENTE");
        }
        return agendamentoRepository.save(agendamento);
    }

    public void deletar(Long id) { agendamentoRepository.deleteById(id); }
}
