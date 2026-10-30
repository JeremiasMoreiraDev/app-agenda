package com.example.demo.controller;

import com.example.demo.model.Mensagem;
import com.example.demo.repository.MensagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mensagens")
public class MensagemController {

    @Autowired
    private MensagemRepository mensagemRepository;

    // 1. CREATE - Enviar/Salvar uma nova mensagem (Método: POST)
    @PostMapping
    public ResponseEntity<Mensagem> enviarMensagem(@RequestBody Mensagem mensagem) {
        Mensagem salva = mensagemRepository.save(mensagem);
        return ResponseEntity.ok(salva);
    }

    // 2. READ - Buscar uma mensagem específica pelo ID (Método: GET)
    // *Importante: Ficar em cima para evitar conflito com o /{ordemServicoId}*
    @GetMapping("/detalhes/{id}")
    public ResponseEntity<Mensagem> buscarPorId(@PathVariable Long id) {
        return mensagemRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. READ - Listar mensagens de uma Ordem de Serviço específica (Método: GET)
    @GetMapping("/{ordemServicoId}")
    public ResponseEntity<List<Mensagem>> listarMensagens(@PathVariable Long ordemServicoId) {
        List<Mensagem> mensagens = mensagemRepository.findByOrdemServicoIdOrderByDataHoraAsc(ordemServicoId);
        return ResponseEntity.ok(mensagens);
    }

    // 4. UPDATE - Editar o texto de uma mensagem existente pelo ID (Método: PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Mensagem> atualizarMensagem(@PathVariable Long id, @RequestBody Mensagem mensagemAtualizada) {
        return mensagemRepository.findById(id)
                .map(mensagem -> {
                    mensagem.setTexto(mensagemAtualizada.getTexto());
                    Mensagem salva = mensagemRepository.save(mensagem);
                    return ResponseEntity.ok(salva);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. DELETE - Apagar uma mensagem pelo ID (Método: DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarMensagem(@PathVariable Long id) {
        if (mensagemRepository.existsById(id)) {
            mensagemRepository.deleteById(id);
            return ResponseEntity.noContent().build(); // Retorna HTTP 204
        }
        return ResponseEntity.notFound().build(); // Retorna HTTP 404 caso não encontre
    }
    // 6. READ - Listar TODAS as mensagens do sistema (Método: GET)
    @GetMapping
    public ResponseEntity<List<Mensagem>> listarTodasMensagens() {
        List<Mensagem> mensagens = mensagemRepository.findAll();
        return ResponseEntity.ok(mensagens);
    }
}