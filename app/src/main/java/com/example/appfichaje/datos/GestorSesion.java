package com.example.appfichaje.datos;

import android.content.Context;
import android.content.SharedPreferences;

public class GestorSesion {
    private SharedPreferences prefs;
    private static final String NOMBRE_PREF = "AppPrefs";
    private static final String CLAVE_TOKEN = "token_jwt";

    public GestorSesion(Context contexto) {
        prefs = contexto.getSharedPreferences(NOMBRE_PREF, Context.MODE_PRIVATE);
    }

    public void guardarToken(String token) {
        prefs.edit().putString(CLAVE_TOKEN, token).apply();
    }

    public String obtenerToken() {
        return prefs.getString(CLAVE_TOKEN, null);
    }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }

    public void guardarRol(String rol) {
        prefs.edit().putString("rol_usuario", rol).apply();
    }
    public String obtenerRol() {
        return prefs.getString("rol_usuario", "Empleado");
    }
}