package com.example.demo.dto;

import com.example.demo.model.Administrador;
import com.example.demo.model.Cliente;
import com.example.demo.model.TipoConta;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContaResponse {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private TipoConta tipo;

    public static ContaResponse deCliente(Cliente c) {
        return new ContaResponse(c.getId(), c.getNome(), c.getEmail(), c.getTelefone(), TipoConta.CLIENTE);
    }

    public static ContaResponse deAdministrador(Administrador a) {
        return new ContaResponse(a.getId(), a.getNome(), a.getEmail(), a.getTelefone(), TipoConta.ADMIN);
    }
}