package com.example.appfichaje.api;

import com.example.appfichaje.modelos.RespuestaApi;
import com.example.appfichaje.modelos.RespuestaLogin;
import com.example.appfichaje.modelos.SolicitudFichaje;
import com.example.appfichaje.modelos.SolicitudLogin;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ServicioApi {
    @POST("api/auth/login")
    Call<RespuestaLogin> login(@Body SolicitudLogin solicitud);

    @POST("api/presencia/entrada")
    Call<RespuestaApi> ficharEntrada(@Body SolicitudFichaje solicitud);

    @POST("api/presencia/salida")
    Call<RespuestaApi> ficharSalida(@Body SolicitudFichaje solicitud);
}
