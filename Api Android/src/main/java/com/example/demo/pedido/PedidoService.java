package com.example.demo.pedido;

import com.example.demo.catalogo.Produto;
import com.example.demo.catalogo.ProdutoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class PedidoService {

    private static final int LIMITE_PRODUTOS = 100;

    private static final BigDecimal TOTAL_MAXIMO =
            new BigDecimal("99999999999999999.99");

    private final PedidoRepository pedidos;
    private final ProdutoRepository produtos;

    public PedidoService(
            PedidoRepository pedidos,
            ProdutoRepository produtos
    ) {
        this.pedidos = pedidos;
        this.produtos = produtos;
    }

    @Transactional
    public PedidoResponse criar(CriarPedidoRequest requisicao) {
        Dados dados = validar(requisicao);

        Optional<Pedido> existente =
                pedidos.findByChaveConfirmacao(dados.chave());

        if (existente.isPresent()) {
            return conferirRepeticao(existente.get(), dados.resumo());
        }

        Pedido pedido = new Pedido();
        pedido.setChaveConfirmacao(dados.chave());
        pedido.setResumoRequisicao(dados.resumo());
        pedido.setCriadoEm(Instant.now());
        pedido.setTotal(BigDecimal.ZERO);

        /*
         * Reserva a chave única dentro desta transação.
         * Se outra confirmação já estiver usando a mesma chave,
         * o banco impede uma segunda gravação.
         */
        pedidos.saveAndFlush(pedido);

        BigDecimal total = BigDecimal.ZERO;

        /*
         * TreeMap mantém os IDs em ordem.
         * As transações bloqueiam os produtos na mesma ordem.
         */
        for (Map.Entry<Long, Integer> entrada
                : dados.quantidades().entrySet()) {

            Long produtoId = entrada.getKey();
            int quantidade = entrada.getValue();

            Produto produto = produtos.buscarPorIdComBloqueio(produtoId)
                    .orElseThrow(() -> erro(
                            HttpStatus.NOT_FOUND,
                            "Produto não encontrado: " + produtoId
                    ));

            if (!Boolean.TRUE.equals(produto.getAtivo())) {
                throw erro(
                        HttpStatus.CONFLICT,
                        "Produto indisponível: " + produto.getNome()
                );
            }

            Integer estoque = produto.getQuantidadeEstoque();

            if (estoque == null || estoque < quantidade) {
                throw erro(
                        HttpStatus.CONFLICT,
                        "Estoque insuficiente para: " + produto.getNome()
                );
            }

            BigDecimal preco = produto.getPreco();

            if (preco == null || preco.signum() < 0) {
                throw erro(
                        HttpStatus.CONFLICT,
                        "Preço inválido para: " + produto.getNome()
                );
            }

            ItemPedido item = new ItemPedido();
            item.setProdutoId(produto.getId());
            item.setNomeProduto(produto.getNome());
            item.setPrecoUnitario(preco);
            item.setQuantidade(quantidade);

            total = total.add(item.getSubtotal());

            if (total.compareTo(TOTAL_MAXIMO) > 0) {
                throw erro(
                        HttpStatus.BAD_REQUEST,
                        "O total do pedido ultrapassa o limite permitido."
                );
            }

            pedido.getItens().add(item);
            produto.setQuantidadeEstoque(estoque - quantidade);
        }

        BigDecimal totalEsperado = requisicao.totalEsperado();

        if (totalEsperado != null
                && total.compareTo(totalEsperado) != 0) {
            throw erro(
                    HttpStatus.CONFLICT,
                    "Os preços foram atualizados. "
                            + "Revise o carrinho antes de confirmar."
            );
        }

        pedido.setTotal(total);

        /*
         * Os produtos foram carregados nesta transação.
         * O JPA persiste suas alterações junto com o pedido.
         */
        pedidos.flush();

        return PedidoResponse.de(pedido);
    }

    /*
     * Será chamado pelo controller depois que uma tentativa
     * concorrente com a mesma chave tiver sido revertida.
     */
    @Transactional(readOnly = true)
    public Optional<PedidoResponse> recuperarRepeticao(
            CriarPedidoRequest requisicao
    ) {
        Dados dados = validar(requisicao);

        return pedidos.findByChaveConfirmacao(dados.chave())
                .map(pedido -> conferirRepeticao(
                        pedido,
                        dados.resumo()
                ));
    }

    private PedidoResponse conferirRepeticao(
            Pedido pedido,
            String resumo
    ) {
        if (!pedido.getResumoRequisicao().equals(resumo)) {
            throw erro(
                    HttpStatus.CONFLICT,
                    "Esta chave de confirmação já foi usada "
                            + "para outro conteúdo de pedido."
            );
        }

        return PedidoResponse.de(pedido);
    }

    private Dados validar(CriarPedidoRequest requisicao) {
        if (requisicao == null) {
            throw erro(
                    HttpStatus.BAD_REQUEST,
                    "Informe os dados do pedido."
            );
        }

        String chave = normalizarChave(
                requisicao.chaveConfirmacao()
        );

        if (requisicao.itens() == null
                || requisicao.itens().isEmpty()) {
            throw erro(
                    HttpStatus.BAD_REQUEST,
                    "O pedido precisa ter pelo menos um produto."
            );
        }

        if (requisicao.itens().size() > LIMITE_PRODUTOS) {
            throw erro(
                    HttpStatus.BAD_REQUEST,
                    "O pedido permite até "
                            + LIMITE_PRODUTOS + " produtos diferentes."
            );
        }

        TreeMap<Long, Integer> quantidades = new TreeMap<>();

        for (CriarPedidoRequest.Item item : requisicao.itens()) {
            if (item == null
                    || item.produtoId() == null
                    || item.produtoId() <= 0) {
                throw erro(
                        HttpStatus.BAD_REQUEST,
                        "Informe um identificador de produto válido."
                );
            }

            if (item.quantidade() == null
                    || item.quantidade() <= 0) {
                throw erro(
                        HttpStatus.BAD_REQUEST,
                        "A quantidade deve ser um inteiro maior que zero."
                );
            }

            Integer anterior = quantidades.putIfAbsent(
                    item.produtoId(),
                    item.quantidade()
            );

            if (anterior != null) {
                throw erro(
                        HttpStatus.BAD_REQUEST,
                        "Envie cada produto apenas uma vez, "
                                + "com sua quantidade total."
                );
            }
        }

        StringBuilder conteudo = new StringBuilder();

        for (Map.Entry<Long, Integer> entrada
                : quantidades.entrySet()) {
            conteudo.append(entrada.getKey())
                    .append(":")
                    .append(entrada.getValue())
                    .append(";");
        }

        if (requisicao.totalEsperado() != null) {
            BigDecimal esperado = requisicao.totalEsperado();

            if (esperado.signum() < 0
                    || esperado.compareTo(TOTAL_MAXIMO) > 0
                    || esperado.stripTrailingZeros().scale() > 2) {
                throw erro(
                        HttpStatus.BAD_REQUEST,
                        "O total esperado deve ser um valor válido "
                                + "com até duas casas decimais."
                );
            }

            conteudo.append("total:")
                    .append(esperado.stripTrailingZeros().toPlainString())
                    .append(";");
        }

        return new Dados(
                chave,
                quantidades,
                calcularResumo(conteudo.toString())
        );
    }

    private String normalizarChave(String valor) {
        if (valor == null || valor.length() != 36) {
            throw erro(
                    HttpStatus.BAD_REQUEST,
                    "Informe uma chave de confirmação UUID válida."
            );
        }

        try {
            String normalizada = UUID.fromString(valor).toString();

            if (!normalizada.equalsIgnoreCase(valor)) {
                throw new IllegalArgumentException();
            }

            return normalizada;

        } catch (IllegalArgumentException erro) {
            throw erro(
                    HttpStatus.BAD_REQUEST,
                    "Informe uma chave de confirmação UUID válida."
            );
        }
    }

    private String calcularResumo(String conteudo) {
        try {
            MessageDigest algoritmo =
                    MessageDigest.getInstance("SHA-256");

            byte[] resultado = algoritmo.digest(
                    conteudo.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(resultado);

        } catch (NoSuchAlgorithmException erro) {
            throw new IllegalStateException(
                    "Não foi possível preparar a confirmação.",
                    erro
            );
        }
    }

    private ResponseStatusException erro(
            HttpStatus status,
            String mensagem
    ) {
        return new ResponseStatusException(status, mensagem);
    }

    private record Dados(
            String chave,
            TreeMap<Long, Integer> quantidades,
            String resumo
    ) {
    }
}