package com.example.demo.controller;

import com.example.demo.dto.RelatorioColaboradorDTO;
import com.example.demo.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @GetMapping("/colaborador/{id}")
    public ResponseEntity<RelatorioColaboradorDTO> obterRelatorioColaborador(@PathVariable Long id) {
        RelatorioColaboradorDTO relatorio = relatorioService.gerarRelatorioColaborador(id);
        return ResponseEntity.ok(relatorio);
    }
}
