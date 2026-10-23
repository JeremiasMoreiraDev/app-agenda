package com.example.demo.catalogo;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Produto> listar() {
        return repository.findByAtivoTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produto não encontrado"
                ));
    }

    public Produto cadastrar(Produto produto) {
        validar(produto);

        produto.setId(null);
        produto.setAtivo(true);

        return repository.save(produto);
    }

    public Produto atualizar(Long id, Produto dados) {
        validar(dados);

        Produto produto = buscarParaAlteracao(id);

        produto.setNome(dados.getNome());
        produto.setDescricao(dados.getDescricao());
        produto.setCategoria(dados.getCategoria());
        produto.setPreco(dados.getPreco());
        produto.setQuantidadeEstoque(dados.getQuantidadeEstoque());
        produto.setUrlImagem(dados.getUrlImagem());

        return repository.save(produto);
    }

    public void desativar(Long id) {
        Produto produto = buscarParaAlteracao(id);
        produto.setAtivo(false);

        repository.save(produto);
    }

    private Produto buscarParaAlteracao(Long id) {
        return repository.buscarPorIdComBloqueio(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produto não encontrado"
                ));
    }

    private void validar(Produto produto) {
        if (produto == null) {
            throw erro("Informe os dados do produto");
        }

        if (produto.getNome() == null
                || produto.getNome().isBlank()) {
            throw erro("O nome é obrigatório");
        }

        if (produto.getCategoria() == null
                || produto.getCategoria().isBlank()) {
            throw erro("A categoria é obrigatória");
        }

        validarTamanho(produto.getNome(), 150, "Nome");
        validarTamanho(produto.getCategoria(), 100, "Categoria");
        validarTamanho(produto.getDescricao(), 2000, "Descrição");
        validarTamanho(produto.getUrlImagem(), 1000, "URL da imagem");

        BigDecimal preco = produto.getPreco();

        if (preco == null || preco.signum() < 0) {
            throw erro(
                    "O preço é obrigatório e não pode ser negativo"
            );
        }

        if (preco.compareTo(new BigDecimal("9999999999.99")) > 0
                || preco.stripTrailingZeros().scale() > 2) {
            throw erro(
                    "O preço deve ter até 10 dígitos inteiros "
                            + "e 2 decimais"
            );
        }

        if (produto.getQuantidadeEstoque() == null
                || produto.getQuantidadeEstoque() < 0) {
            throw erro(
                    "O estoque é obrigatório e não pode ser negativo"
            );
        }
    }

    private void validarTamanho(
            String texto,
            int limite,
            String campo
    ) {
        if (texto != null && texto.length() > limite) {
            throw erro(
                    campo + " deve ter até " + limite + " caracteres"
            );
        }
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                mensagem
        );
    }
}