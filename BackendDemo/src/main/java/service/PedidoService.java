package com.example.demo.service;

import com.example.demo.model.ItemPedido;
import com.example.demo.model.ItemPedidoRequest;
import com.example.demo.model.Pedido;
import com.example.demo.model.PedidoRequest;
import com.example.demo.model.PixResponse;
import com.example.demo.model.Produto;
import com.example.demo.model.StatusPagamento;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.util.PixPayloadGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class PedidoService {

    private static final String STATUS_PENDENTE = "Pendente";
    private static final String STATUS_CANCELADO = "Cancelado";

    @Autowired
    private PedidoRepository repository;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private PixPayloadGenerator pixPayloadGenerator;

    public List<Pedido> listarTodos() {
        return repository.findAllByOrderByDataPedidoDesc();
    }

    public List<Pedido> listarPorCliente(Long clienteId) {
        return repository.findByClienteIdOrderByDataPedidoDesc(clienteId);
    }

    public Pedido buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido nao encontrado"));
    }

    // Cria um pedido com varios itens (carrinho).
    // Se QUALQUER item falhar, o @Transactional desfaz tudo:
    // nao fica estoque baixado sem pedido, nem pedido pela metade.
    @Transactional
    public Pedido comprar(PedidoRequest request) {
        if (request.getClienteId() == null) {
            throw new IllegalArgumentException("Cliente nao informado");
        }

        List<ItemPedidoRequest> itensSolicitados = obterItens(request);
        if (itensSolicitados.isEmpty()) {
            throw new IllegalArgumentException("Pedido sem itens");
        }

        // soma as quantidades se o mesmo produto aparecer mais de uma vez
        Map<Long, Integer> quantidadePorProduto = new LinkedHashMap<>();
        for (ItemPedidoRequest item : itensSolicitados) {
            if (item == null || item.getProdutoId() == null) {
                throw new IllegalArgumentException("Produto nao informado");
            }
            if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade deve ser maior que zero");
            }
            quantidadePorProduto.merge(item.getProdutoId(), item.getQuantidade(), Integer::sum);
        }

        Pedido pedido = new Pedido();
        pedido.setClienteId(request.getClienteId());
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus(STATUS_PENDENTE);
        pedido.setStatusPagamento(StatusPagamento.AGUARDANDO_PAGAMENTO);

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entrada : quantidadePorProduto.entrySet()) {
            int quantidade = entrada.getValue();

            Produto produto = produtoService.buscarPorId(entrada.getKey())
                    .orElseThrow(() -> new NoSuchElementException("Produto nao encontrado"));

            if (produto.getPreco() == null) {
                throw new IllegalArgumentException(
                        "Produto sem preco cadastrado: " + produto.getNome());
            }

            int estoque = produto.getQuantidadeEstoque() == null ? 0 : produto.getQuantidadeEstoque();
            if (quantidade > estoque) {
                throw new IllegalArgumentException(
                        "Estoque insuficiente para " + produto.getNome() + ". Disponivel: " + estoque);
            }

            // baixa o estoque
            produto.setQuantidadeEstoque(estoque - quantidade);
            produtoService.salvar(produto);

            // cria o item do pedido
            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(quantidade));

            ItemPedido item = new ItemPedido();
            item.setProdutoId(produto.getId());
            item.setNomeProduto(produto.getNome());
            item.setPrecoUnitario(produto.getPreco());
            item.setQuantidade(quantidade);
            item.setSubtotal(subtotal);
            pedido.adicionarItem(item);

            total = total.add(subtotal);
        }

        pedido.setValorTotal(total);
        return repository.save(pedido);
    }

    // gera o codigo Pix (copia e cola) do pedido; o app transforma isso em QR code
    public PixResponse gerarPix(Long pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);
        validarPodeReceberPagamento(pedido);

        String codigo = pixPayloadGenerator.gerar(pedido.getValorTotal(), "PEDIDO" + pedido.getId());
        return new PixResponse(pedido.getId(), pedido.getValorTotal(), codigo);
    }

    // DEMONSTRACAO: simula a confirmacao do pagamento (sem provedor real)
    @Transactional
    public Pedido confirmarPagamento(Long pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);
        validarPodeReceberPagamento(pedido);

        pedido.setStatusPagamento(StatusPagamento.PAGO);
        pedido.setDataPagamento(LocalDateTime.now());
        return repository.save(pedido);
    }

    // Aceita o formato novo (lista de itens) e o antigo (um produto so)
    private List<ItemPedidoRequest> obterItens(PedidoRequest request) {
        if (request.getItens() != null && !request.getItens().isEmpty()) {
            return request.getItens();
        }
        if (request.getProdutoId() != null) {
            return List.of(new ItemPedidoRequest(request.getProdutoId(), request.getQuantidade()));
        }
        return List.of();
    }

    private void validarPodeReceberPagamento(Pedido pedido) {
        if (STATUS_CANCELADO.equals(pedido.getStatus())) {
            throw new IllegalStateException("Pedido cancelado nao pode ser pago");
        }
        if (pedido.getStatusPagamento() == StatusPagamento.PAGO) {
            throw new IllegalStateException("Pedido ja foi pago");
        }
    }
}