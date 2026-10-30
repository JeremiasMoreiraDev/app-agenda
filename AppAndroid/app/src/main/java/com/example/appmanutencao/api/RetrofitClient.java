package com.example.appmanutencao.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Celular fisico: IP do notebook na rede Wi-Fi (descubra com "hostname -I")
    // Emulador Android Studio: use "http://10.0.2.2:8080/"
    private static final String BASE_URL = "http://192.168.15.8:8080/";

    private static Retrofit retrofit = null;

    // Retorna o Retrofit
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Retorna o serviço da API
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}