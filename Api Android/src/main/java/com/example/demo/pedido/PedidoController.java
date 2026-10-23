package com.example.demo.pedido;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(
            @RequestBody CriarPedidoRequest requisicao
    ) {
        try {
            return service.criar(requisicao);

        } catch (DataIntegrityViolationException erro) {
            /*
             * A transação anterior já terminou com rollback.
             * Se a chave foi confirmada por outra requisição,
             * recupera o pedido sem repetir a baixa de estoque.
             */
            return service.recuperarRepeticao(requisicao)
                    .orElseThrow(() -> erro);
        }
    }
}