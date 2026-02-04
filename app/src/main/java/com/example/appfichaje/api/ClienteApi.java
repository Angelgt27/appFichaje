package com.example.appfichaje.api;

import android.content.Context;

import com.example.appfichaje.datos.GestorSesion;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClienteApi {
    private static final String URL_BASE = "https://angelgt27.eu.pythonanywhere.com/";
    private static Retrofit retrofit = null;

    public static ServicioApi obtenerCliente(Context contexto) {
        if (retrofit == null) {
            GestorSesion sesion = new GestorSesion(contexto);

            OkHttpClient clienteHttp = new OkHttpClient.Builder().addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder constructor = original.newBuilder();

                String token = sesion.obtenerToken();
                if (token != null) {
                    constructor.header("Authorization", "Bearer " + token);
                }
                return chain.proceed(constructor.build());
            }).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(URL_BASE)
                    .client(clienteHttp)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ServicioApi.class);
    }
}
