package com.example.demo.controller;

import com.example.demo.model.Agendamento;
import com.example.demo.service.AgendamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {
    @Autowired
    private AgendamentoService service;

    @GetMapping
    public List<Agendamento> listar() { return service.listarTodos(); }

    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> buscar(@PathVariable Long id) {
        return service.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    public List<Agendamento> listarPorCliente(@PathVariable Long clienteId) {
        return service.listarPorCliente(clienteId);
    }

    @PostMapping
    public Agendamento criar(@RequestBody Agendamento agendamento) { return service.salvar(agendamento); }

    @PutMapping("/{id}")
    public ResponseEntity<Agendamento> atualizar(@PathVariable Long id, @RequestBody Agendamento dados) {
        return service.buscarPorId(id).map(agendamento -> {
            agendamento.setDataAgendada(dados.getDataAgendada());
            agendamento.setStatus(dados.getStatus());
            agendamento.setTipoAtendimento(dados.getTipoAtendimento());
            agendamento.setObservacoes(dados.getObservacoes());
            return ResponseEntity.ok(service.salvar(agendamento));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Agendamento> atualizarStatus(
            @PathVariable Long id,
            @RequestParam String novoStatus) {
        try {
            // Corrigido de agendamentoService para service (que é o nome injetado acima)
            Agendamento agendamentoAtualizado = service.atualizarStatus(id, novoStatus);
            return ResponseEntity.ok(agendamentoAtualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}