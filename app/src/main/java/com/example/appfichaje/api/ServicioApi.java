package com.example.appfichaje.api;

import com.example.appfichaje.modelos.RespuestaApi;
import com.example.appfichaje.modelos.RespuestaEstado;
import com.example.appfichaje.modelos.RespuestaLogin;
import com.example.appfichaje.modelos.RespuestaNotificacion;
import com.example.appfichaje.modelos.SolicitudFichaje;
import com.example.appfichaje.modelos.SolicitudLogin;
import com.example.appfichaje.modelos.SolicitudRecuperar;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ServicioApi {
    @POST("api/auth/login")
    Call<RespuestaLogin> login(@Body SolicitudLogin solicitud);

    // NUEVO: Endpoint para invalidar el token en el servidor
    @POST("api/auth/logout")
    Call<RespuestaApi> logout();

    @POST("api/presencia/entrada")
    Call<RespuestaApi> ficharEntrada(@Body SolicitudFichaje solicitud);

    @POST("api/presencia/salida")
    Call<RespuestaApi> ficharSalida(@Body SolicitudFichaje solicitud);

    @GET("api/presencia/estado")
    Call<RespuestaEstado> obtenerEstado();

    @POST("api/auth/reset-password-request")
    Call<RespuestaApi> recuperarPassword(@Body SolicitudRecuperar solicitud);

    @POST("api/incidencias")
    Call<RespuestaApi> registrarIncidencia(@Body Map<String, String> body);

    @GET("api/presencia/resumen-mensual")
    Call<Map<String, Object>> obtenerResumenMensual(@Query("mes") String mes);

    @GET("api/presencia/notificacion-pendiente")
    Call<RespuestaNotificacion> comprobarNotificacionPendiente();
}