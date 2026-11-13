package com.example.demo.dto;

import com.example.demo.model.Cliente;
import lombok.Data;

@Data
public class PerfilResponse {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String tipoImovel;
    private String possuiCarroEletrico;
    private String possuiBateria;

    public static PerfilResponse de(Cliente cliente) {
        PerfilResponse resposta = new PerfilResponse();
        resposta.setId(cliente.getId());
        resposta.setNome(cliente.getNome());
        resposta.setEmail(cliente.getEmail());
        resposta.setTelefone(cliente.getTelefone());
        resposta.setTipoImovel(cliente.getTipoImovel());
        resposta.setPossuiCarroEletrico(cliente.getPossuiCarroEletrico());
        resposta.setPossuiBateria(cliente.getPossuiBateria());
        return resposta;
    }
}