package com.example.appmanutencao.api;

import com.example.appmanutencao.model.AgendamentoRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/agendamentos")
    Call<Void> criarAgendamento(@Body AgendamentoRequest agendamento);
}
