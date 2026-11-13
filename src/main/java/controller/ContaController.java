package com.example.demo.controller;

import com.example.demo.dto.ContaRequest;
import com.example.demo.dto.ContaResponse;
import com.example.demo.model.TipoConta;
import com.example.demo.service.ContaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contas")
public class ContaController {

    private final ContaService service;

    public ContaController(ContaService service) {
        this.service = service;
    }

    // GET /api/contas  ou  /api/contas?tipo=ADMIN  ou  /api/contas?tipo=CLIENTE
    @GetMapping
    public List<ContaResponse> listar(@RequestParam(required = false) TipoConta tipo) {
        return service.listar(tipo);
    }

    @PostMapping
    public ResponseEntity<ContaResponse> criar(@RequestBody ContaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    // DELETE /api/contas/CLIENTE/3  (o tipo vai na URL porque o id de cliente e o de admin podem ser iguais)
    @DeleteMapping("/{tipo}/{id}")
    public ResponseEntity<Void> remover(@PathVariable TipoConta tipo, @PathVariable Long id) {
        service.remover(tipo, id);
        return ResponseEntity.noContent().build();
    }
}