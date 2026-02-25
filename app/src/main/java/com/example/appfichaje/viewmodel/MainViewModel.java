package com.example.appfichaje.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.example.appfichaje.api.ClienteApi;
import com.example.appfichaje.modelos.RespuestaApi;
import com.example.appfichaje.modelos.RespuestaEstado;
import com.example.appfichaje.modelos.SolicitudFichaje;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainViewModel extends AndroidViewModel {
    private FusedLocationProviderClient clienteUbicacion;
    public MutableLiveData<String> mensajeEstado = new MutableLiveData<>();
    public MutableLiveData<Boolean> procesando = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> sesionCaducada = new MutableLiveData<>(false);

    // NUEVAS VARIABLES para NFC y Logout
    public MutableLiveData<Boolean> estaDentro = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> logoutCompletado = new MutableLiveData<>(false);

    public MainViewModel(@NonNull Application application) {
        super(application);
        clienteUbicacion = LocationServices.getFusedLocationProviderClient(application);
    }

    public void comprobarEstadoSesion() {
        procesando.setValue(true);
        mensajeEstado.setValue("Comprobando sesión...");

        ClienteApi.obtenerCliente(getApplication()).obtenerEstado().enqueue(new Callback<RespuestaEstado>() {
            @Override
            public void onResponse(Call<RespuestaEstado> call, Response<RespuestaEstado> response) {
                procesando.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    String estado = response.body().getEstado().toUpperCase();
                    String ultimaMarca = convertirHoraUTCaLocal(response.body().getUltimaMarca());

                    // Guardamos el estado para que el NFC sepa qué hacer
                    estaDentro.setValue("DENTRO".equalsIgnoreCase(estado));

                    mensajeEstado.setValue("Estado actual: " + estado + "\nÚltima marca: " + ultimaMarca);
                } else if (response.code() == 401) {
                    sesionCaducada.setValue(true);
                } else {
                    mensajeEstado.setValue("Error al cargar estado inicial.");
                }
            }

            @Override
            public void onFailure(Call<RespuestaEstado> call, Throwable t) {
                procesando.setValue(false);
                mensajeEstado.setValue("Error de conexión al servidor.");
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void realizarFichaje(boolean esEntrada) {
        procesando.setValue(true);
        mensajeEstado.setValue("Obteniendo ubicación GPS precisa...");

        // MEJORA: Obtiene la ubicación actual real, no la última guardada en caché
        clienteUbicacion.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(ubicacion -> {
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
        Call<RespuestaApi> llamada = esEntrada ?
                ClienteApi.obtenerCliente(getApplication()).ficharEntrada(datos) :
                ClienteApi.obtenerCliente(getApplication()).ficharSalida(datos);

        llamada.enqueue(new Callback<RespuestaApi>() {
            @Override
            public void onResponse(Call<RespuestaApi> call, Response<RespuestaApi> response) {
                procesando.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    String horaLocal = convertirHoraUTCaLocal(response.body().getHora());
                    mensajeEstado.setValue(response.body().getMessage() + "\nHora: " + horaLocal);
                    estaDentro.setValue(esEntrada); // Actualizar estado local tras fichar
                } else {
                    if (response.code() == 401) {
                        sesionCaducada.setValue(true);
                        return;
                    }
                    try {
                        String errorJson = response.errorBody().string();
                        JSONObject json = new JSONObject(errorJson);
                        String mensajeError = json.optString("message", json.optString("msg", "Error interno"));
                        mensajeEstado.setValue("Error: " + mensajeError);
                    } catch (Exception e) {
                        mensajeEstado.setValue("Error del servidor (" + response.code() + ")");
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


    public void cerrarSesionApi() {
        procesando.setValue(true);
        ClienteApi.obtenerCliente(getApplication()).logout().enqueue(new Callback<RespuestaApi>() {
            @Override
            public void onResponse(Call<RespuestaApi> call, Response<RespuestaApi> response) {
                procesando.setValue(false);
                logoutCompletado.setValue(true);
            }

            @Override
            public void onFailure(Call<RespuestaApi> call, Throwable t) {
                procesando.setValue(false);
                logoutCompletado.setValue(true); // Cerrar localmente aunque falle la red
            }
        });
    }

    private String convertirHoraUTCaLocal(String horaOriginal) {
        if (horaOriginal == null || horaOriginal.equals("N/A")) return "Ninguna";
        try {
            if (horaOriginal.length() > 8) horaOriginal = horaOriginal.substring(0, 8);
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            formatoEntrada.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date fecha = formatoEntrada.parse(horaOriginal);

            SimpleDateFormat formatoSalida = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            // MEJORA: Zona horaria dinámica del dispositivo
            formatoSalida.setTimeZone(TimeZone.getDefault());
            return formatoSalida.format(fecha);
        } catch (Exception e) {
            return horaOriginal;
        }
    }

}