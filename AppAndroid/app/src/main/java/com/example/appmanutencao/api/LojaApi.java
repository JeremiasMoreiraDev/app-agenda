package com.example.appmanutencao.api;

import com.example.appmanutencao.model.Pedido;
import com.example.appmanutencao.model.PedidoRequest;
import com.example.appmanutencao.model.ProdutoLoja;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

// Interface so da loja (produtos e pedidos). Nao mexe no seu ApiService.
public interface LojaApi {

    @GET("api/produtos")
    Call<List<ProdutoLoja>> listarProdutos();

    @POST("api/pedidos")
    Call<Pedido> comprar(@Body PedidoRequest pedido);

    @GET("api/pedidos/cliente/{id}")
    Call<List<Pedido>> listarPedidosDoCliente(@Path("id") long clienteId);
}