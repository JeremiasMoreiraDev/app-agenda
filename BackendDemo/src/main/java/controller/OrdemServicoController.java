package com.example.demo.controller;

import com.example.demo.model.OrdemServico;
import com.example.demo.model.StatusOs;
import com.example.demo.service.OrdemServicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ordens-servico")
public class OrdemServicoController {

    @Autowired
    private OrdemServicoService service;

    // Read - Listar todas
    @GetMapping
    public ResponseEntity<List<OrdemServico>> listarTodas() {
        List<OrdemServico> lista = service.listarTodas();
        return ResponseEntity.ok(lista);
    }

    // Read - Listar so os atendimentos de um cliente (tela "Meus Atendimentos")
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<OrdemServico>> listarPorCliente(@PathVariable Long clienteId) {
        List<OrdemServico> doCliente = service.listarTodas().stream()
                .filter(o -> o.getCliente() != null && clienteId.equals(o.getCliente().getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(doCliente);
    }

    // Read - Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<OrdemServico> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create - Criar nova OS
    @PostMapping
    public ResponseEntity<OrdemServico> criar(@RequestBody OrdemServico ordemServico) {
        OrdemServico novaOrdem = service.salvar(ordemServico);
        return ResponseEntity.status(201).body(novaOrdem);
    }

    // Update - Atualizar OS existente
    @PutMapping("/{id}")
    public ResponseEntity<OrdemServico> atualizar(@PathVariable Long id, @RequestBody OrdemServico ordemAtualizada) {
        try {
            OrdemServico atualizada = service.atualizar(id, ordemAtualizada);
            return ResponseEntity.ok(atualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Update - Mudar so o status (tela do ADM). Corpo: { "status": "EM_ANDAMENTO" }
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(@PathVariable Long id, @RequestBody Map<String, String> corpo) {
        Optional<OrdemServico> encontrada = service.buscarPorId(id);
        if (encontrada.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String texto = corpo.get("status");
        if (texto == null || texto.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Status nao informado");
        }

        StatusOs novoStatus;
        try {
            novoStatus = StatusOs.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Status invalido. Opcoes: " + Arrays.toString(StatusOs.values()));
        }

        OrdemServico ordem = encontrada.get();
        ordem.setStatus(novoStatus);
        return ResponseEntity.ok(service.salvar(ordem));
    }

    // Delete - Excluir OS
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}