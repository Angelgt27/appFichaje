package com.example.appfichaje.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.appfichaje.api.ClienteApi;
import com.example.appfichaje.datos.GestorSesion;
import com.example.appfichaje.modelos.RespuestaLogin;
import com.example.appfichaje.modelos.SolicitudLogin;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {
    private MutableLiveData<String> resultadoLogin = new MutableLiveData<>();
    private MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private GestorSesion gestorSesion;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        gestorSesion = new GestorSesion(application);
    }

    public MutableLiveData<String> getResultadoLogin() { return resultadoLogin; }
    public MutableLiveData<Boolean> getCargando() { return cargando; }

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
}