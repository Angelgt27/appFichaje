package com.example.appfichaje.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.appfichaje.api.ClienteApi;
import com.example.appfichaje.modelos.RespuestaApi;
import com.example.appfichaje.modelos.SolicitudFichaje;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends AndroidViewModel {
    private FusedLocationProviderClient clienteUbicacion;
    public MutableLiveData<String> mensajeEstado = new MutableLiveData<>();
    public MutableLiveData<Boolean> procesando = new MutableLiveData<>(false);

    public MainViewModel(@NonNull Application application) {
        super(application);
        clienteUbicacion = LocationServices.getFusedLocationProviderClient(application);
    }

    @SuppressLint("MissingPermission") // Se verifica en la Activity antes de llamar
    public void realizarFichaje(boolean esEntrada) {
        procesando.setValue(true);
        mensajeEstado.setValue("Obteniendo ubicación GPS...");

        clienteUbicacion.getLastLocation().addOnSuccessListener(ubicacion -> {
            if (ubicacion != null) {
                enviarAlServidor(ubicacion, esEntrada);
            } else {
                procesando.setValue(false);
                mensajeEstado.setValue("No se pudo obtener ubicación. Enciende el GPS.");
            }
        }).addOnFailureListener(e -> {
            procesando.setValue(false);
            mensajeEstado.setValue("Error GPS: " + e.getMessage());
        });
    }

    private void enviarAlServidor(Location ubicacion, boolean esEntrada) {
        SolicitudFichaje datos = new SolicitudFichaje(ubicacion.getLatitude(), ubicacion.getLongitude());
        Call<RespuestaApi> llamada;

        if (esEntrada) {
            llamada = ClienteApi.obtenerCliente(getApplication()).ficharEntrada(datos);
        } else {
            llamada = ClienteApi.obtenerCliente(getApplication()).ficharSalida(datos);
        }

        llamada.enqueue(new Callback<RespuestaApi>() {
            @Override
            public void onResponse(Call<RespuestaApi> call, Response<RespuestaApi> response) {
                procesando.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    mensajeEstado.setValue(response.body().getMessage() + "\nHora: " + response.body().getHora());
                } else {
                    // Intentamos leer el mensaje de error del JSON que devuelve Flask (ej: fuera de radio)
                    try {
                        String errorJson = response.errorBody().string();
                        JSONObject json = new JSONObject(errorJson);
                        String mensajeError = json.optString("message", "Error desconocido");
                        mensajeEstado.setValue("Error: " + mensajeError);
                    } catch (Exception e) {
                        mensajeEstado.setValue("Error del servidor: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<RespuestaApi> call, Throwable t) {
                procesando.setValue(false);
                mensajeEstado.setValue("Error de red: " + t.getMessage());
            }
        });
    }
}