package com.example.demo.controller;

import com.example.demo.model.Colaborador;
import com.example.demo.repository.ColaboradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colaboradores")
public class ColaboradorController {

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    // CREATE - Cadastrar novo colaborador
    @PostMapping
    public ResponseEntity<Colaborador> criar(@RequestBody Colaborador colaborador) {
        Colaborador salvo = colaboradorRepository.save(colaborador);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // READ ALL - Listar todos os colaboradores
    @GetMapping
    public List<Colaborador> listar() {
        return colaboradorRepository.findAll();
    }

    // READ BY ID - Buscar colaborador específico por ID
    @GetMapping("/{id}")
    public ResponseEntity<Colaborador> buscarPorId(@PathVariable Long id) {
        return colaboradorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE - Atualizar dados do colaborador
    @PutMapping("/{id}")
    public ResponseEntity<Colaborador> atualizar(@PathVariable Long id, @RequestBody Colaborador colaboradorAtualizado) {
        return colaboradorRepository.findById(id)
                .map(colaboradorExistente -> {
                    // ATENÇÃO: Ajuste os campos abaixo conforme os atributos reais da sua classe Colaborador (ex: setNome, setEmail, etc.)
                    colaboradorExistente.setNome(colaboradorAtualizado.getNome());

                    Colaborador salvo = colaboradorRepository.save(colaboradorExistente);
                    return ResponseEntity.ok(salvo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE - Remover colaborador
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (colaboradorRepository.existsById(id)) {
            colaboradorRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}