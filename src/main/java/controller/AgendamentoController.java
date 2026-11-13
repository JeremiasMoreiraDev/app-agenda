
package com.example.demo.controller;

import com.example.demo.model.Agendamento;
import com.example.demo.service.AgendamentoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Agendamento> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    public List<Agendamento> listarPorCliente(
            @PathVariable Long clienteId
    ) {
        return service.listarPorCliente(clienteId);
    }

    @PostMapping
    public Agendamento criar(@RequestBody Agendamento agendamento) {
        return service.salvar(agendamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Agendamento> atualizar(
            @PathVariable Long id,
            @RequestBody Agendamento dados
    ) {
        return service.buscarPorId(id)
                .map(agendamento -> {
                    agendamento.setDataAgendada(dados.getDataAgendada());
                    agendamento.setStatus(dados.getStatus());
                    agendamento.setTipoAtendimento(
                            dados.getTipoAtendimento()
                    );
                    agendamento.setObservacoes(dados.getObservacoes());

                    return ResponseEntity.ok(service.salvar(agendamento));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        service.deletar(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(
            @PathVariable Long id,
            @RequestParam String novoStatus
    ) {
        try {
            Agendamento atualizado =
                    service.atualizarStatus(id, novoStatus);

            return ResponseEntity.ok(atualizado);

        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null
                    && e.getMessage().startsWith(
                    "Agendamento não encontrado"
            )) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.badRequest().body(
                    Map.of("erro", e.getMessage())
            );
        }
    }
}
