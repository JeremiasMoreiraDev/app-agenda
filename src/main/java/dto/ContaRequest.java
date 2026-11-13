package com.example.demo.dto;

import com.example.demo.model.TipoConta;
import lombok.Data;

@Data
public class ContaRequest {
    private String nome;
    private String email;
    private String telefone;
    private TipoConta tipo;
}