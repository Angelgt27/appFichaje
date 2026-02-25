package com.example.appfichaje.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.appfichaje.datos.GestorSesion;
import com.example.appfichaje.api.ClienteApi;
import com.example.appfichaje.modelos.RespuestaApi;
import com.example.appfichaje.modelos.RespuestaLogin;
import com.example.appfichaje.modelos.SolicitudLogin;
import com.example.appfichaje.modelos.SolicitudRecuperar;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {
    private MutableLiveData<String> resultadoLogin = new MutableLiveData<>();
    private MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);

    // NUEVO: LiveData para el resultado de recuperar contraseña
    private MutableLiveData<String> mensajeRecuperacion = new MutableLiveData<>();

    private GestorSesion gestorSesion;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        gestorSesion = new GestorSesion(application);
    }

    public MutableLiveData<String> getResultadoLogin() { return resultadoLogin; }
    public MutableLiveData<Boolean> getCargando() { return cargando; }
    public MutableLiveData<String> getMensajeRecuperacion() { return mensajeRecuperacion; }

    public void login(String email, String password) {
        cargando.setValue(true);
        SolicitudLogin solicitud = new SolicitudLogin(email, password);

        ClienteApi.obtenerCliente(getApplication()).login(solicitud)
                .enqueue(new Callback<RespuestaLogin>() {
                    @Override
                    public void onResponse(Call<RespuestaLogin> call, Response<RespuestaLogin> response) {
                        cargando.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            gestorSesion.guardarToken(response.body().getAccessToken());
                            resultadoLogin.setValue("EXITO");
                        } else {
                            resultadoLogin.setValue("Error: Credenciales incorrectas");
                        }
                    }

                    @Override
                    public void onFailure(Call<RespuestaLogin> call, Throwable t) {
                        cargando.setValue(false);
                        resultadoLogin.setValue("Fallo de conexión: " + t.getMessage());
                    }
                });
    }

    // NUEVA FUNCIÓN: Solicitar correo de recuperación
    public void solicitarRecuperacion(String email) {
        cargando.setValue(true);
        SolicitudRecuperar solicitud = new SolicitudRecuperar(email);

        ClienteApi.obtenerCliente(getApplication()).recuperarPassword(solicitud)
                .enqueue(new Callback<RespuestaApi>() {
                    @Override
                    public void onResponse(Call<RespuestaApi> call, Response<RespuestaApi> response) {
                        cargando.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            // El backend devuelve "message": "Si el email existe en nuestro sistema..."
                            mensajeRecuperacion.setValue(response.body().getMessage());
                        } else {
                            try {
                                String errorJson = response.errorBody().string();
                                JSONObject json = new JSONObject(errorJson);
                                mensajeRecuperacion.setValue("Error: " + json.optString("message", "No se pudo enviar la solicitud"));
                            } catch (Exception e) {
                                mensajeRecuperacion.setValue("Error del servidor al recuperar (" + response.code() + ")");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<RespuestaApi> call, Throwable t) {
                        cargando.setValue(false);
                        mensajeRecuperacion.setValue("Error de red: " + t.getMessage());
                    }
                });
    }
}