package com.example.appmanutencao.model;

import com.google.gson.annotations.SerializedName;

public class ClienteRef {
    @SerializedName("id")
    private Long id;

    public ClienteRef(Long id) {
        this.id = id;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}