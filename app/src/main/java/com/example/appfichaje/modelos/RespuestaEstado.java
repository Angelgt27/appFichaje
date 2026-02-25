package com.example.appfichaje.modelos;

import com.google.gson.annotations.SerializedName;

public class RespuestaEstado {
    private String estado;

    @SerializedName("ultima_marca")
    private String ultimaMarca;

    public String getEstado() { return estado; }
    public String getUltimaMarca() { return ultimaMarca; }
}