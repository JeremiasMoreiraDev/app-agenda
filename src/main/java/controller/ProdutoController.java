package com.example.demo.controller;

import com.example.demo.model.Produto;
import com.example.demo.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    @Autowired
    private ProdutoService service;

    @GetMapping
    public List<Produto> listar() { return service.listarTodos(); }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscar(@PathVariable Long id) {
        return service.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Produto criar(@RequestBody Produto produto) { return service.salvar(produto); }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @RequestBody Produto dados) {
        return service.buscarPorId(id).map(produto -> {
            produto.setNome(dados.getNome());
            produto.setDescricao(dados.getDescricao());
            produto.setCategoria(dados.getCategoria());
            produto.setPreco(dados.getPreco());
            produto.setQuantidadeEstoque(dados.getQuantidadeEstoque());
            // so troca a foto se vier uma nova (assim editar pelo app nao apaga a foto)
            if (dados.getImagemUrl() != null) {
                produto.setImagemUrl(dados.getImagemUrl());
            }
            return ResponseEntity.ok(service.salvar(produto));
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
}