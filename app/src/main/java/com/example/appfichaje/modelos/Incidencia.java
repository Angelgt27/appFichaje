package com.example.appfichaje.modelos;

import com.google.gson.annotations.SerializedName;

public class Incidencia {
    @SerializedName("id_incidencia")
    private int idIncidencia;
    private String fecha;
    private String hora;
    private String descripcion;

    public int getIdIncidencia() { return idIncidencia; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getDescripcion() { return descripcion; }
}