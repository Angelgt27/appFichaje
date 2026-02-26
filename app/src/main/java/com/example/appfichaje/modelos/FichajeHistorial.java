package com.example.appfichaje.modelos;
import com.google.gson.annotations.SerializedName;

public class FichajeHistorial {
    private String fecha;
    @SerializedName("hora_entrada")
    private String horaEntrada;
    @SerializedName("hora_salida")
    private String horaSalida;

    public String getFecha() { return fecha; }
    public String getHoraEntrada() { return horaEntrada; }
    public String getHoraSalida() { return horaSalida; }
}