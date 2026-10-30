package com.example.appmanutencao.api;

import com.example.appmanutencao.model.AgendamentoRequest;
import com.example.appmanutencao.model.Cliente;
import com.example.appmanutencao.model.Mensagem;
import com.example.appmanutencao.model.OrdemServico;
import com.example.appmanutencao.model.Pedido;
import com.example.appmanutencao.model.PedidoRequest;
import com.example.appmanutencao.model.PixResponse;
import com.example.appmanutencao.model.Produto;
import com.example.appmanutencao.model.Servico;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Clientes
    @GET("api/clientes")
    Call<List<Cliente>> listarClientes();

    @PUT("api/clientes/{id}")
    Call<Cliente> atualizarCliente(
            @Path("id") Long id,
            @Body Cliente cliente
    );

    // Serviços
    @GET("api/servicos")
    Call<List<Servico>> listarServicos();

    // Agendamentos / Ordens de Serviço
    @GET("api/agendamentos")
    Call<List<OrdemServico>> listarAgendamentos();

    @POST("api/agendamentos")
    Call<OrdemServico> criarAgendamento(
            @Body AgendamentoRequest request
    );

    @PATCH("api/agendamentos/{id}/status")
    Call<Void> atualizarStatusAgendamento(
            @Path("id") Long id,
            @Query("novoStatus") String novoStatus
    );

    // Chat
    @GET("api/mensagens/{ordemServicoId}")
    Call<List<Mensagem>> listarMensagens(
            @Path("ordemServicoId") Long ordemServicoId
    );

    @POST("api/mensagens")
    Call<Mensagem> enviarMensagem(
            @Body Mensagem mensagem
    );

    // Produtos
    @GET("api/produtos")
    Call<List<Produto>> listarProdutos();

    @POST("api/produtos")
    Call<Produto> criarProduto(@Body Produto produto);

    @PUT("api/produtos/{id}")
    Call<Produto> atualizarProduto(
            @Path("id") Long id,
            @Body Produto produto
    );

    @DELETE("api/produtos/{id}")
    Call<Void> deletarProduto(@Path("id") Long id);

    // Pedidos / Pagamento
    @GET("api/pedidos")
    Call<List<Pedido>> listarPedidos();

    @GET("api/pedidos/cliente/{clienteId}")
    Call<List<Pedido>> listarPedidosPorCliente(
            @Path("clienteId") Long clienteId
    );

    @POST("api/pedidos")
    Call<Pedido> criarPedido(
            @Body PedidoRequest request
    );

    @GET("api/pedidos/{id}/pix")
    Call<PixResponse> gerarPix(
            @Path("id") Long pedidoId
    );

    @POST("api/pedidos/{id}/pagamento/confirmar")
    Call<Pedido> confirmarPagamento(
            @Path("id") Long pedidoId
    );
}