package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PerfilRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String nome;

    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @Pattern(regexp = "Residencial|Comercial|Rural|Usina",
            message = "Tipo de imóvel deve ser Residencial, Comercial, Rural ou Usina")
    private String tipoImovel;

    @Pattern(regexp = "Sim|Nao", message = "Informe Sim ou Nao para carro elétrico")
    private String possuiCarroEletrico;

    @Pattern(regexp = "Sim|Nao", message = "Informe Sim ou Nao para bateria")
    private String possuiBateria;
}