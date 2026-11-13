package com.example.demo.controller;

import com.example.demo.model.Pedido;
import com.example.demo.model.PedidoRequest;
import com.example.demo.model.PixResponse;
import com.example.demo.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    @Autowired
    private PedidoService service;

    // tela do ADM: todos os pedidos
    @GetMapping
    public List<Pedido> listar() {
        return service.listarTodos();
    }

    // tela do cliente: so os pedidos dele
    @GetMapping("/cliente/{clienteId}")
    public List<Pedido> listarPorCliente(@PathVariable Long clienteId) {
        return service.listarPorCliente(clienteId);
    }

    // cliente clicou em "Comprar"
    @PostMapping
    public ResponseEntity<Pedido> comprar(@RequestBody PedidoRequest request) {
        Pedido pedido = service.comprar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    // tela de pagamento: codigo Pix (copia e cola) para gerar o QR code
    @GetMapping("/{id}/pix")
    public PixResponse gerarPix(@PathVariable Long id) {
        return service.gerarPix(id);
    }

    // DEMONSTRACAO: botao "Ja paguei" simula a confirmacao do pagamento
    @PostMapping("/{id}/pagamento/confirmar")
    public Pedido confirmarPagamento(@PathVariable Long id) {
        return service.confirmarPagamento(id);
    }

    // ---- tratamento de erros (vale para todos os metodos acima) ----

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> naoEncontrado(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> requisicaoInvalida(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> conflito(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}