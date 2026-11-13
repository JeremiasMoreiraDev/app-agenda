
package com.example.demo.controller;

import com.example.demo.model.Produto;
import com.example.demo.model.ProdutoImagem;
import com.example.demo.repository.ProdutoImagemRepository;
import com.example.demo.service.ProdutoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoImagemController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoImagemRepository imagemRepository;

    @PostMapping(
            value = "/{id}/imagem",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> enviar(
            @PathVariable Long id,
            @RequestParam("arquivo") MultipartFile arquivo) {

        Optional<Produto> encontrado = produtoService.buscarPorId(id);

        if (encontrado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (arquivo == null || arquivo.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Selecione uma imagem.");
        }

        String tipo = arquivo.getContentType();

        if (tipo == null || !tipo.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body("O arquivo precisa ser uma imagem.");
        }

        try {
            Optional<ProdutoImagem> existente =
                    imagemRepository.findByProdutoId(id);

            ProdutoImagem imagem;

            if (existente.isPresent()) {
                imagem = existente.get();
            } else {
                imagem = new ProdutoImagem();
                imagem.setProdutoId(id);
            }

            imagem.setTipo(tipo);
            imagem.setDados(arquivo.getBytes());

            imagemRepository.save(imagem);

        } catch (IOException e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).body("Erro ao salvar a imagem.");
        }

        Produto produto = encontrado.get();

        produto.setImagemUrl("/api/produtos/" + id + "/imagem");

        return ResponseEntity.ok(produtoService.salvar(produto));
    }

    @GetMapping("/{id}/imagem")
    public ResponseEntity<byte[]> buscar(@PathVariable Long id) {

        Optional<ProdutoImagem> resultado =
                imagemRepository.findByProdutoId(id);

        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ProdutoImagem imagem = resultado.get();

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(imagem.getTipo())
                )
                .cacheControl(
                        CacheControl.maxAge(1, TimeUnit.DAYS)
                )
                .body(imagem.getDados());
    }
}
