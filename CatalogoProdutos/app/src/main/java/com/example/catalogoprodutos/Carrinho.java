package com.example.catalogoprodutos;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.UUID;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Carrinho {

    private static final String ARQUIVO = "carrinho_produtos";
    private static final String CHAVE_ITENS = "itens";

    private final SharedPreferences preferencias;

    public Carrinho(Context contexto) {
        preferencias = contexto.getApplicationContext()
                .getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE);
    }

    public static class Item {

        private final long produtoId;
        private final String nome;
        private final BigDecimal preco;
        private final String urlImagem;
        private final int estoque;
        private int quantidade;

        private Item(
                long produtoId,
                String nome,
                BigDecimal preco,
                String urlImagem,
                int estoque,
                int quantidade
        ) {
            this.produtoId = produtoId;
            this.nome = nome;
            this.preco = preco;
            this.urlImagem = urlImagem;
            this.estoque = estoque;
            this.quantidade = quantidade;
        }

        public long getProdutoId() {
            return produtoId;
        }

        public String getNome() {
            return nome;
        }

        public BigDecimal getPreco() {
            return preco;
        }

        public String getUrlImagem() {
            return urlImagem;
        }

        public int getEstoque() {
            return estoque;
        }

        public int getQuantidade() {
            return quantidade;
        }

        public BigDecimal getSubtotal() {
            return preco.multiply(BigDecimal.valueOf(quantidade));
        }
    }

    public List<Item> listar() {
        String conteudo = preferencias.getString(CHAVE_ITENS, "[]");
        List<Item> itens = new ArrayList<>();

        try {
            JSONArray dados = new JSONArray(conteudo);

            for (int i = 0; i < dados.length(); i++) {
                JSONObject objeto = dados.getJSONObject(i);

                long id = objeto.getLong("produtoId");
                String nome = objeto.getString("nome");
                BigDecimal preco = new BigDecimal(
                        objeto.getString("preco")
                );
                int estoque = objeto.getInt("estoque");
                int quantidade = objeto.getInt("quantidade");

                if (id <= 0 || nome.trim().isEmpty()
                        || preco.signum() < 0
                        || estoque <= 0
                        || quantidade <= 0
                        || quantidade > estoque) {
                    throw new IllegalArgumentException(
                            "Item inválido no carrinho."
                    );
                }

                itens.add(new Item(
                        id,
                        nome,
                        preco,
                        objeto.optString("urlImagem", ""),
                        estoque,
                        quantidade
                ));
            }

            return itens;

        } catch (JSONException | IllegalArgumentException erro) {
            throw new IllegalStateException(
                    "Não foi possível ler o carrinho salvo.",
                    erro
            );
        }
    }

    public void adicionar(
            long produtoId,
            String nome,
            BigDecimal preco,
            String urlImagem,
            int estoque
    ) {
        if (produtoId <= 0 || nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Produto inválido.");
        }

        if (preco == null || preco.signum() < 0) {
            throw new IllegalArgumentException(
                    "O produto não possui um preço válido."
            );
        }

        if (estoque <= 0) {
            throw new IllegalArgumentException(
                    "Produto indisponível em estoque."
            );
        }

        List<Item> itens = listar();

        for (int i = 0; i < itens.size(); i++) {
            Item item = itens.get(i);

            if (item.getProdutoId() == produtoId) {
                if (item.getQuantidade() >= estoque) {
                    throw new IllegalArgumentException(
                            "Você atingiu o estoque informado "
                                    + "para este produto."
                    );
                }

                itens.set(i, new Item(
                        produtoId,
                        nome,
                        preco,
                        urlImagem == null ? "" : urlImagem,
                        estoque,
                        item.getQuantidade() + 1
                ));

                salvar(itens);
                return;
            }
        }

        itens.add(new Item(
                produtoId,
                nome,
                preco,
                urlImagem == null ? "" : urlImagem,
                estoque,
                1
        ));

        salvar(itens);
    }

    public void alterarQuantidade(long produtoId, int quantidade) {
        List<Item> itens = listar();

        for (Item item : itens) {
            if (item.getProdutoId() == produtoId) {
                if (quantidade <= 0) {
                    throw new IllegalArgumentException(
                            "A quantidade mínima é 1. "
                                    + "Use Remover para excluir o item."
                    );
                }

                if (quantidade > item.getEstoque()) {
                    throw new IllegalArgumentException(
                            "Quantidade acima do estoque informado."
                    );
                }

                item.quantidade = quantidade;
                salvar(itens);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Produto não encontrado no carrinho."
        );
    }

    public void remover(long produtoId) {
        List<Item> itens = listar();

        for (int i = 0; i < itens.size(); i++) {
            if (itens.get(i).getProdutoId() == produtoId) {
                itens.remove(i);
                salvar(itens);
                return;
            }
        }
    }

    public BigDecimal calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;

        for (Item item : listar()) {
            total = total.add(item.getSubtotal());
        }

        return total;
    }

    private void salvar(List<Item> itens) {
        if (temPedidoPendente()) {
            throw new IllegalStateException(
                    "Existe uma confirmação pendente. "
                            + "Abra o carrinho para verificar o resultado."
            );
        }
        JSONArray dados = new JSONArray();

        try {
            for (Item item : itens) {
                JSONObject objeto = new JSONObject();

                objeto.put("produtoId", item.getProdutoId());
                objeto.put("nome", item.getNome());
                objeto.put("preco", item.getPreco().toPlainString());
                objeto.put("urlImagem", item.getUrlImagem());
                objeto.put("estoque", item.getEstoque());
                objeto.put("quantidade", item.getQuantidade());

                dados.put(objeto);
            }
        } catch (JSONException erro) {
            throw new IllegalStateException(
                    "Não foi possível preparar o carrinho.",
                    erro
            );
        }

        boolean salvo = preferencias.edit()
                .putString(CHAVE_ITENS, dados.toString())
                .commit();

        if (!salvo) {
            throw new IllegalStateException(
                    "Não foi possível salvar o carrinho no celular."
            );
        }
    }
    public boolean temPedidoPendente() {
        return preferencias.contains("pedido_pendente");
    }

    public String obterPedidoPendente() {
        return preferencias.getString("pedido_pendente", null);
    }

    public String prepararPedido() {
        String pendente = obterPedidoPendente();

        if (pendente != null) {
            return pendente;
        }

        List<Item> itens = listar();

        if (itens.isEmpty()) {
            throw new IllegalStateException(
                    "Adicione produtos antes de confirmar."
            );
        }

        if (itens.size() > 100) {
            throw new IllegalStateException(
                    "O pedido permite até 100 produtos diferentes."
            );
        }

        JSONArray produtos = new JSONArray();
        BigDecimal total = BigDecimal.ZERO;

        try {
            for (Item item : itens) {
                JSONObject produto = new JSONObject();

                produto.put("produtoId", item.getProdutoId());
                produto.put("quantidade", item.getQuantidade());

                produtos.put(produto);
                total = total.add(item.getSubtotal());
            }

            JSONObject pedido = new JSONObject();

            pedido.put(
                    "chaveConfirmacao",
                    UUID.randomUUID().toString()
            );
            pedido.put("totalEsperado", total.toPlainString());
            pedido.put("itens", produtos);

            String conteudo = pedido.toString();

            boolean salvo = preferencias.edit()
                    .putString("pedido_pendente", conteudo)
                    .commit();

            if (!salvo) {
                throw new IllegalStateException(
                        "Não foi possível guardar a confirmação. "
                                + "O pedido não será enviado."
                );
            }

            return conteudo;

        } catch (JSONException erro) {
            throw new IllegalStateException(
                    "Não foi possível preparar o pedido.",
                    erro
            );
        }
    }

    public void concluirPedido(String resposta) {
        String pendente = obterPedidoPendente();

        if (pendente == null) {
            throw new IllegalStateException(
                    "Não há confirmação pendente para este carrinho."
            );
        }

        try {
            JSONObject enviado = new JSONObject(pendente);
            JSONObject recebido = new JSONObject(resposta);

            boolean mesmaChave = enviado.getString("chaveConfirmacao")
                    .equals(recebido.getString("chaveConfirmacao"));

            BigDecimal esperado = new BigDecimal(
                    enviado.getString("totalEsperado")
            );

            BigDecimal confirmado = new BigDecimal(
                    recebido.getString("total")
            );

            if (!mesmaChave
                    || recebido.getLong("id") <= 0
                    || esperado.compareTo(confirmado) != 0) {
                throw new IllegalStateException(
                        "A resposta não corresponde à confirmação enviada. "
                                + "O carrinho foi preservado."
                );
            }

            boolean salvo = preferencias.edit()
                    .putString("ultimo_pedido", resposta)
                    .remove(CHAVE_ITENS)
                    .remove("pedido_pendente")
                    .commit();

            if (!salvo) {
                throw new IllegalStateException(
                        "O pedido foi confirmado, mas houve uma falha "
                                + "ao salvar o comprovante no celular."
                );
            }

        } catch (JSONException | NumberFormatException erro) {
            throw new IllegalStateException(
                    "Não foi possível interpretar a confirmação. "
                            + "Verifique novamente o resultado.",
                    erro
            );
        }
    }

    public void liberarPedidoRecusado(String chaveConfirmacao) {
        String pendente = obterPedidoPendente();

        if (pendente == null) {
            return;
        }

        try {
            JSONObject enviado = new JSONObject(pendente);

            if (!enviado.getString("chaveConfirmacao")
                    .equals(chaveConfirmacao)) {
                throw new IllegalStateException(
                        "A confirmação pendente foi alterada."
                );
            }

            boolean salvo = preferencias.edit()
                    .remove("pedido_pendente")
                    .commit();

            if (!salvo) {
                throw new IllegalStateException(
                        "Não foi possível atualizar a confirmação local."
                );
            }

        } catch (JSONException erro) {
            throw new IllegalStateException(
                    "Não foi possível ler a confirmação pendente.",
                    erro
            );
        }
    }

    public String obterUltimoPedido() {
        return preferencias.getString("ultimo_pedido", null);
    }
}