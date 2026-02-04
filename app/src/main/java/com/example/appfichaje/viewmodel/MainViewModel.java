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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

    @SuppressLint("MissingPermission")
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
                    // AQUÍ ESTÁ EL CAMBIO: Convertimos la hora antes de mostrarla
                    String horaServer = response.body().getHora();
                    String horaLocal = convertirHoraUTCaLocal(horaServer);

                    mensajeEstado.setValue(response.body().getMessage() + "\nHora: " + horaLocal);
                } else {
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

    // Función auxiliar para sumar 1 hora (UTC a UTC+1)
    private String convertirHoraUTCaLocal(String horaOriginal) {
        try {
            // El servidor suele enviar formato con milisegundos: "14:30:00.123456"
            // Cortamos para quedarnos solo con HH:mm:ss
            if (horaOriginal != null && horaOriginal.length() > 8) {
                horaOriginal = horaOriginal.substring(0, 8);
            }

            SimpleDateFormat formatoEntrada = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            formatoEntrada.setTimeZone(TimeZone.getTimeZone("UTC")); // Le decimos que lo que entra es UTC

            Date fecha = formatoEntrada.parse(horaOriginal);

            SimpleDateFormat formatoSalida = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            // Aquí ponemos "GMT+1" o "Europe/Madrid"
            formatoSalida.setTimeZone(TimeZone.getTimeZone("GMT+1"));

            return formatoSalida.format(fecha);
        } catch (Exception e) {
            return horaOriginal; // Si falla algo, devolvemos la hora tal cual vino
        }
    }
}