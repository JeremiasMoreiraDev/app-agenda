package com.example.demo.controller;

import com.example.demo.dto.PerfilRequest;
import com.example.demo.dto.PerfilResponse;
import com.example.demo.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final ClienteService service;

    public PerfilController(ClienteService service) {
        this.service = service;
    }

    // GET /api/perfil?email=fulano@email.com  (o app usa o e-mail do login)
    @GetMapping
    public PerfilResponse buscarPorEmail(@RequestParam String email) {
        return PerfilResponse.de(service.buscarPerfilPorEmail(email));
    }

    @GetMapping("/{id}")
    public PerfilResponse buscarPorId(@PathVariable Long id) {
        return PerfilResponse.de(service.buscarPerfilPorId(id));
    }

    @PutMapping("/{id}")
    public PerfilResponse atualizar(@PathVariable Long id, @Valid @RequestBody PerfilRequest dados) {
        return PerfilResponse.de(service.atualizarPerfil(id, dados));
    }
}